package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class BPlusTreeInternalNode<K extends Comparable> extends BPlusTreeNode<K> {
    //keys[0] is useless
    private Object[] keys;
    private BPlusTreeNode<K>[] values;

    public BPlusTreeInternalNode(int maxSize) {
        this.size = 0;
        this.maxSize = maxSize;
        this.keys = new Object[maxSize];
        this.values = new BPlusTreeNode[maxSize];
    }

    @Override
    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public BPlusTreeNode<K> getPointer(int idx) {
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
        for (int i = size; i > l; i--) {
            keys[i] = keys[i - 1];
            values[i] = values[i - 1];
        }
        keys[l] = key;
        values[l] = pointer;
        size++;
        return true;
    }

    public int remove(K key) {
        int idx = findKeyIndex(key);
        if (idx < 0) {
            return size;
        }
        // move the entries move to the left where the entries right to the remove key
        for (int i = idx; i < size - 1; i++) {
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
    public void populateNewRoot(BPlusTreeNode oldValue, K newKey, BPlusTreeNode newValue) {
        this.values[0] = oldValue;
        this.values[1] = newValue;
        this.keys[1] = newKey;
        this.size += 2;
        oldValue.parent = this;
        newValue.parent = this;
    }

    /*****************************************************************************
     * SPLIT
     *****************************************************************************/

    public void moveHalfTo(BPlusTreeInternalNode<K> recipient) {
        // split into two parts.[0,splitIdx) and [splitIdx,size)
        int splitIdx = size / 2;
        int idx = 0;
        for (int i = splitIdx; i < size; i++) {
            recipient.keys[idx] = getKey(i);
            recipient.values[idx] = getPointer(i);
            getPointer(i).parent = recipient;
            idx++;
        }
        recipient.size = idx;
        recipient.parent = this.parent;
        this.size -= size - splitIdx;
    }

    /* Copy entries into me, starting from {items} and copy {size} entries.
     * Since it is an internal page, for all entries (pages) moved, their parents page now changes to me.
     * So I need to 'adopt' them by changing their parent page id, which needs to be persisted with BufferPoolManger
     */
    public void copyNFrom(BPlusTreeInternalNode<K> node, int from, int to) {
        int idx = 0;
        for (int i = from; i < to; i++) {
            if (idx > 0) {
                this.keys[idx] = node.getKey(i);
            }
            this.values[idx] = node.getPointer(i);
            this.values[idx].parent = this;
            idx++;
        }
        this.size = idx;
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

    /**
     * find the sliding node,return form like [pre sliding node,post sliding node]
     *
     * @return
     */
    public BPlusTreeInternalNode<K>[] findSlidingNodes() {
        if (parent == null) {
            return new BPlusTreeInternalNode[]{null, null};
        }
        BPlusTreeInternalNode<K> p = (BPlusTreeInternalNode<K>) this.parent;
        BPlusTreeInternalNode pre = null, post = null;
        int index = p.findKeyIndex(getKey(0));
        if (index > 0) {
            pre = (BPlusTreeInternalNode<K>) getPointer(index - 1);
        }
        if (index < p.size - 1) {
            post = (BPlusTreeInternalNode<K>) getPointer(index + 1);
        }
        return new BPlusTreeInternalNode[]{pre, post};
    }

    /**
     * find the key index
     *
     * @param key
     * @return
     */
    public int findKeyIndex(K key) {
        int l = 1, r = size - 1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            if (getKey(mid).compareTo(key) == 0) {
                return mid;
            } else if (getKey(mid).compareTo(key) > 0) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return -1;
    }

    public void setKeyAt(int index, K key) {
        this.keys[index] = key;
    }

    /*
     * Remove the last key & value pair from this page to head of "recipient" page.
     * You need to handle the original dummy key properly, e.g. updating recipient’s array to position the middle_key at the
     * right place.
     * You also need to use BufferPoolManager to persist changes to the parent page id for those pages that are
     * moved to the recipient
     */
    public K moveLastToFrontOf(BPlusTreeInternalNode<K> recipient, K middleKey) {
        for (int i = recipient.size - 1; i >= 0; i--) {
            recipient.keys[i + 1] = recipient.keys[i];
            recipient.values[i + 1] = recipient.values[i];
        }
        recipient.keys[0] = middleKey;
        recipient.values[0] = getPointer(size - 1);
        getPointer(size - 1).parent = this;
        recipient.size++;
        K key = this.getKey(size - 1);
        size--;
        return key;
    }

    /*
     * Remove the first key & value pair from this page to tail of "recipient" page.
     *
     * The middle_key is the separation key you should get from the parent. You need
     * to make sure the middle key is added to the recipient to maintain the invariant.
     * You also need to use BufferPoolManager to persist changes to the parent page id for those
     * pages that are moved to the recipient
     */
    public K moveFirstToEndOf(BPlusTreeInternalNode<K> recipient, K middleKey) {
        int n = recipient.size;
        recipient.keys[n] = middleKey;
        recipient.values[n] = this.getPointer(0);
        recipient.size++;
        K key = getKey(1);
        // all element move to the left
        values[0] = values[1];
        for (int i = 1; i < size - 1; i++) {
            keys[i] = keys[i + 1];
            values[i] = values[i + 1];
        }
        size--;
        return key;
    }

    /*****************************************************************************
     * MERGE
     *****************************************************************************/
    /*
     * Remove all of key & value pairs from this page to "recipient" page.
     * The middle_key is the separation key you should get from the parent. You need
     * to make sure the middle key is added to the recipient to maintain the invariant.
     * You also need to use BufferPoolManager to persist changes to the parent page id for those
     * pages that are moved to the recipient
     */
    public void moveAllTo(BPlusTreeInternalNode<K> recipient, K middleKey) {
        int n = recipient.size;
        recipient.keys[n] = middleKey;
        recipient.values[n] = getPointer(0);
        for (int i = 1; i < this.size; i++) {
            recipient.keys[n + i] = getKey(i);
            recipient.values[n + i] = getPointer(i);
        }
        recipient.size += this.size;
        this.size = 0;
    }

    @Override
    public String toString() {
        return "BPlusTreeInternalNode{" +
                "keys=" + StringUtils.join(keys, ",", 0, size) +
                ", values=" + StringUtils.join(values, ",", 0, size) +
                '}';
    }
}
