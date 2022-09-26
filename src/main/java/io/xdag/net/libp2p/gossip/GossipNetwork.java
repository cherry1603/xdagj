package io.xdag.net.libp2p.gossip;

import io.xdag.net.libp2p.peer.NodeId;
import io.xdag.utils.SafeFuture;
import org.apache.tuweni.bytes.Bytes;

import java.util.Collection;
import java.util.Map;

public interface GossipNetwork {
    SafeFuture<?> gossip(String topic, Bytes data);

    TopicChannel subscribe(String topic, TopicHandler topicHandler);

    Map<String, Collection<NodeId>> getSubscribersByTopic();

    void updateGossipTopicScoring(final GossipTopicsScoringConfig config);

    GossipMessageHandler getGossipMessageHandler();
}
