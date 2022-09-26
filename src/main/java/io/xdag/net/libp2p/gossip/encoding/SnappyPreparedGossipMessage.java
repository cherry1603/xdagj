package io.xdag.net.libp2p.gossip.encoding;


import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.xdag.net.libp2p.gossip.PreparedGossipMessage;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.Hash;

import java.util.Optional;

public class SnappyPreparedGossipMessage implements PreparedGossipMessage {
    public static final Bytes MESSAGE_DOMAIN_INVALID_SNAPPY = Bytes.fromHexString("0x00000000");
    public static final Bytes MESSAGE_DOMAIN_VALID_SNAPPY = Bytes.fromHexString("0x01000000");

    private final Bytes compressedData;
    private final String valueType;
    private SnappyBlockCompressor snappyCompressor;
    private final Supplier<Optional<Bytes>> uncompressed =
            Suppliers.memoize(this::maybeUncompressPayload);

    public static SnappyPreparedGossipMessage createUnknown(Bytes compressedData) {
        return new SnappyPreparedGossipMessage(compressedData, null, null);
    }

    static SnappyPreparedGossipMessage create(
            Bytes compressedData, String valueType, SnappyBlockCompressor snappyCompressor) {
        return new SnappyPreparedGossipMessage(compressedData, valueType, snappyCompressor);
    }

    private SnappyPreparedGossipMessage(
            Bytes compressedData, String valueType, SnappyBlockCompressor snappyCompressor) {
        this.compressedData = compressedData;
        this.valueType = valueType;
        this.snappyCompressor = snappyCompressor;
    }

    public Bytes getUncompressedOrThrow() throws DecodingException {
        return getMaybeUncompressed()
                .orElseThrow(
                        () -> new DecodingException("Couldn't uncompress the message"));
    }

    public Optional<Bytes> getMaybeUncompressed() {
        return uncompressed.get();
    }


    private Optional<Bytes> maybeUncompressPayload() {
        try {
            if (valueType == null) {
                return Optional.empty();
            } else {
                return Optional.of(uncompressPayload());
            }
        } catch (DecodingException e) {
            return Optional.empty();
        }
    }

    private Bytes uncompressPayload() throws DecodingException {
        return snappyCompressor.uncompress(compressedData, valueType.length());
    }


    @Override
    public Bytes getMessageId() {
        return Hash.sha2_256(
                getMaybeUncompressed()
                        .map(
                                validSnappyUncompressed ->
                                        Bytes.wrap(MESSAGE_DOMAIN_VALID_SNAPPY, validSnappyUncompressed))
                        .orElse(Bytes.wrap(MESSAGE_DOMAIN_INVALID_SNAPPY, compressedData)))
                .slice(0, 20);
    }
}
