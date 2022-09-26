package io.xdag.net.libp2p.gossip;

import io.libp2p.core.pubsub.ValidationResult;
import io.xdag.utils.SafeFuture;
import org.apache.tuweni.bytes.Bytes;

public interface TopicHandler {

    /**
     * Preprocess 'raw' Gossip message returning the instance which may calculate Gossip 'message-id'
     * and cache intermediate data for later message handling with {@link
     * #handleMessage(PreparedGossipMessage)}
     */
    PreparedGossipMessage prepareMessage(Bytes payload);

    /**
     * Validates and handles gossip message preprocessed earlier by {@link #prepareMessage(Bytes)}
     *
     * @param message The preprocessed gossip message
     * @return Message validation promise
     */
    SafeFuture<ValidationResult> handleMessage(PreparedGossipMessage message);
}