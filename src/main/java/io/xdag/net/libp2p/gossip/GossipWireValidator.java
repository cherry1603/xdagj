package io.xdag.net.libp2p.gossip;

import io.libp2p.pubsub.PubsubMessage;
import io.libp2p.pubsub.PubsubRouterMessageValidator;
import org.jetbrains.annotations.NotNull;
import pubsub.pb.Rpc;

public class GossipWireValidator implements PubsubRouterMessageValidator {

    public static class InvalidGossipMessageException extends IllegalArgumentException {
        public InvalidGossipMessageException(String s) {
            super(s);
        }
    }

    @Override
    public void validate(@NotNull PubsubMessage pubsubMessage) {
        Rpc.Message message = pubsubMessage.getProtobufMessage();
        if (message.hasFrom()) {
            throw new InvalidGossipMessageException("The message has prohibited 'from' field: ");
        }
        if (message.hasSignature()) {
            throw new InvalidGossipMessageException("The message has prohibited 'signature' field");
        }
        if (message.hasSeqno()) {
            throw new InvalidGossipMessageException("The message has prohibited 'seqno' field");
        }
    }
}
