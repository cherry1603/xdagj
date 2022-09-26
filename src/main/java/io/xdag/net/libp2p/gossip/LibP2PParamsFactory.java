package io.xdag.net.libp2p.gossip;

import io.libp2p.core.PeerId;
import io.libp2p.pubsub.gossip.*;
import io.libp2p.pubsub.gossip.builders.GossipPeerScoreParamsBuilder;
import io.xdag.net.libp2p.LibP2PNodeId;
import kotlin.jvm.functions.Function1;

import java.util.Map;
import java.util.stream.Collectors;

public class LibP2PParamsFactory {
    public static GossipParams createGossipParams(final GossipConfig gossipConfig) {
        return GossipParams.builder()
                .D(gossipConfig.getD())
                .DLow(gossipConfig.getDLow())
                .DHigh(gossipConfig.getDHigh())
                .DLazy(gossipConfig.getDLazy())
                // Calculate dScore and dOut based on other params
                .DScore(gossipConfig.getD() * 2 / 3)
                .DOut(Math.min(gossipConfig.getD() / 2, Math.max(0, gossipConfig.getDLow()) - 1))
                .fanoutTTL(gossipConfig.getFanoutTTL())
                .gossipSize(gossipConfig.getAdvertise())
                .gossipHistoryLength(gossipConfig.getHistory())
                .heartbeatInterval(gossipConfig.getHeartbeatInterval())
                .floodPublish(true)
                .seenTTL(gossipConfig.getSeenTTL())
                .maxPublishedMessages(1000)
                .maxTopicsPerPublishedMessage(1)
                .maxSubscriptions(200)
                .maxGraftMessages(200)
                .maxPruneMessages(200)
                .maxPeersPerPruneMessage(1000)
                .maxIHaveLength(5000)
                .maxIWantMessageIds(5000)
                .build();
    }

    public static GossipScoreParams createGossipScoreParams(final GossipScoringConfig config) {
        return GossipScoreParams.builder()
                .peerScoreParams(createPeerScoreParams(config.getPeerScoringConfig()))
                .topicsScoreParams(createTopicsScoreParams(config))
                .gossipThreshold(config.getGossipThreshold())
                .publishThreshold(config.getPublishThreshold())
                .graylistThreshold(config.getGraylistThreshold())
                .acceptPXThreshold(config.getAcceptPXThreshold())
                .opportunisticGraftThreshold(config.getOpportunisticGraftThreshold())
                .build();
    }

    public static GossipPeerScoreParams createPeerScoreParams(final GossipPeerScoringConfig config) {
        final GossipPeerScoreParamsBuilder builder =
                GossipPeerScoreParams.builder()
                        .topicScoreCap(config.getTopicScoreCap())
                        .appSpecificWeight(config.getAppSpecificWeight())
                        .ipColocationFactorWeight(config.getIpColocationFactorWeight())
                        .ipColocationFactorThreshold(config.getIpColocationFactorThreshold())
                        .behaviourPenaltyWeight(config.getBehaviourPenaltyWeight())
                        .behaviourPenaltyDecay(config.getBehaviourPenaltyDecay())
                        .behaviourPenaltyThreshold(config.getBehaviourPenaltyThreshold())
                        .decayInterval(config.getDecayInterval())
                        .decayToZero(config.getDecayToZero())
                        .retainScore(config.getRetainScore());

        // Configure optional params
        config
                .getAppSpecificScorer()
                .ifPresent(
                        scorer -> {
                            final Function1<? super PeerId, Double> appSpecificScore =
                                    peerId -> scorer.scorePeer(new LibP2PNodeId(peerId));
                            builder.appSpecificScore(appSpecificScore);
                        });

        config
                .getDirectPeerManager()
                .ifPresent(
                        mgr -> {
                            final Function1<? super PeerId, Boolean> isDirectPeer =
                                    peerId -> mgr.isDirectPeer(new LibP2PNodeId(peerId));
                            builder.isDirect(isDirectPeer);
                        });

        config
                .getWhitelistManager()
                .ifPresent(
                        mgr -> {
                            // Ip whitelisting
                            final Function1<? super String, Boolean> isIpWhitelisted = mgr::isWhitelisted;
                            builder.ipWhitelisted(isIpWhitelisted);
                        });

        return builder.build();
    }

    public static GossipTopicsScoreParams createTopicsScoreParams(final GossipScoringConfig config) {
        final GossipTopicScoreParams defaultTopicParams =
                createTopicScoreParams(config.getDefaultTopicScoringConfig());
        final Map<String, GossipTopicScoreParams> topicParams =
                config.getTopicScoringConfig().entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, e -> createTopicScoreParams(e.getValue())));
        return new GossipTopicsScoreParams(defaultTopicParams, topicParams);
    }

    public static GossipTopicScoreParams createTopicScoreParams(
            final GossipTopicScoringConfig config) {
        return GossipTopicScoreParams.builder()
                .topicWeight(config.getTopicWeight())
                .timeInMeshWeight(config.getTimeInMeshWeight())
                .timeInMeshQuantum(config.getTimeInMeshQuantum())
                .timeInMeshCap(config.getTimeInMeshCap())
                .firstMessageDeliveriesWeight(config.getFirstMessageDeliveriesWeight())
                .firstMessageDeliveriesDecay(config.getFirstMessageDeliveriesDecay())
                .firstMessageDeliveriesCap(config.getFirstMessageDeliveriesCap())
                .meshMessageDeliveriesWeight(config.getMeshMessageDeliveriesWeight())
                .meshMessageDeliveriesDecay(config.getMeshMessageDeliveriesDecay())
                .meshMessageDeliveriesThreshold(config.getMeshMessageDeliveriesThreshold())
                .meshMessageDeliveriesCap(config.getMeshMessageDeliveriesCap())
                .meshMessageDeliveriesActivation(config.getMeshMessageDeliveriesActivation())
                .meshMessageDeliveryWindow(config.getMeshMessageDeliveryWindow())
                .meshFailurePenaltyWeight(config.getMeshFailurePenaltyWeight())
                .meshFailurePenaltyDecay(config.getMeshFailurePenaltyDecay())
                .invalidMessageDeliveriesWeight(config.getInvalidMessageDeliveriesWeight())
                .invalidMessageDeliveriesDecay(config.getInvalidMessageDeliveriesDecay())
                .build();
    }
}
