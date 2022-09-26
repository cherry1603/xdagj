package io.xdag.net.libp2p.gossip.encoding;

/**
 * @Author: wawa
 * @Date: 2021/10/22/3:40 下午
 * @Description:
 */
public class DecodingException extends Exception{
    public DecodingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DecodingException(final String message) {
        super(message);
    }
}
