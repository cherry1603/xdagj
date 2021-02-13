/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.xdag.core;

import io.xdag.libp2p.Libp2pNode;
import io.xdag.net.node.Node;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockWrapper implements Cloneable {
    private Block block;
    private int ttl;
    /** 记录区块接收节点 */
    private Node remoteNode;
    private Libp2pNode libp2pNode;
    // NO_PARENT waiting time
    private long time;

    public BlockWrapper(Block block, int ttl, Node remoteNode) {
        this.block = block;
        this.ttl = ttl;
        this.remoteNode = remoteNode;
    }

    public BlockWrapper(Block block, int ttl) {
        this.block = block;
        this.ttl = ttl;
    }

    public BlockWrapper(Block block, int ttl, Libp2pNode libp2pNode) {
        this.block = block;
        this.ttl = ttl;
        this.libp2pNode = libp2pNode;
    }
}
