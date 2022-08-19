package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BPlusTreeImpl<K extends Comparable, V> implements BPlusTree<K, V> {

    public static final int MAX_DEGREE = 3;

    private BPlusTreeNode root;

    public BPlusTreeImpl() {
        root = new BPlusTreeLeafNode<K, V>(MAX_DEGREE - 1);
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
        // need to split
        log.info("leaf node need to split...key:{},value:{}", key, value);
        BPlusTreeLeafNode<K, V> tmpNode = new BPlusTreeLeafNode<>(MAX_DEGREE);
        node.moveAllTo(tmpNode);
        if (!tmpNode.insert(key, value)) {
            return false;
        }
        BPlusTreeLeafNode<K, V> splitNode = new BPlusTreeLeafNode<>(MAX_DEGREE - 1);
        node.setNext(splitNode);
        tmpNode.moveHalfTo(splitNode);
        //splitNode.parent = node.parent;
        tmpNode.moveAllTo(node);
        // pKey need to add to the parent node
        K pKey = splitNode.getKey(0);
        log.info("leaf node split to {} and {}.split key:{}", node, splitNode, pKey);
        insertInParent(node, pKey, splitNode);
        return true;
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
        if (node.size == 0 || key.compareTo(node.getKey(0)) < 0
                || key.compareTo(node.getValue(node.size - 1)) > 0) {
            return null;
        }
        int l = 0, r = node.size - 1;
        while (l <= r) {
            int mid = l + (r - l) / 2;
            K selectKey = node.getKey(mid);
            if (selectKey.compareTo(key) == 0) {
                return node.getValue(mid);
            } else if (selectKey.compareTo(key) > 0) {
                r = mid - 1;
            } else {
                l = mid + 1;
            }
        }
        return null;
    }

    // referer to book <<database system concept>>
    private void insertInParent(BPlusTreeNode node, K key, BPlusTreeNode pointer) {
        log.info("add node to parent...parent:{},key:{}", node.parent, key);
        if (node.parent == null) {
            BPlusTreeInternalNode<K> newRoot = new BPlusTreeInternalNode<>(MAX_DEGREE);
            newRoot.populateNewRoot(node, key, pointer);
            //newRoot.insert(key, pointer);
            //node.parent = newRoot;
            this.root = newRoot;
            //pointer.parent = newRoot;
            return;
        }
        BPlusTreeInternalNode<K> parent = (BPlusTreeInternalNode<K>) node.parent;
        // pointer.parent = parent;
        if (parent.size < parent.maxSize) {
            parent.insert(key, pointer);
            return;
        }
        // need to split
        BPlusTreeInternalNode<K> tmpNode = new BPlusTreeInternalNode<>(MAX_DEGREE + 1);
        K midKey = parent.moveAllTo(tmpNode);
        tmpNode.insert(key, pointer);
        BPlusTreeInternalNode<K> splitNode = new BPlusTreeInternalNode<>(MAX_DEGREE);
        tmpNode.moveHalfTo(splitNode);
        tmpNode.moveAllTo(parent);
        log.info("inner node split to {} and {}.split key:{}", parent, splitNode, midKey);
        // new key need to add parent
        insertInParent(parent, midKey, splitNode);
    }
}
