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
//package io.xdag.rpc.modules.eth.subscribe;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import io.netty.channel.ChannelHandlerContext;
//import io.xdag.rpc.jsonrpc.JsonRpcResultOrError;
//import io.xdag.rpc.jsonrpc.JsonRpcVersion;
//import io.xdag.rpc.modules.XdagJsonRpcMethod;
//import io.xdag.rpc.modules.XdagJsonRpcRequest;
//import io.xdag.rpc.modules.XdagJsonRpcRequestVisitor;
//
//import java.util.Objects;
//
//public class EthUnsubscribeRequest extends XdagJsonRpcRequest {
//
//    private final EthUnsubscribeParams params;
//
//    @JsonCreator
//    public EthUnsubscribeRequest(
//            @JsonProperty("jsonrpc") JsonRpcVersion version,
//            @JsonProperty("method") XdagJsonRpcMethod method,
//            @JsonProperty("id") int id,
//            @JsonProperty("params") EthUnsubscribeParams params) {
//        super(version, verifyMethod(method), id);
//        this.params = params;
//    }
//
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    public EthUnsubscribeParams getParams() {
//        return params;
//    }
//
//    @Override
//    public JsonRpcResultOrError accept(XdagJsonRpcRequestVisitor visitor, ChannelHandlerContext ctx) {
//        return visitor.visit(this, ctx);
//    }
//
//    private static XdagJsonRpcMethod verifyMethod(XdagJsonRpcMethod method) {
//        if (method != XdagJsonRpcMethod.ETH_UNSUBSCRIBE) {
//            throw new IllegalArgumentException(
//                    "Wrong method mapped to eth_unsubscribe. Check JSON mapping configuration in JsonRpcRequest."
//            );
//        }
//
//        return method;
//    }
//
//}
