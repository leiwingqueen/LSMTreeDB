package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class BPlusTreeLeafNode<K extends Comparable, V> extends BPlusTreeNode<K> {
    private Object[] keys;
    private Object[] values;
    private BPlusTreeLeafNode<K, V> next;

    public BPlusTreeLeafNode(int maxSize) {
        // left node has only n-1 keys
        this.maxSize = maxSize;
        this.size = 0;
        keys = new Object[maxSize];
        values = new Object[maxSize];
    }

    @Override
    public K getKey(int idx) {
        return (K) keys[idx];
    }

    public V getValue(int idx) {
        return (V) values[idx];
    }

    public boolean insert(K key, V value) {
        if (size == 0 || getKey(size - 1).compareTo(key) < 0) {
            keys[size] = key;
            values[size] = value;
            log.info("insert node into leaf node,idx:{},key:{},value:{}", size, key, value);
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
        for (int i = size; i > l; i--) {
            keys[i] = keys[i - 1];
            values[i] = values[i - 1];
        }
        keys[l] = key;
        values[l] = value;
        size++;
        log.info("insert node into leaf node,idx:{},key:{},value:{}", l, key, value);
        return true;
    }

    /**
     * First look through leaf page to see whether delete key exist or not. If
     * exist, perform deletion, otherwise return immediately.
     * NOTE: store key&value pair continuously after deletion
     *
     * @return page size after deletion
     */
    public int remove(K key) {
        int idx = keyIndex(key);
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

    // helper for insert operation to split node
    public void moveHalfTo(BPlusTreeLeafNode recipient) {
        // split into two parts.[0,splitIdx) and [splitIdx,size)
        int splitIdx = size / 2;
        for (int i = splitIdx; i < size; i++) {
            recipient.keys[recipient.size] = getKey(i);
            recipient.values[recipient.size] = getValue(i);
            recipient.size++;
        }
        // recipient.parent = this.parent;
        this.size -= size - splitIdx;
    }

    public void copyNFrom(BPlusTreeLeafNode<K, V> node, int from, int to) {
        int idx = 0;
        for (int i = from; i < to; i++) {
            keys[idx] = node.getKey(i);
            values[idx] = node.getValue(i);
            idx++;
        }
        this.size = idx;
    }

    /*****************************************************************************
     * MERGE
     *****************************************************************************/

    /*
     * Remove all of key & value pairs from this page to "recipient" page. Don't forget
     * to update the next_page id in the sibling page
     */
    public void moveAllTo(BPlusTreeLeafNode recipient) {
        for (int i = 0; i < size; i++) {
            recipient.keys[recipient.size] = getKey(i);
            recipient.values[recipient.size] = getValue(i);
            recipient.size++;
        }
        recipient.parent = this.parent;
        recipient.next = this.getNext();
        this.size = 0;
    }

    /*
     * Remove the last key & value pair from this page to "recipient" page.
     */
    public void moveLastToFrontOf(BPlusTreeLeafNode<K, V> recipient) {
        for (int i = recipient.size - 1; i >= 0; i--) {
            recipient.keys[i + 1] = recipient.keys[i];
            recipient.values[i + 1] = recipient.values[i];
        }
        recipient.keys[0] = this.getKey(size - 1);
        recipient.values[0] = this.getValue(size - 1);
        recipient.size++;
        size--;
    }

    /*
     * Remove the first key & value pair from this page to "recipient" page.
     */
    public void moveFirstToEndOf(BPlusTreeLeafNode<K, V> recipient) {
        int n = recipient.size;
        recipient.keys[n] = getKey(0);
        recipient.values[n] = getValue(0);
        for (int i = 0; i < size - 1; i++) {
            keys[i] = keys[i + 1];
            values[i] = values[i + 1];
        }
        size--;
        recipient.size++;
    }

    /**
     * Helper method to find the first index i so that array[i].first >= key
     *
     * @param key
     * @return
     */
    public int keyIndex(K key) {
        int l = 0, r = size - 1;
        if (getKey(r).compareTo(key) < 0) {
            return -1;
        }
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (getKey(mid).compareTo(key) >= 0) {
                r = mid;
            } else {
                l = mid + 1;
            }
        }
        return l;
    }


    public void setNext(BPlusTreeLeafNode<K, V> next) {
        this.next = next;
    }

    public BPlusTreeLeafNode<K, V> getNext() {
        return next;
    }

    @Override
    public String toString() {
        return "BPlusTreeLeafNode{" +
                "keys=" + StringUtils.join(keys, ",", 0, size) +
                ", values=" + StringUtils.join(keys, ",", 0, size) +
                '}';
    }
}
