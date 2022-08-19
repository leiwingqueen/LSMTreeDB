package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

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
}