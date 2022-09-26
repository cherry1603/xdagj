package io.xdag.net.libp2p.gossip.encoding;

import io.xdag.net.libp2p.gossip.PreparedGossipMessage;
import org.apache.tuweni.bytes.Bytes;

/**
 * @Author: wawa
 * @Date: 2021/10/22/4:27 下午
 * @Description:
 */
class SszSnappyEncoding implements GossipEncoding {
    private static final String NAME = "ssz_snappy";
    private final SnappyBlockCompressor snappyCompressor;
    private final SszGossipCodec sszCodec = new SszGossipCodec();

    public SszSnappyEncoding(final SnappyBlockCompressor snappyCompressor) {
        this.snappyCompressor = snappyCompressor;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Bytes encode(final String value) {
        return snappyCompressor.compress(sszCodec.encode(value));
    }

    public String  decodeMessage(PreparedGossipMessage message, String valueType)
            throws DecodingException {
        if (!(message instanceof SnappyPreparedGossipMessage)) {
            throw new DecodingException("Unexpected PreparedMessage subclass: " + message.getClass());
        }
        SnappyPreparedGossipMessage lazyMessage = (SnappyPreparedGossipMessage) message;
        return sszCodec.decode(lazyMessage.getUncompressedOrThrow(), valueType.length());
    }

    @Override
    public PreparedGossipMessage prepareMessage(
            Bytes data, String valueType) {
        return SnappyPreparedGossipMessage.create(data, valueType, snappyCompressor);
    }

    @Override
    public PreparedGossipMessage prepareUnknownMessage(Bytes data) {
        return SnappyPreparedGossipMessage.createUnknown(data);
    }
}
