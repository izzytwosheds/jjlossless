/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.twosheds.jjlossless.Marker.*;

public class Scan {
    private Frame frame;
    private List<HuffmanTree> frameHuffmanTrees;
    private ArrayList<HuffmanTree> huffmanTrees;

    private int[] componentIndices;
    private int[] componentDcTables;
    private int predictor;
    private int pointTransform;
    private byte[] buffer;

    Scan(Frame frame, List<HuffmanTree> frameHuffmanTrees) {
        this.frame = frame;
        this.frameHuffmanTrees = frameHuffmanTrees;
        huffmanTrees = new ArrayList<HuffmanTree>(4);
    }

    boolean read(JpegInputStream inputStream) throws IOException {
        try {
            int currentMarker;
            do {
                currentMarker = inputStream.skipToNextMarker();
                switch (currentMarker) {
                    case DHT:
                        huffmanTrees = readHuffmanTables(inputStream);
                        break;
                    case SOS:
                        if (!readScan(inputStream)) {
                            return false;
                        }
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

    private boolean readScan(JpegInputStream inputStream) throws IOException {
        int currentMarker = inputStream.readCurrentMarker();

        int length = inputStream.readUnsignedShort();
        length -= 2;

        int numComponents = inputStream.read();
        length--;

        if (numComponents < 0 || numComponents > 4 || length != (numComponents * 2 + 3)) {
            System.out.println("Bad scan length: " + length + " or number of components: " + numComponents);
            return false;
        }

        // TODO resolve indices to references and store references
        componentIndices = new int[numComponents];
        componentDcTables = new int[numComponents];

        for (int i=0; i<numComponents; i++) {
            int componentIndex = inputStream.read();
            int params = inputStream.read();
            length -= 2;

            componentIndices[i] = componentIndex;
            componentDcTables[i] = (params >> 4) & 0x0F;
        }

        predictor = inputStream.read();

        // skip over spectral selection, lossless mode doesn't use it
        inputStream.skip(1);

        pointTransform = inputStream.read();
        pointTransform &= 0x0F;

        BitInputStream bitInputStream = new BitInputStream(inputStream);
        HuffmanTree huffmanTree = frameHuffmanTrees.get(componentDcTables[0]);

        // allocate a buffer for decoded pixels and fill it with diff values
        int[] decoded = new int[frame.numberOfLines * frame.numberOfSamples];
        for (int i=0; i<decoded.length; i++) {
            if (inputStream.isAtMarker()) {
                // oops, we hit a marker, break out
                // TODO handle the marker correctly (say, if it is a restart marker)
                break;
            }
            decoded[i] = huffmanTree.getNextValue(bitInputStream);
        }

        // now, let's reconstruct the image
        for (int j=0; j<frame.numberOfLines; j++) {
            for (int i=0; i<frame.numberOfSamples; i++) {
                int p = i + j * frame.numberOfSamples;
                int predictedValue;
                if (i==0 && j==0) {
                    predictedValue = 1 << (frame.samplePrecision - pointTransform - 1);
                } else if (j==0) {
                    int iPrev = (i-1) + j * frame.numberOfSamples;
                    predictedValue = decoded[iPrev];
                } else if (i==0) {
                    int jPrev = i + (j-1) * frame.numberOfSamples;
                    predictedValue = decoded[jPrev];
                } else {
                    int iPrev = (i-1) + j * frame.numberOfSamples;
                    int jPrev = i + (j-1) * frame.numberOfSamples;
                    int ijPrev = (i-1) + (j-1) * frame.numberOfSamples;
                    predictedValue = getPredictedValue(decoded[iPrev], decoded[jPrev], decoded[ijPrev], predictor);
                }
                decoded[p] += predictedValue;
            }
        }

        // pack the decoded image into a little endian byte array
        // TODO handle multiple components
        int precisionBytes = (frame.samplePrecision + 7) / 8;
        buffer = new byte[decoded.length * precisionBytes];
        for (int i=0; i<decoded.length; i++) {
            for (int bytePos=0; bytePos<precisionBytes; bytePos++) {
                buffer[i*precisionBytes+bytePos] = (byte) ((decoded[i] >> (bytePos * 8)) & 0xFF);
            }
        }

        return true;
    }

    private int getPredictedValue(int a, int b, int c, int predictor) {
        switch (predictor) {
            case 1:
                return a;
            case 2:
                return b;
            case 3:
                return c;
            case 4:
                return a + b - c;
            case 5:
                return a + (b - c) / 2;
            case 6:
                return b + (a - c) / 2;
            case 7:
                return (a + b) / 2;
            default:
                System.out.println("Unsupported predictor: " + predictor + ", defaulting to 1");
                return a;
        }
    }

    public byte[] getDecodedBuffer() {
        return buffer;
    }

}
