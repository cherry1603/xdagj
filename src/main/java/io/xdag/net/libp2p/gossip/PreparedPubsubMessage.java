package io.xdag.net.libp2p.gossip;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.libp2p.etc.types.WBytes;
import io.libp2p.pubsub.AbstractPubsubMessage;
import org.jetbrains.annotations.NotNull;
import pubsub.pb.Rpc;

public class PreparedPubsubMessage extends AbstractPubsubMessage {

    private final Rpc.Message protobufMessage;
    private final PreparedGossipMessage preparedMessage;
    private final Supplier<WBytes> cachedMessageId;

    public PreparedPubsubMessage(Rpc.Message protobufMessage, PreparedGossipMessage preparedMessage) {
        this.protobufMessage = protobufMessage;
        this.preparedMessage = preparedMessage;
        cachedMessageId =
                Suppliers.memoize(() -> new WBytes(preparedMessage.getMessageId().toArrayUnsafe()));
    }

    @NotNull
    @Override
    public WBytes getMessageId() {
        return cachedMessageId.get();
    }

    @NotNull
    @Override
    public Rpc.Message getProtobufMessage() {
        return protobufMessage;
    }

    public PreparedGossipMessage getPreparedMessage() {
        return preparedMessage;
    }
}
