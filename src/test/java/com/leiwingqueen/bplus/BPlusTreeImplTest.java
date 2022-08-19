package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

@Slf4j
public class BPlusTreeImplTest {
    @Test
    public void get() {
        int n = 1000;
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = 1; i <= n; i++) {
            tree.insert(i, i);
        }
        for (int i = 1; i <= n; i++) {
            Integer v = tree.get(i);
            Assert.assertEquals(i, v == null ? -1 : v.intValue());
        }
        log.info("depth:{}", tree.getDepth());
    }

    // try to scan the leaf node
    @Test
    public void testScanAll() {
        int n = 100;
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = 1; i <= n; i++) {
            tree.insert(i, i);
        }
        Iterator<Pair<Integer, Integer>> iterator = tree.begin();
        int cnt = 0;
        while (iterator.hasNext()) {
            Pair<Integer, Integer> pair = iterator.next();
            log.info("[{},{}]", pair.getKey(), pair.getValue());
            cnt++;
        }
        Assert.assertEquals(100, cnt);
    }

    @Test
    public void testScanSpecifyKey() {
        int n = 100;
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = 1; i <= n; i++) {
            tree.insert(i, i);
        }
        Iterator<Pair<Integer, Integer>> iterator = tree.begin(51);
        int cnt = 0;
        while (iterator.hasNext()) {
            Pair<Integer, Integer> pair = iterator.next();
            log.info("[{},{}]", pair.getKey(), pair.getValue());
            cnt++;
        }
        Assert.assertEquals(50, cnt);
    }

    @Test
    public void testScan_keyNotExist() {
        int n = 100;
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = 1; i <= n; i++) {
            tree.insert(i, i);
        }
        Iterator<Pair<Integer, Integer>> iterator = tree.begin(101);
        int cnt = 0;
        while (iterator.hasNext()) {
            Pair<Integer, Integer> pair = iterator.next();
            log.info("[{},{}]", pair.getKey(), pair.getValue());
            cnt++;
        }
        Assert.assertEquals(0, cnt);
    }
}