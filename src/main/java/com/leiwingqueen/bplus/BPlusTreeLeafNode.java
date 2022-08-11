package com.leiwingqueen.bplus;

public class BPlusTreeLeafNode<K extends Comparable, V> extends BPlusTreeNode {
    private Object[] keys;
    private Object[] values;
    private BPlusTreeNode next;

    public BPlusTreeLeafNode(int maxSize) {
        keys = new Object[maxSize];
        values = new Object[maxSize];
    }

    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public V getValue(int idx) {
        return (V) values[idx];
    }
}
