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

package io.xdag.net;

import io.xdag.config.Config;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import io.xdag.core.XdagLifecycle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the network database including whitelisted nodes
 */
@Getter
@Slf4j
public class NetDBManager implements XdagLifecycle {
    private final String database;
    private final String databaseWhite;
    private final NetDB whiteDB;
    private final Config config;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Constructor initializes database paths and network DBs
     */
    public NetDBManager(Config config) {
        this.config = config;
        database = config.getNodeSpec().getNetDBDir();
        databaseWhite = config.getNodeSpec().getWhiteListDir();
        whiteDB = new NetDB();
    }

    /**
     * Load whitelist IPs from config
     */
    public void loadFromConfig() {
        for (InetSocketAddress address:config.getNodeSpec().getWhiteIPList()){
            whiteDB.addNewIP(address);
        }
    }

    /**
     * Initialize network database
     */
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            loadFromConfig();
        }
    }

    @Override
    public void stop() {
        running.compareAndSet(true, false);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Check if address is whitelisted
     */
    public boolean canAccept(InetSocketAddress address) {
        return whiteDB.contains(address);
    }

}
