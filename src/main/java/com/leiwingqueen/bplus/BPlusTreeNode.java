package com.leiwingqueen.bplus;

import java.util.concurrent.locks.ReadWriteLock;

public abstract class BPlusTreeNode<K extends Comparable> {
    protected int size;
    protected int maxSize;
    // parent node
    protected BPlusTreeNode<K> parent;
    // with a disk orient b+ tree,the latch should be defined in the page structure.
    protected ReadWriteLock latch;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public BPlusTreeNode getParent() {
        return parent;
    }

    public void setParent(BPlusTreeNode parent) {
        this.parent = parent;
    }

    public abstract K getKey(int idx);

    public ReadWriteLock getLatch() {
        return latch;
    }
}
