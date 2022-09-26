package io.xdag.net.libp2p.gossip;

import org.apache.tuweni.bytes.Bytes;

/** Factory for {@link PreparedGossipMessage} instances */
public interface PreparedGossipMessageFactory {

    /** Creates a {@link PreparedGossipMessage} instance */
    PreparedGossipMessage create(String topic, Bytes payload);
}
