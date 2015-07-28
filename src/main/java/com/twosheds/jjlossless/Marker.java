/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

public class Marker {
    static final int SOF0 = 0xC0;
    static final int SOF1 = 0xC1;
    static final int SOF2 = 0xC2;
    static final int SOF3 = 0xC3;

    static final int SOF5 = 0xC5;
    static final int SOF6 = 0xC6;
    static final int SOF7 = 0xC7;

    static final int JPG = 0xC8;
    static final int SOF9 = 0xC9;
    static final int SOF10 = 0xCA;
    static final int SOF11 = 0xCB;

    static final int SOF13 = 0xCD;
    static final int SOF14 = 0xCE;
    static final int SOF15 = 0xCF;

    static final int DHT = 0xC4;

    static final int DAC = 0xCC;

    static final int RST0 = 0xD0;
    static final int RST1 = 0xD1;
    static final int RST2 = 0xD2;
    static final int RST3 = 0xD3;
    static final int RST4 = 0xD4;
    static final int RST5 = 0xD5;
    static final int RST6 = 0xD6;
    static final int RST7 = 0xD7;

    static final int SOI = 0xD8;
    static final int EOI = 0xD9;
    static final int SOS = 0xDA;
    static final int DQT = 0xDB;
    static final int DNL = 0xDC;
    static final int DRI = 0xDD;
    static final int DHP = 0xDE;
    static final int EXP = 0xDF;

    static final int APP0 = 0xE0;
    static final int APP15 = 0xEF;

    static final int JPG0 = 0xF0;
    static final int JPG13 = 0xFD;
    static final int COM = 0xFE;

    static final int TEM = 0x01;

    static final int ERROR = 0x100;

    static boolean isFrameMarker(int marker) {
        return marker == SOF0
                || marker == SOF1
                || marker == SOF2
                || marker == SOF3
                || marker == SOF5
                || marker == SOF6
                || marker == SOF7
                || marker == SOF9
                || marker == SOF10
                || marker == SOF11
                || marker == SOF13
                || marker == SOF14
                || marker == SOF15;
    }
}
