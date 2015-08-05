/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import java.io.IOException;
import java.util.ArrayList;

import static com.twosheds.jjlossless.Marker.*;

public class Frame {
    int samplePrecision;
    int numberOfLines;
    int numberOfSamples;
    int numberOfImageComponents;
    Component[] components;
    ArrayList<HuffmanTree> huffmanTrees;

    ArrayList<Scan> scans;

    public static class Component {
        int index;
        int id;
        int horizontalSamplingFactor;
        int verticalSamplingFactor;
        int dcTable;

        Component(int index, int id, int horizontalSamplingFactor, int verticalSamplingFactor, int dcTable) {
            this.index = index;
            this.id = id;
            this.horizontalSamplingFactor = horizontalSamplingFactor;
            this.verticalSamplingFactor = verticalSamplingFactor;
            this.dcTable = dcTable;
        }
    }

    Frame() {
        huffmanTrees = new ArrayList<HuffmanTree>(4);
        scans = new ArrayList<Scan>(1);
    }

    boolean read(JpegInputStream inputStream) {
        try {
            int currentMarker;
            do {
                currentMarker = inputStream.skipToNextMarker();
                switch (currentMarker) {
                    case DHT:
                        huffmanTrees = readHuffmanTables(inputStream);
                        break;
                    case SOF3:
                        readFrameHeader(inputStream);
                        break;
                    case SOS:
                        Scan scan = new Scan(this, huffmanTrees);
                        if (scan.read(inputStream)) {
                            scans.add(scan);
                        }
                        break;
                    case APP0:
                    case APP15:
                        // don't do anything, just suppress the error message
                        currentMarker = inputStream.readCurrentMarker();
                        break;
                    case EOI:
                        // end of file, done
                        break;
                    default:
                        currentMarker = inputStream.readCurrentMarker();
                        System.out.println("Bad marker when reading a frame: " + currentMarker);
                        break;
                }
            } while (currentMarker != EOI);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private ArrayList<HuffmanTree> readHuffmanTables(JpegInputStream inputStream) throws IOException {
        int marker = inputStream.readCurrentMarker();
        int length = inputStream.readUnsignedShort();
        length -=2;
        ArrayList<HuffmanTree> trees = new ArrayList<HuffmanTree>(4);

        while (length > 0) {
            int index = inputStream.read();
            int tableClass = (index >> 4) & 0xF;
            int destinationId = index & 0xF;

            byte[] bits = new byte[16];
            int bytesRead = inputStream.read(bits);
            if (bytesRead < 16) {
                System.out.println("Error reading Huffman table bits, expected 16 bytes, read " + bytesRead);
                return null;
            }

            int count = 0;
            for (byte c: bits) {
                count += c;
            }

            if (count > 256) {
                System.out.println("Bad Huffman table count: " + count);
                return null;
            }

            byte[] values = new byte[count];
            bytesRead = inputStream.read(values);
            if (bytesRead < count) {
                System.out.println("Error reading Huffman table values, expected " + count + " bytes, read " + bytesRead);
                return null;
            }

            length -= 1 + 16 + count;

            if (tableClass > 0) {
                System.out.println("DC is not defined");
            } else {
                if (destinationId < 0 || destinationId > 3) {
                    System.out.println("Bad Huffman table destination id: " + destinationId);
                    return null;
                }
                HuffmanTree huffmanTree = new HuffmanTree(tableClass, destinationId, bits, values);
                trees.add(huffmanTree);
            }
        }

        return trees;
    }

    private boolean readFrameHeader(JpegInputStream inputStream) throws IOException {
        int marker = inputStream.readCurrentMarker();
        int length = inputStream.readUnsignedShort();
        samplePrecision = inputStream.read();
        numberOfLines = inputStream.readUnsignedShort();
        numberOfSamples = inputStream.readUnsignedShort();
        numberOfImageComponents = inputStream.read();

        if (numberOfLines <=0 || numberOfSamples <= 0 || numberOfImageComponents <= 0) {
            System.out.println("Bad image parameters");
            return false;
        }

        if (numberOfImageComponents != 1) {
            System.out.println("This version supports only grayscale images");
            return false;
        }

        if (length != numberOfImageComponents * 3 + 8) {
            System.out.println("Bad length");
            return false;
        }

        components = new Component[numberOfImageComponents];
        for (int c = 0; c < numberOfImageComponents; c++) {
            int id = inputStream.read();

            int samplingFactor = inputStream.read();
            int horizontalSamplingFactor = (samplingFactor >> 4) & 0x0F;
            int verticalSamplingFactor = samplingFactor & 0x0F;
            int dcTable = inputStream.read();

            components[c] = new Frame.Component(c, id, horizontalSamplingFactor, verticalSamplingFactor, dcTable);
        }

        return true;
    }
}
