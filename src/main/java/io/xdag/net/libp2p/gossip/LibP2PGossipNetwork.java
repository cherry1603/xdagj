package io.xdag.net.libp2p.gossip;

import com.google.common.base.Preconditions;
import io.libp2p.core.PeerId;
import io.libp2p.core.pubsub.PubsubSubscription;
import io.libp2p.core.pubsub.*;
import io.libp2p.pubsub.*;
import io.libp2p.pubsub.gossip.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.xdag.net.libp2p.LibP2PNodeId;
import io.xdag.net.libp2p.peer.NodeId;
import io.xdag.utils.SafeFuture;
import kotlin.jvm.functions.Function0;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.Hash;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LibP2PGossipNetwork implements GossipNetwork {

    private static final Logger LOG = LogManager.getLogger();

    private static final PubsubRouterMessageValidator STRICT_FIELDS_VALIDATOR =
            new GossipWireValidator();
    private static final Function0<Long> NULL_SEQNO_GENERATOR = () -> null;

    private final Gossip gossip;
    private final PubsubPublisherApi publisher;
    private final TopicHandlers topicHandlers;
    private GossipMessageHandler gossipMessageHandler;
    private GossipHandler gossipHandler;


    public static LibP2PGossipNetwork create(
            GossipConfig gossipConfig,
            PreparedGossipMessageFactory defaultMessageFactory,
            GossipTopicFilter gossipTopicFilter,
            boolean logWireGossip) {

        TopicHandlers topicHandlers = new TopicHandlers();
        Gossip gossip =
                createGossip(
                        gossipConfig, logWireGossip, defaultMessageFactory, gossipTopicFilter, topicHandlers);
        PubsubPublisherApi publisher = gossip.createPublisher(null, NULL_SEQNO_GENERATOR);
        return new LibP2PGossipNetwork(gossip, publisher, topicHandlers);
    }

    private static Gossip createGossip(
            GossipConfig gossipConfig,
            boolean gossipLogsEnabled,
            PreparedGossipMessageFactory defaultMessageFactory,
            GossipTopicFilter gossipTopicFilter,
            TopicHandlers topicHandlers) {
        final GossipParams gossipParams = LibP2PParamsFactory.createGossipParams(gossipConfig);
        final GossipScoreParams scoreParams =
                LibP2PParamsFactory.createGossipScoreParams(gossipConfig.getScoringConfig());

        final TopicSubscriptionFilter subscriptionFilter =
                new MaxCountTopicSubscriptionFilter(100, 200, gossipTopicFilter::isRelevantTopic);
        GossipRouter router =
                new GossipRouter(
                        gossipParams, scoreParams, PubsubProtocol.Gossip_V_1_1, subscriptionFilter) {


                    final SeenCache<Optional<ValidationResult>> seenCache =
                            new TTLSeenCache<>(
                                    new FastIdSeenCache<>(
                                            msg ->

                                                    Bytes.wrap(
                                                            Hash.sha2_256(msg.getProtobufMessage().getData().toByteArray()))),
                                    gossipParams.getSeenTTL(),
                                    getCurTimeMillis());

                    @NotNull
                    @Override
                    protected SeenCache<Optional<ValidationResult>> getSeenMessages() {
                        return seenCache;
                    }

                };

        router.setMessageFactory(
                msg -> {
                    Preconditions.checkArgument(
                            msg.getTopicIDsCount() == 1,
                            "Unexpected number of topics for a single message: " + msg.getTopicIDsCount());
                    String topic = msg.getTopicIDs(0);
                    Bytes payload = Bytes.wrap(msg.getData().toByteArray());

                    PreparedGossipMessage preparedMessage =
                            topicHandlers
                                    .getHandlerForTopic(topic)
                                    .map(handler -> handler.prepareMessage(payload))
                                    .orElse(defaultMessageFactory.create(topic, payload));

                    return new PreparedPubsubMessage(msg, preparedMessage);
                });
        router.setMessageValidator(STRICT_FIELDS_VALIDATOR);

        ChannelHandler debugHandler =
                gossipLogsEnabled ? new LoggingHandler("wire.gossip", LogLevel.DEBUG) : null;
        PubsubApi pubsubApi = PubsubApiKt.createPubsubApi(router);

        return new Gossip(router, pubsubApi, debugHandler);
    }

    public LibP2PGossipNetwork(
            Gossip gossip,
            PubsubPublisherApi publisher,
            TopicHandlers topicHandlers) {
        this.gossip = gossip;
        this.publisher = publisher;
        this.topicHandlers = topicHandlers;
    }

    @Override
    public SafeFuture<?> gossip(final String topic, final Bytes data) {
        return SafeFuture.of(
                publisher.publish(Unpooled.wrappedBuffer(data.toArrayUnsafe()), new Topic(topic)));
    }

    @Override
    public TopicChannel subscribe(final String topic, final TopicHandler topicHandler) {
        LOG.trace("Subscribe to topic: {}", topic);
        topicHandlers.add(topic, topicHandler);
        final Topic libP2PTopic = new Topic(topic);
        gossipHandler =
                new GossipHandler(libP2PTopic, publisher, topicHandler);
        PubsubSubscription subscription = gossip.subscribe(gossipHandler, libP2PTopic);
        return new LibP2PTopicChannel(gossipHandler, subscription);
    }

    @Override
    public Map<String, Collection<NodeId>> getSubscribersByTopic() {
        Map<PeerId, Set<Topic>> peerTopics = gossip.getPeerTopics().join();
        final Map<String, Collection<NodeId>> result = new HashMap<>();
        for (Map.Entry<PeerId, Set<Topic>> peerTopic : peerTopics.entrySet()) {
            final LibP2PNodeId nodeId = new LibP2PNodeId(peerTopic.getKey());
            peerTopic
                    .getValue()
                    .forEach(
                            topic -> result.computeIfAbsent(topic.getTopic(), __ -> new HashSet<>()).add(nodeId));
        }
        return result;
    }

    @Override
    public void updateGossipTopicScoring(final GossipTopicsScoringConfig config) {
        if (config.isEmpty()) {
            return;
        }

        final Map<String, GossipTopicScoreParams> params =
                config.getTopicConfigs().entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> LibP2PParamsFactory.createTopicScoreParams(e.getValue())));
        gossip.updateTopicScoreParams(params);
    }

    @Override
    public GossipMessageHandler getGossipMessageHandler() {
        return gossipMessageHandler;
    }

    public void setGossipMessageHandler(GossipMessageHandler gossipMessageHandler) {
        gossipHandler.setGossipMessageHandler(gossipMessageHandler);
    }

    public Gossip getGossip() {
        return gossip;
    }

    private static class TopicHandlers {

        private final Map<String, TopicHandler> topicToHandlerMap = new ConcurrentHashMap<>();

        public void add(String topic, TopicHandler handler) {
            topicToHandlerMap.put(topic, handler);
        }

        public Optional<TopicHandler> getHandlerForTopic(String topic) {
            return Optional.ofNullable(topicToHandlerMap.get(topic));
        }
    }
}
