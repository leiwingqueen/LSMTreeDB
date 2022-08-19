package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
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

    public int remove(int index) {
        // move the entries move to the left where the entries right to the remove key
        for (int i = index; i < size - 1; i++) {
            keys[i] = keys[i + 1];
            values[i] = values[i + 1];
        }
        size--;
        return size;
    }

    /*
     * Populate new root page with old_value + new_key & new_value
     * When the insertion cause overflow from leaf page all the way upto the root
     * page, you should create a new root page and populate its elements.
     * NOTE: This method is only called within InsertIntoParent()(b_plus_tree.cpp)
     *
     * refer to cmu bustub api design
     */
    void populateNewRoot(BPlusTreeNode oldValue, K newKey, BPlusTreeNode newValue) {
        this.values[0] = oldValue;
        this.values[1] = newValue;
        this.keys[1] = newKey;
        this.size += 2;
        oldValue.parent = this;
        newValue.parent = this;
    }

    public void moveHalfTo(BPlusTreeInternalNode<K> recipient) {
        // split into two parts.[0,splitIdx) and [splitIdx,size)
        int splitIdx = size / 2;
        recipient.values[0] = getPointer(splitIdx);
        // parent point change
        getPointer(splitIdx).parent = recipient;
        recipient.size++;
        for (int i = splitIdx + 1; i < size; i++) {
            recipient.keys[recipient.size] = getKey(i);
            recipient.values[recipient.size] = getPointer(i);
            getPointer(i).parent = recipient;
            recipient.size++;
        }
        recipient.parent = this.parent;
        this.size -= size - splitIdx;
    }

    /**
     * move all entities to the recipient
     *
     * @param recipient
     * @return the middle key
     */
    public K moveAllTo(BPlusTreeInternalNode<K> recipient) {
        for (int i = 0; i < size; i++) {
            recipient.keys[recipient.size] = getKey(i);
            recipient.values[recipient.size] = getPointer(i);
            getPointer(i).parent = recipient;
            recipient.size++;
        }
        //split to [0,splitIdx),[splitIdx,size)
        // need to add one because we need to add the new key later
        int splitIdx = (recipient.size + 1) / 2;
        recipient.parent = this.parent;
        this.size = 0;
        return recipient.getKey(splitIdx);
    }

    @Override
    public String toString() {
        return "BPlusTreeInternalNode{" +
                "keys=" + StringUtils.join(keys, ",", 0, size) +
                ", values=" + StringUtils.join(values, ",", 0, size) +
                '}';
    }
}
