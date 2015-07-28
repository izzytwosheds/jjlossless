/*
 * Copyright 2015 the jjlossless Project Authors (Izzat Bahadirov et alia).
 * Licensed AS IS and WITHOUT WARRANTY under the Apache License,
 * Version 2.0. Details: <http://www.apache.org/licenses/LICENSE-2.0>
 */

package com.twosheds.jjlossless;

import java.io.IOException;
import java.util.LinkedList;

public class HuffmanTree {
    private int tableClass;
    private int destinationId;

    private Node rootNode;

    private class Node {
        private Node left;
        private Node right;
        private Integer value;
        private int level;

        private Node(int level) {
            this.level = level;
        }

        @Override
        public String toString() {
            return "level: " + level + ", value: " + value;
        }
    }

    HuffmanTree(int tableClass, int destinationId, byte[] bits, byte[] values) {
        this.tableClass = tableClass;
        this.destinationId = destinationId;

        rootNode = createHuffmanTree(bits, values);
    }

    private Node createHuffmanTree(byte[] bits, byte[] values) {
        int maxLevel = 0;
        for (int i=bits.length-1; i>=0; i--) {
            if (bits[i] != 0) {
                maxLevel = i;
                break;
            }
        }

        int valuePos = 0;
        Node rootNode = new Node(0);

        LinkedList<Node> queue = new LinkedList<Node>();
        queue.addLast(rootNode);

        while (!queue.isEmpty()) {
            Node currentNode = queue.removeFirst();
            int currentLevel = currentNode.level;

            // no need to create child nodes for the leaf node
            if (currentNode.value != null) {
                continue;
            }

            // we reached the last level, no need to add child nodes
            if (currentLevel <= maxLevel) {
                Node left = new Node(currentLevel + 1);
                Node right = new Node(currentLevel + 1);

                if (bits[currentLevel] > 0) {
                    left.value = (int) values[valuePos];
                    valuePos++;
                    bits[currentLevel]--;
                }

                if (bits[currentLevel] > 0) {
                    right.value = (int) values[valuePos];
                    valuePos++;
                    bits[currentLevel]--;
                }

                currentNode.left = left;
                currentNode.right = right;

                queue.addLast(left);
                queue.addLast(right);
            }
        }

        return rootNode;
    }

    int getTableClass() {
        return tableClass;
    }

    int getDestinationId() {
        return destinationId;
    }

    int getNextValue(BitInputStream inputStream) throws IOException {
        Node node = rootNode;
        while (node != null && node.value == null) {
            int direction = inputStream.getBit();
            if (direction < 0) {
                node = null;
            } else if (direction == 0) {
                node = node.left;
            } else {
                node = node.right;
            }
        }

        int numBits = 0;
        if (node != null) {
            numBits = node.value;
        }
        if (numBits == 16) {
            return (1 << 15);
        }

        return inputStream.getValue(numBits);
    }
}
