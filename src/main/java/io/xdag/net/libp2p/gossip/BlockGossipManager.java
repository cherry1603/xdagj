package io.xdag.net.libp2p.gossip;

import io.xdag.core.Block;
import io.xdag.core.XdagBlock;
import io.xdag.crypto.jni.Native;
import io.xdag.net.message.Message;
import io.xdag.net.message.impl.NewBlockMessage;
import org.apache.tuweni.bytes.Bytes;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;


public class BlockGossipManager implements GossipManager{
    //gossip的Inbound和Outbound
    //todo:因为无法获得对称的Inbound和Outbound，直接广播原始数据，不进行移位
    private TopicChannel channel;

    public BlockGossipManager(TopicChannel channel) {
        this.channel = channel;
    }



    public final  AtomicBoolean shutdown = new AtomicBoolean(false);


    public void gossipBlock(Block block){
        NewBlockMessage newBlockMessage = new NewBlockMessage(block,2);
        System.out.println("gossioblock:" + block.getHash());
        gossipMessage(newBlockMessage);
//        byte[] unCryptData = newBlockMessage.getEncoded().toArray();
//        byte[] encryptData;
//        encryptData = Native.dfslib_encrypt_byte_sector(unCryptData, unCryptData.length,
//                5);
//        channel.gossip(Bytes.wrap(encryptData));
    }

    public void gossipString(String s){
        channel.gossip(Bytes.wrap(s.getBytes(StandardCharsets.UTF_8)));
    }

    public void gossipMessage(Message message){
        XdagBlock block = new XdagBlock(message.getEncoded().toArray());
        byte[] unCryptData = block.getData().toArray();
//        System.out.println("unCryptData = "+Bytes.wrap(unCryptData));
        byte[] encryptData;
        encryptData = Native.dfslib_encrypt_byte_sector(unCryptData, unCryptData.length,
                1);
//        System.out.println("gossipMessage =" + Bytes.wrap(encryptData));
        channel.gossip(Bytes.wrap(encryptData));
    }


    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            // Close gossip channels
            channel.close();
        }
    }
}
