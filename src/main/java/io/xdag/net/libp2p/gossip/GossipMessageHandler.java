package io.xdag.net.libp2p.gossip;

import com.google.common.util.concurrent.SettableFuture;
import io.xdag.Kernel;
import io.xdag.consensus.SyncManager;
import io.xdag.core.Block;
import io.xdag.core.BlockWrapper;
import io.xdag.core.Blockchain;
import io.xdag.core.XdagStats;
import io.xdag.net.message.AbstractMessage;
import io.xdag.net.message.Message;
import io.xdag.net.message.NetDB;
import io.xdag.net.message.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes;
import org.apache.tuweni.bytes.MutableBytes32;

import java.util.List;

import static io.xdag.utils.BasicUtils.hash2Address;

/**
 * @Author: wawa
 * @Date: 2021/11/01/10:40 下午
 * @Description:
 */
@Slf4j
public class GossipMessageHandler {
    Kernel kernel;
    private BlockGossipManager blockGossipManager;
    private Blockchain blockchain;

    public void setSyncMgr(SyncManager syncMgr) {
        this.syncMgr = syncMgr;
    }

    private SyncManager syncMgr;
    public GossipMessageHandler(Kernel kernel,BlockGossipManager blockGossipManager) {
        this.kernel = kernel;
        this.blockGossipManager = blockGossipManager;
        this.blockchain = kernel.getBlockchain();
        this.syncMgr = kernel.getSyncMgr();
    }

    public void respondMessage(Message msg){
        switch (msg.getCommand()) {
            case NEW_BLOCK:
                processNewBlock((NewBlockMessage) msg);
                break;
            case BLOCK_REQUEST:
                processBlockRequest((BlockRequestMessage) msg);
                break;
            case BLOCKS_REQUEST:
                processBlocksRequest((BlocksRequestMessage) msg);
                break;
            case BLOCKS_REPLY:
                processBlocksReply((BlocksReplyMessage) msg);
                break;
            case SUMS_REQUEST:
                processSumsRequest((SumRequestMessage) msg);
                break;
            case SUMS_REPLY:
                processSumsReply((SumReplyMessage) msg);
                break;
            case BLOCKEXT_REQUEST:
                processBlockExtRequest((BlockExtRequestMessage) msg);
                break;
            default:
                break;
        }
    }

    private void processBlockExtRequest(BlockExtRequestMessage msg) {

    }

    private void processSumsReply(SumReplyMessage msg) {
        updateXdagStats(msg);
        long randomSeq = msg.getRandom();
        SettableFuture<Bytes> sf = kernel.getSync().getSumsRequestMap().get(randomSeq);
        if (sf != null) {
            sf.set(msg.getSum());
        }
    }

    private void processSumsRequest(SumRequestMessage msg) {
        updateXdagStats(msg);
        MutableBytes sums = MutableBytes.create(256);
        kernel.getBlockStore().loadSum(msg.getStarttime(), msg.getEndtime(), sums);
        SumReplyMessage reply = new SumReplyMessage(msg.getEndtime(), msg.getRandom(),
                kernel.getBlockchain().getXdagStats(), sums,new NetDB());
        sendMessage(reply);
    }

    private void processBlocksReply(BlocksReplyMessage msg) {
        updateXdagStats(msg);
        long randomSeq = msg.getRandom();
        SettableFuture<Bytes> sf = kernel.getSync().getBlocksRequestMap().get(randomSeq);
        if (sf != null) {
            sf.set(Bytes.wrap(new byte[]{0}));
        }
    }

    private void processBlocksRequest(BlocksRequestMessage msg) {
        updateXdagStats(msg);
        long startTime = msg.getStarttime();
        long endTime = msg.getEndtime();
        long random = msg.getRandom();

        // TODO: paulochen 处理多区块请求
//        // 如果大于快照点的话 我可以发送
//        if (startTime > 1658318225407L) {
//            // TODO: 如果请求时间间隔过大，启动新线程发送，目的是避免攻击
        List<Block> blocks = blockchain.getBlocksByTime(startTime, endTime);
        System.out.println("blocks size = " + blocks.size());
        for (Block block : blocks) {
            sendNewBlock(block, 1);
        }
        BlocksReplyMessage blocksReplyMessage =  new BlocksReplyMessage(startTime,endTime,random,kernel.getBlockchain().getXdagStats(),new NetDB());
        sendMessage(blocksReplyMessage);
    }

    public void sendNewBlock(Block newBlock, int TTL) {
        NewBlockMessage msg = new NewBlockMessage(newBlock, TTL);

        sendMessage(msg);
    }

    private void processBlockRequest(BlockRequestMessage msg) {
        Bytes32 hash = msg.getHash();
//        hash = Arrays.reverse(hash);
//        System.arraycopy(hash, 8, find, 8, 24);
        MutableBytes32 find = MutableBytes32.create();
        find.set(8, hash.reverse().slice(8, 24));
        Block block = blockchain.getBlockByHash(find, true);
        System.out.println("syncblock:" + hash);
        if (block != null) {
//            log.debug("processBlockRequest: findBlock" + Hex.toHexString(block.getHashLow()));
            NewBlockMessage message = new NewBlockMessage(block, kernel.getConfig().getNodeSpec().getTTL());
            sendMessage(message);
        }
    }

    private void processNewBlock(NewBlockMessage msg) {
        Block block = msg.getBlock();
        System.out.println("processnewblock:" + hash2Address(Bytes32.fromHexString(block.getHash().toString())));
        log.debug("processNewBlock:{}", block.getHashLow().toHexString());
        BlockWrapper bw = new BlockWrapper(block, msg.getTtl() - 1);
        syncMgr.validateAndAddNewBlock(bw);
    }

    public long sendGetBlock(MutableBytes32 hash) {
        BlockRequestMessage msg = new BlockRequestMessage(hash,kernel.getBlockchain().getXdagStats(),new NetDB());
        sendMessage(msg);
        return msg.getRandom();
    }

    public long sendGetBlocks(long startTime, long endTime) {
        BlocksRequestMessage msg = new BlocksRequestMessage(startTime,endTime,kernel.getBlockchain().getXdagStats(),new NetDB());
        sendMessage(msg);
        return msg.getRandom();
    }

    public void sendMessage(Message message) {
        if(blockGossipManager.shutdown.get()){
            log.debug("blockGossipManager is shutdown");
        }else{
            blockGossipManager.gossipMessage(message);
        }

    }
    //不连接dnet网络
    public void updateXdagStats(AbstractMessage message) {
        XdagStats remoteXdagStats = message.getXdagStats();
        kernel.getBlockchain().getXdagStats().update(remoteXdagStats);
//        kernel.getNetDBMgr().updateNetDB(message.getNetDB());
    }

    public long sendGetSums(long startTime, long endTime) {
        SumRequestMessage msg = new SumRequestMessage(startTime,endTime,kernel.getBlockchain().getXdagStats(),new NetDB());
//        System.out.println("sendgetSums = "+ msg.getEncoded());
        sendMessage(msg);
        return msg.getRandom();
    }
}
