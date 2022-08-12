package com.leiwingqueen.bplus;

public class BPlusTreeImpl<K extends Comparable, V> implements BPlusTree<K, V> {

    public static final int MAX_SIZE = 3;

    private BPlusTreeNode root;

    public BPlusTreeImpl() {
        root = new BPlusTreeLeafNode<K, V>(MAX_SIZE);
    }

    @Override
    public V get(K key) {
        BPlusTreeLeafNode<K, V> node = find(root, key);
        return getValueInLeaf(node, key);
    }

    @Override
    public boolean insert(K key, V value) {
        BPlusTreeLeafNode<K, V> node = find(root, key);
        // insert in leaf node
        if (node.size < node.maxSize) {
            return node.insert(key, value);
        }

        return false;
    }

    private BPlusTreeLeafNode<K, V> find(BPlusTreeNode node, K key) {
        if (node instanceof BPlusTreeLeafNode) {
            return (BPlusTreeLeafNode) node;
        }
        BPlusTreeInternalNode<K> innerNode = (BPlusTreeInternalNode<K>) node;
        //inner node index 0 key is useless.we can use binary search for speed up
        /*int idx = 1;
        while (idx < innerNode.size && innerNode.getKey(idx).compareTo(key) >= 0) {
            idx++;
        }*/
        //find the first key > $key
        if (innerNode.getKey(innerNode.size - 1).compareTo(key) <= 0) {
            return find(innerNode.getPointer(innerNode.size - 1), key);
        }
        int l = 1, r = innerNode.size - 1;
        while (l < r) {
            int mid = l + (r - l) / 2;
            K selectKey = innerNode.getKey(mid);
            if (selectKey.compareTo(key) > 0) {
                r = mid;
            } else {
                l = mid + 1;
            }
        }
        return find(innerNode.getPointer(l - 1), key);
    }

    private V getValueInLeaf(BPlusTreeLeafNode<K, V> node, K key) {
        int idx = 0;
        if (key.compareTo(node.getKey(0)) < 0
                || key.compareTo(node.getValue(node.size - 1)) > 0) {
            return null;
        }
        int l = 0, r = node.size - 1;
        while (l < r) {
            int mid = l + (r - l) / 2;
            K selectKey = node.getKey(mid);
            if (selectKey.compareTo(key) == 0) {
                return node.getValue(idx);
            } else if (selectKey.compareTo(key) > 0) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return null;
    }
}
