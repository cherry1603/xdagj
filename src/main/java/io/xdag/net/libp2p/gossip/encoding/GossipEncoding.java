package io.xdag.net.libp2p.gossip.encoding;

import io.xdag.net.libp2p.gossip.PreparedGossipMessage;
import org.apache.tuweni.bytes.Bytes;

/**
 * @Author: wawa
 * @Date: 2021/10/21/10:02 下午
 * @Description:
 */
public interface GossipEncoding {

    GossipEncoding SSZ_SNAPPY = new SszSnappyEncoding(new SnappyBlockCompressor());

    String getName();

    Bytes encode(String v);

    PreparedGossipMessage prepareMessage(Bytes data, String valueType);

    PreparedGossipMessage prepareUnknownMessage(Bytes data);


}
