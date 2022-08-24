package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
public class BPlusTreeImplTest {
    @Test
    public void get_5() {
        int n = 5;
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

    @Test
    public void get_1000() {
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

    @Test
    public void revert_insert_and_get() {
        int n = 1000;
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = n; i >= 1; i--) {
            tree.insert(i, i);
        }
        for (int i = 1; i <= n; i++) {
            Integer v = tree.get(i);
            Assert.assertEquals(i, v == null ? -1 : v.intValue());
        }
    }

    @Test
    public void shuffle_insert_and_get() {
        int n = 1000;
        List<Integer> list = new ArrayList<>(n);
        for (int i = 1; i <= n; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = 0; i < n; i++) {
            tree.insert(list.get(i), list.get(i));
        }
        for (int i = 1; i <= n; i++) {
            Integer v = tree.get(i);
            Assert.assertEquals(i, v == null ? -1 : v.intValue());
        }
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

    @Test
    public void testDelete_simple() {
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = 0; i < 5; i++) {
            tree.insert(i + 1, i + 1);
        }
        tree.remove(5);
        Assert.assertNull(tree.get(5));
        tree.remove(4);
        Assert.assertNull(tree.get(4));
    }

    @Test
    public void testDelete_1000() {
        int n = 1000;
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        for (int i = 1; i <= n; i++) {
            tree.insert(i, i);
            Assert.assertEquals(true, tree.checkValidate());
        }
        for (int i = 1; i <= n; i++) {
            Assert.assertEquals(i, tree.get(i).intValue());
            tree.remove(i);
            Assert.assertEquals(true, tree.checkValidate());
            Assert.assertNull(tree.get(i));
        }
    }
}