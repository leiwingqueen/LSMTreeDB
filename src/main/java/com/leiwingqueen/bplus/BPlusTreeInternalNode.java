package com.leiwingqueen.bplus;

import lombok.Data;

@Data
public class BPlusTreeInternalNode<K extends Comparable> extends BPlusTreeNode {
    //keys[0] is useless
    private Object[] keys;
    private BPlusTreeNode[] values;

    public BPlusTreeInternalNode(int maxSize) {
        this.size = 0;
        this.maxSize = maxSize;
        this.keys = new Object[maxSize];
        this.values = new BPlusTreeNode[maxSize];
    }

    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public BPlusTreeNode getPointer(int idx) {
        return values[idx];
    }
}
