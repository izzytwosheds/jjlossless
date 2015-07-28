/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;


public class BitInputStreamTest {
    private BitInputStream prepareInputStream(byte[] buffer) {
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
        JpegInputStream jpegInputStream = new JpegInputStream(bis);
        return new BitInputStream(jpegInputStream);
    }

    @Test
    public void readBitTest1() {
        byte[] buffer = new byte[] {0x01};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int[] referenceArray = new int[] {0, 0, 0, 0, 0, 0, 0, 1};
        int[] array = new int[8];

        try {
            for (int i=0; i<8; i++) {
                array[i] = bitInputStream.getBit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(referenceArray, array);
    }

    @Test
    public void readBitTest2() {
        byte[] buffer = new byte[] {0x02};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int[] referenceArray = new int[] {0, 0, 0, 0, 0, 0, 1, 0};
        int[] array = new int[8];

        try {
            for (int i=0; i<8; i++) {
                array[i] = bitInputStream.getBit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(referenceArray, array);
    }

    @Test
    public void readBitTestMultiByte() {
        byte[] buffer = new byte[] {0x02, 0x7F};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int[] referenceArray = new int[] {0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1};
        int[] array = new int[11];

        try {
            for (int i=0; i<11; i++) {
                array[i] = bitInputStream.getBit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(referenceArray, array);
    }

    @Test
    public void readBitTestStuffedByte() {
        byte[] buffer = new byte[] {(byte) 0xFF, 0x00, 0x7F};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int[] referenceArray = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1};
        int[] array = new int[11];

        try {
            for (int i=0; i<11; i++) {
                array[i] = bitInputStream.getBit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(referenceArray, array);
    }

    @Test
    public void readValue1() {
        byte[] buffer = new byte[] {(byte) 0xF0};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int referenceValue = 1;
        int value = -1;

        try {
            value = bitInputStream.getValue(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(referenceValue, value);
    }

    @Test
    public void readValue3() {
        byte[] buffer = new byte[] {(byte) 0xF0};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int referenceValue = 3;
        int value = -1;

        try {
            value = bitInputStream.getValue(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(referenceValue, value);
    }

    @Test
    public void readValues3and12() {
        byte[] buffer = new byte[] {(byte) 0xF0};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int[] referenceValues = new int[] {3, 12};
        int[] values = new int[2];

        try {
            values[0] = bitInputStream.getValue(2);
            values[1] = bitInputStream.getValue(4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(referenceValues, values);
    }

    @Test
    public void readValuesAcrossBytes() {
        byte[] buffer = new byte[] {(byte) 0xF2, 0x01};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int[] referenceValues = new int[] {3, 12, 32};
        int[] values = new int[3];

        try {
            values[0] = bitInputStream.getValue(2);
            values[1] = bitInputStream.getValue(4);
            values[2] = bitInputStream.getValue(6);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(referenceValues, values);
    }

    @Test
    public void readValuesAcrossBytesStuffingZero() {
        byte[] buffer = new byte[] {(byte) 0xFF, 0x00, 0x01};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int[] referenceValues = new int[] {3, 31, 32};
        int[] values = new int[3];

        try {
            values[0] = bitInputStream.getValue(2);
            values[1] = bitInputStream.getValue(5);
            values[2] = bitInputStream.getValue(6);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertArrayEquals(referenceValues, values);
    }

    @Test
    public void readValueNegative1() {
        byte[] buffer = new byte[] {0x00};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int referenceValue = -1;
        int value = -1;

        try {
            value = bitInputStream.getValue(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(referenceValue, value);
    }

    @Test
    public void readValueNegative2() {
        byte[] buffer = new byte[] {0x70};
        BitInputStream bitInputStream = prepareInputStream(buffer);

        int referenceValue = -2;
        int value = -1;

        try {
            value = bitInputStream.getValue(2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(referenceValue, value);
    }
}