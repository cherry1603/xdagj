package io.xdag.net.libp2p.gossip;

import org.apache.tuweni.bytes.Bytes;

/**
 * Semi-processed raw gossip message which can supply Gossip 'message-id'
 *
 * @see TopicHandler#prepareMessage(Bytes)
 */
public interface PreparedGossipMessage {

    /**
     * Returns the Gossip 'message-id' If the 'message-id' calculation is resource consuming operation
     * is should performed lazily by implementation class
     */
    Bytes getMessageId();
}
