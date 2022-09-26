package io.xdag.net.libp2p.gossip.encoding;

import org.apache.tuweni.bytes.Bytes;

import java.nio.charset.StandardCharsets;

/**
 * @Author: wawa
 * @Date: 2021/10/22/3:48 下午
 * @Description:
 */
public class SszGossipCodec {
    public Bytes encode(final String value) {
        return Bytes.wrap(value.getBytes(StandardCharsets.UTF_8));
    }

    public String decode(final Bytes data, final int valueTypelength)
            throws DecodingException {
        try {
            if (valueTypelength<data.size()) {
                throw new DecodingException(
                        "Uncompressed length " + data.size() + " is not within expected bounds");
            }
            final String result = data.toHexString();
            if (result == null) {
                throw new DecodingException("Unable to decode value");
            }
            return result;
        } catch (Exception e) {
            throw new DecodingException("Encountered exception while deserializing value", e);
        }
    }
}
