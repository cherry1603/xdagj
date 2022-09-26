package io.xdag.net.libp2p.gossip;

import io.libp2p.core.pubsub.ValidationResult;
import io.xdag.net.libp2p.gossip.encoding.GossipEncoding;
import io.xdag.utils.SafeFuture;
import org.apache.tuweni.bytes.Bytes;

/**
 * wawa
 * 2021.10.18
 */

public class XdagTopicHandler implements TopicHandler{
    private String topicname = "XDAG";
    private GossipEncoding gossipEncoding;


    public XdagTopicHandler(GossipEncoding gossipEncoding) {
        this.gossipEncoding = gossipEncoding;
    }

    @Override
    public PreparedGossipMessage prepareMessage(Bytes payload) {
        return getGossipEncoding().prepareMessage(payload, "1");
    }


    @Override
    public SafeFuture<ValidationResult> handleMessage(PreparedGossipMessage message) {
        //todo
//        System.out.println("handleMessage");
        return null;
    }

    public GossipEncoding getGossipEncoding() {
        return gossipEncoding;
    }

    public String getTopic() {
        return topicname;
    }
}
