package com.leiwingqueen.bplus;

import lombok.Data;

@Data
public class BPlusTreeInternalNode<K extends Comparable> extends BPlusTreeNode {
    //keys[0] is useless
    private Object[] keys;
    private BPlusTreeNode[] values;

    public BPlusTreeInternalNode(BPlusTreeNode node, int maxSize) {
        this.size = 0;
        this.maxSize = maxSize;
        this.keys = new Object[maxSize];
        this.values = new BPlusTreeNode[maxSize];
        this.values[0] = node;
        this.size++;
    }

    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public BPlusTreeNode getPointer(int idx) {
        return values[idx];
    }

    public boolean insert(K key, BPlusTreeNode pointer) {
        //size>=1
        if (size == 1 || getKey(size - 1).compareTo(key) < 0) {
            keys[size] = key;
            values[size] = pointer;
            size++;
            return true;
        }
        // find the first one>key
        int l = 1, r = size - 1;
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
            values[i] = values[i - 1];
        }
        keys[l] = key;
        values[l] = pointer;
        size++;
        return true;
    }

    public void moveHalfTo(BPlusTreeInternalNode<K> recipient) {
        // split into two parts.[0,splitIdx) and [splitIdx,size)
        int splitIdx = size / 2;
        for (int i = splitIdx; i < size; i++) {
            K key = getKey(i);
            BPlusTreeNode value = getPointer(i);
            recipient.insert(key, value);
        }
        this.size -= size - splitIdx;
    }

    public void moveAllTo(BPlusTreeInternalNode<K> recipient) {
        for (int i = 1; i < size; i++) {
            K key = getKey(i);
            BPlusTreeNode value = getPointer(i);
            recipient.insert(key, value);
        }
        this.size = 0;
    }
}
