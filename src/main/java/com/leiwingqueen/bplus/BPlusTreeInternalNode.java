package com.leiwingqueen.bplus;

import lombok.Data;

@Data
public class BPlusTreeInternalNode<K extends Comparable> extends BPlusTreeNode {
    //keys[0] is useless
    private Object[] keys;
    private BPlusTreeNode[] values;

    public BPlusTreeInternalNode(int maxDegree) {
        this.size = 0;
        this.maxSize = maxDegree;
        this.keys = new Object[maxDegree];
        this.values = new BPlusTreeNode[maxDegree];
    }

    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public BPlusTreeNode getPointer(int idx) {
        return values[idx];
    }
}
