package io.xdag.net.libp2p.gossip;

import org.apache.tuweni.bytes.Bytes;

public interface TopicChannel {
    void gossip(Bytes data);

    void close();
}
