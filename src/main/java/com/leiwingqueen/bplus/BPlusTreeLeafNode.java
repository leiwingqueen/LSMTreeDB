package com.leiwingqueen.bplus;

public class BPlusTreeLeafNode<K extends Comparable, V> extends BPlusTreeNode {
    private Object[] keys;
    private Object[] values;
    private BPlusTreeNode next;

    public BPlusTreeLeafNode(int maxSize) {
        // left node has only n-1 keys
        this.maxSize = maxSize - 1;
        this.size = 0;
        keys = new Object[maxSize - 1];
        values = new Object[maxSize - 1];
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
            values[i] = values[i - 1];
        }
        keys[l] = key;
        values[l] = value;
        return true;
    }

    public void moveHalfTo(BPlusTreeLeafNode recipient) {
        // split into two parts.[0,splitIdx) and [splitIdx,size)
        int splitIdx = size / 2;
        for (int i = splitIdx; i < size; i++) {
            K key = getKey(i);
            V value = getValue(i);
            recipient.insert(key, value);
        }
        this.size -= size - splitIdx;
    }

    public void moveAllTo(BPlusTreeLeafNode recipient) {
        for (int i = 0; i < size; i++) {
            K key = getKey(i);
            V value = getValue(i);
            recipient.insert(key, value);
        }
        this.size = 0;
    }


    public void setNext(BPlusTreeNode next) {
        this.next = next;
    }
}
