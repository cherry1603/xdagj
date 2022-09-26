package io.xdag.net.libp2p.gossip;

import io.libp2p.core.pubsub.MessageApi;
import io.libp2p.core.pubsub.PubsubPublisherApi;
import io.libp2p.core.pubsub.Topic;
import io.libp2p.core.pubsub.ValidationResult;
import io.libp2p.pubsub.PubsubMessage;
import io.netty.buffer.Unpooled;
import io.xdag.core.XdagBlock;
import io.xdag.core.XdagField;
import io.xdag.crypto.jni.Native;
import io.xdag.net.message.Message;
import io.xdag.net.message.MessageFactory;
import io.xdag.net.message.impl.NewBlockMessage;
import io.xdag.net.message.impl.Xdag03MessageFactory;
import io.xdag.utils.BytesUtils;
import io.xdag.utils.SafeFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.xdag.core.XdagField.FieldType.XDAG_FIELD_HEAD;
import static io.xdag.core.XdagField.FieldType.XDAG_FIELD_HEAD_TEST;
import static io.xdag.utils.BasicUtils.crc32Verify;
@Slf4j
public class GossipHandler implements Function<MessageApi, CompletableFuture<ValidationResult>> {
    private static final Logger LOG = LogManager.getLogger();

    private static final SafeFuture<ValidationResult> VALIDATION_FAILED =
            SafeFuture.completedFuture(ValidationResult.Invalid);
    private static final SafeFuture<ValidationResult> VALIDATION_IGNORED =
            SafeFuture.completedFuture(ValidationResult.Ignore);

    public static final int GOSSIP_MAX_SIZE = 1048576;
    private final Topic topic;
    private final PubsubPublisherApi publisher;
    private final TopicHandler handler;
    private final Set<Bytes> processedMessages = new HashSet<>();
    private MessageFactory messageFactory;
    private GossipMessageHandler gossipMessageHandler;

    public GossipHandler(
            final Topic topic,
            final PubsubPublisherApi publisher,
            final TopicHandler handler) {
        this.topic = topic;
        this.publisher = publisher;
        this.handler = handler;
        this.messageFactory = new Xdag03MessageFactory();
    }

    public void gossip(Bytes bytes) {
//        if (!processedMessages.add(bytes)) {
//            // We've already gossiped this data
//            return;
//        }

        SafeFuture.of(publisher.publish(Unpooled.wrappedBuffer(bytes.toArrayUnsafe()), topic))
                .finish(
                        () -> log.info("Successfully gossiped message on {}", topic),
                        err -> log.info("Failed to gossip message on " + topic + err));
    }

    //receive message
    @Override
    public SafeFuture<ValidationResult> apply(final MessageApi message) {
        final int messageSize = message.getData().readableBytes();
        if (messageSize > GOSSIP_MAX_SIZE) {
            LOG.trace(
                    "Rejecting gossip message of length {} which exceeds maximum size of {}",
                    messageSize,
                    GOSSIP_MAX_SIZE);
            return VALIDATION_FAILED;
        }
        byte[] arr = new byte[message.getData().readableBytes()];
        message.getData().slice().readBytes(arr);
        Bytes bytes = Bytes.wrap(arr);
//        System.out.println("byte = "+bytes);
//        System.out.println(arr.length);
        //todo:多余的
//        byte[] encryptedBytes;
//        encryptedBytes = Native.dfslib_encrypt_byte_sector(arr,arr.length,5);
        byte[] unCryptData = Native.dfslib_uncrypt_byte_sector(arr, arr.length,
                1);
        Message msg = decodeReceiveMessage(unCryptData);
        if(msg!=null){
            if(gossipMessageHandler==null){
//                System.out.println(111);
                return null;
            }
            gossipMessageHandler.respondMessage(msg);
        }

        if (!processedMessages.add(bytes)) {
            // We've already seen this message, skip processing
            LOG.trace("Ignoring duplicate message for topic {}: {} bytes", topic, bytes.size());
            return VALIDATION_IGNORED;
        }
        LOG.trace("Received message for topic {}: {} bytes", topic, bytes.size());

        PubsubMessage pubsubMessage = message.getOriginalMessage();
        if (!(pubsubMessage instanceof PreparedPubsubMessage gossipPubsubMessage)) {
            throw new IllegalArgumentException(
                    "Don't know this PubsubMessage implementation: " + pubsubMessage.getClass());
        }
        return handler.handleMessage(gossipPubsubMessage.getPreparedMessage());
    }



    public Message decodeReceiveMessage(byte[] unCryptData){
        int ttl = (int) ((BytesUtils.bytesToLong(unCryptData, 0, true) >> 8) & 0xff);
        if (isDataIllegal(unCryptData.clone())) {
            System.out.println("Receive error block!");
            log.debug("Receive error block!");
            return null;
        }
        System.arraycopy(BytesUtils.longToBytes(0, true), 0, unCryptData, 0, 8);

        XdagBlock xdagBlock = new XdagBlock(unCryptData);
        byte first_field_type = getMsgCode(xdagBlock, 0);
        Message msg = null;
        // 普通区块

        if(first_field_type==XDAG_FIELD_HEAD.asByte() ||first_field_type== XDAG_FIELD_HEAD_TEST.asByte()){
            msg = new NewBlockMessage(xdagBlock, ttl);
        }

        // 消息区块
        else if (XdagField.FieldType.XDAG_FIELD_NONCE.asByte() == first_field_type) {
            msg = messageFactory.create(getMsgCode(xdagBlock, 1), xdagBlock.getData());
        }
        return msg;
    }

    public boolean isDataIllegal(byte[] uncryptData) {
        long transportHeader = BytesUtils.bytesToLong(uncryptData, 0, true);
        long dataLength = (transportHeader >> 16 & 0xffff);
        int crc = BytesUtils.bytesToInt(uncryptData, 4, true);
        // clean transport header
        System.arraycopy(BytesUtils.longToBytes(0, true), 0, uncryptData, 4, 4);
        return (dataLength != 512 || !crc32Verify(uncryptData, crc));

    }

    public static byte getMsgCode(XdagBlock xdagblock, int n) {
        Bytes data = xdagblock.getData();
//        long type = BytesUtils.bytesToLong(data, 8, true);
        long type = data.getLong(8, ByteOrder.LITTLE_ENDIAN);
        return (byte) (type >> (n << 2) & 0xf);
    }

    public void setGossipMessageHandler(GossipMessageHandler gossipMessageHandler){
        this.gossipMessageHandler = gossipMessageHandler;
    }
}
