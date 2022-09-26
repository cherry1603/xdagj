package io.xdag.net.libp2p.gossip;

import io.libp2p.core.pubsub.PubsubSubscription;
import org.apache.tuweni.bytes.Bytes;

import java.util.concurrent.atomic.AtomicBoolean;

public class LibP2PTopicChannel implements TopicChannel {
    private final GossipHandler topicHandler;
    private final PubsubSubscription subscription;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public LibP2PTopicChannel(
            final GossipHandler topicHandler, final PubsubSubscription subscription) {
        this.topicHandler = topicHandler;
        this.subscription = subscription;
    }

    @Override
    public void gossip(final Bytes data) {
        if (closed.get()) {
            return;
        }
        topicHandler.gossip(data);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            subscription.unsubscribe();
        }
    }
}
