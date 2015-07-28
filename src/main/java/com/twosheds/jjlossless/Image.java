/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import java.io.*;
import java.util.ArrayList;

import static com.twosheds.jjlossless.Marker.*;

public class Image {
    private ArrayList<Frame> frames;

    public static Image create(byte[] buffer) {
        if (buffer == null) {
            return null;
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        return create(byteArrayInputStream);
    }

    public static Image create(String path) {
        if (path == null) {
            return null;
        }

        return create(new File(path));
    }

    public static Image create(File file) {
        if (file == null) {
            return null;
        }
        if (!file.exists()) {
            System.out.println("Source JPEG file doesn't exist: " + file.getPath());
            return null;
        }
        if (file.isDirectory()) {
            System.out.println("Source JPEG file is a directory: " + file.getPath());
            return null;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            return create(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Image create(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }

        Image image = new Image();
        if (!image.read(inputStream)) {
            image = null;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    Image() {
        frames = new ArrayList<Frame>(1);
    }

    boolean read(InputStream inputStream) {
        try {
            JpegInputStream jpegInputStream = new JpegInputStream(inputStream);

            int currentMarker = jpegInputStream.readCurrentMarker();
            if (currentMarker != SOI) {
                System.out.println("Not a JPEG image");
                return false;
            }

            do {
                currentMarker = jpegInputStream.skipToNextMarker();
                if (currentMarker != EOI) {
                    Frame frame = new Frame();
                    if (frame.read(jpegInputStream)) {
                        frames.add(frame);
                    }
                }
            } while (currentMarker != EOI);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public int getWidth() {
        return frames.get(0).numberOfSamples;
    }

    public int getHeight() {
        return frames.get(0).numberOfLines;
    }

    public int getBitsPerPixel() {
        return frames.get(0).samplePrecision;
    }

    public int getNumChannels() {
        return frames.get(0).numberOfImageComponents;
    }

    public byte[] getDecodedPixels() {
        return frames.get(0).scans.get(0).getDecodedBuffer();
    }

}
