/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import java.io.File;
import java.io.FileOutputStream;

public class Main {
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.out.println("Not enough arguments!");
            return;
        }

        String command = args[0];
        if ("e".equals(command)) {
            System.out.println("Encoding is not supported yet");
            return;
        } else if ("d".equals(command)) {
            File srcFile = new File(args[1]);
            if (srcFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                for (File file: files) {
                    convertJpegToPgm(file);
                }
            } else {
                convertJpegToPgm(srcFile);
            }
        } else {
            System.out.println("Unknown command: " + command);
        }
    }

    private static void convertJpegToPgm(File srcFile) {
        if (!(srcFile.getName().endsWith(".jpg")
                || srcFile.getName().endsWith(".jpeg"))) {
            return;
        }

        System.out.println("Converting file " + srcFile.getPath());

        try {
            Image image = Image.create(srcFile);
            if (image != null) {
                File outFile = new File(srcFile.getParent(), srcFile.getName() + ".pgm");
                if (outFile.exists()) {
                    outFile.delete();
                }

                FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                fileOutputStream.write("P5\n".getBytes());
                fileOutputStream.write(String.valueOf(image.getWidth()).getBytes());
                fileOutputStream.write(" ".getBytes());
                fileOutputStream.write(String.valueOf(image.getHeight()).getBytes());
                fileOutputStream.write("\n".getBytes());
                int maxGrayscale = (1 << image.getBitsPerPixel()) - 1;
                fileOutputStream.write(String.valueOf(maxGrayscale).getBytes());
                fileOutputStream.write("\n".getBytes());
                fileOutputStream.write(image.getDecodedPixels());
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
