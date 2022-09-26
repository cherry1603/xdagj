package io.xdag.net.libp2p.gossip;

@FunctionalInterface
public interface GossipTopicFilter {
    boolean isRelevantTopic(String topic);
}
