package com.leiwingqueen.bplus;

public class BPlusTreeLeafNode<K extends Comparable, V> extends BPlusTreeNode {
    private Object[] keys;
    private Object[] values;
    private BPlusTreeNode next;

    public BPlusTreeLeafNode(int maxDegree) {
        // left node has only n-1 keys
        this.maxSize = maxDegree - 1;
        this.size = 0;
        keys = new Object[maxDegree - 1];
        values = new Object[maxDegree - 1];
    }

    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public V getValue(int idx) {
        return (V) values[idx];
    }

    public boolean insert(K key, V value) {
        if (getKey(size - 1).compareTo(key) < 0) {
            keys[size] = key;
            values[size] = value;
            size++;
            return true;
        }
        // find the first one>key
        int l = 0, r = size - 1;
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (getKey(mid).compareTo(key) == 0) {
                return false;
            } else if (getKey(mid).compareTo(key) > 0) {
                r = mid;
            } else {
                l = mid + 1;
            }
        }
        // move [l,size) one step
        for (int i = size; i > l; i++) {
            keys[i] = keys[i - 1];
            values[i] = keys[i - 1];
        }
        keys[l] = key;
        values[l] = value;
        return true;
    }
}
