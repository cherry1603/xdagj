package io.xdag.net.libp2p.gossip;


public class XdagGossipTopicFilter implements GossipTopicFilter{

    public XdagGossipTopicFilter() {

    }

    @Override
    public boolean isRelevantTopic(String topic) {
        return true;
    }

}
