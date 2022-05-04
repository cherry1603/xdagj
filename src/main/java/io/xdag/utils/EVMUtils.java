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
package io.xdag.utils;

import io.xdag.core.XAmount;
import io.xdag.core.XUnit;
import io.xdag.evm.client.Repository;
import org.apache.tuweni.bytes.Bytes;

import java.math.BigInteger;

public class EVMUtils {

    private static final BigInteger TEN_POW_NINE = BigInteger.TEN.pow(9);

    /**
     * Returns number of VM words required to hold data of size {@code size}
     */
    public static long getSizeInWords(long size) {
        return size == 0 ? 0 : (size - 1) / 32 + 1;
    }

    public static void transfer(Repository repository, Bytes fromAddr, Bytes toAddr, BigInteger value) {
        repository.addBalance(fromAddr, value.negate());
        repository.addBalance(toAddr, value);
    }

    public static XAmount weiToXAmount(BigInteger value) {
        BigInteger nanoXDAG = value.divide(TEN_POW_NINE);
        return XAmount.of(nanoXDAG.longValue(), XUnit.NANO_XDAG);
    }

    public static BigInteger xAmountToWei(XAmount value) {
        return value.toBigInteger().multiply(TEN_POW_NINE);
    }

    public static BigInteger xAmountToWei(long nanoXDAG) {
        return BigInteger.valueOf(nanoXDAG).multiply(TEN_POW_NINE);
    }

}