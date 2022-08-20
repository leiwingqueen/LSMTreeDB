package com.leiwingqueen.bplus;

public abstract class BPlusTreeNode<K extends Comparable> {
    protected int size;
    protected int maxSize;
    // parent node
    protected BPlusTreeNode<K> parent;

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
}
