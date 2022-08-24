package com.leiwingqueen.bplus;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;

public interface BPlusTree<K, V> {
    // return the value associated with a given key
    V get(K key);

    // insert a key-value pair into B+ tree
    boolean insert(K key, V value);

    // remove key
    boolean remove(K key);

    // index iterator
    Iterator<Pair<K, V>> begin();

    Iterator<Pair<K, V>> begin(K key);

    int getDepth();

    boolean checkValidate();
}
