package com.leiwingqueen.bplus;

import lombok.Data;

public class BPlusTreeNode {
    protected int size;
    protected int maxSize;
    // parent node
    protected BPlusTreeNode parent;

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
}
