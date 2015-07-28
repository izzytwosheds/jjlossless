/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import java.io.IOException;

public class BitInputStream {
    private JpegInputStream inputStream;
    private int bitPos;
    private int lastByte;

    BitInputStream(JpegInputStream inputStream) {
        this.inputStream = inputStream;
        bitPos = -1;
        lastByte = 0;
    }

    int getBit() throws IOException {
        if (bitPos < 0) {
            // before reading the next byte, make sure it is not a marker thus can be used as ECS
            if (inputStream.isAtMarker()) {
                // oops, next byte is a marker, cannot use, so return an error
                return -1;
            }

            if (lastByte == 0xFF) {
                // since we are not at the marker, next byte is a stuffing byte, so skip it
                inputStream.skip(1);
            }

            lastByte = inputStream.read();
            bitPos = 7;
        }

        int ret = (lastByte >> bitPos) & 0x01;
        bitPos--;

        return ret;
    }

    int getValue(int numBits) throws IOException {
        if (numBits == 0) {
            return 0;
        }

        int ret = 0;
        int sign = 1;

        // read the first bit
        int bit = getBit();

        // if we are at the marker, simply return zero
        if (bit < 0) {
            return 0;
        }

        if (bit == 0) {
            // if first bit is zero, the value is negative
            sign = -1;
        } else {
            // first bit is one, value is positive, first bit is used
            ret |= bit << (numBits - 1);
        }

        // read remaining bits and "assemble" the value
        for (int i = numBits-2; i >= 0; i--) {
            bit = getBit();
            ret |= bit << i;
        }

        // adjust the negative value
        if (sign < 0) {
            ret += (-1 << numBits) + 1;
        }

        return ret;
    }
}
