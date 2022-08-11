package com.leiwingqueen.bplus;

public interface BPlusTree<K, V> {
    V get(K key);

    boolean insert(K key, V value);
}
