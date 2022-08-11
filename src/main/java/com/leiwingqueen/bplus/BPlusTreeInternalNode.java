package com.leiwingqueen.bplus;

import lombok.Data;

@Data
public class BPlusTreeInternalNode<K extends Comparable> extends BPlusTreeNode {
    //keys[0]是没有意义的
    private Object[] keys;
    private BPlusTreeNode[] values;

    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public BPlusTreeNode getPointer(int idx) {
        return values[idx];
    }
}
