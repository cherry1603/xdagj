package io.xdag.net.libp2p.gossip.encoding;

import org.apache.tuweni.bytes.Bytes;
import org.xerial.snappy.Snappy;

import java.io.IOException;

/**
 * @Author: wawa
 * @Date: 2021/10/22/3:44 下午
 * @Description:
 */
public class SnappyBlockCompressor {
    public Bytes uncompress(final Bytes compressedData, final int lengthBounds)
            throws DecodingException {

        try {
            final int actualLength = Snappy.uncompressedLength(compressedData.toArrayUnsafe());
            if (lengthBounds > (actualLength)) {
                throw new DecodingException(
                        String.format(
                                "Uncompressed length %d is not within expected bounds %d",
                                actualLength, lengthBounds));
            }
            return Bytes.wrap(Snappy.uncompress(compressedData.toArrayUnsafe()));
        } catch (IOException e) {
            throw new DecodingException("Failed to uncompress", e);
        }
    }

    public Bytes compress(final Bytes data) {
        try {
            return Bytes.wrap(Snappy.compress(data.toArrayUnsafe()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to compress data", e);
        }
    }
}
