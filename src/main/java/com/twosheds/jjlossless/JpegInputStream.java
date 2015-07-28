/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JpegInputStream extends BufferedInputStream {
    private static byte[] buffer = new byte[2];

    public JpegInputStream(InputStream in) {
        super(in);
    }

    int readCurrentMarker() throws IOException {
        int firstByte = read();
        int secondByte = read();
        if (firstByte == 0xFF && secondByte != 0x00) {
            return secondByte;
        }

        return -1;
    }

    int skipToNextMarker() throws IOException {
        while (!isAtMarker()) {
            skip(1);
        }

        mark(2);
        int firstByte = read();
        int secondByte = read();
        reset();

        return secondByte;
    }

    boolean isAtMarker() throws IOException {
        mark(2);
        int firstByte = read();
        int secondByte = read();
        reset();

        return (firstByte == 0xFF && secondByte != 0x00);
    }

    int readUnsignedShort() throws IOException {
        int bytesRead = read(buffer);
        if (bytesRead < 2) {
            return -1;
        }

        return ((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF);
    }
}
