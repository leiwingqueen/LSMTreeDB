package com.leiwingqueen.bplus;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class BPlusTreeImplTest {
    @Test
    public void get() {
        BPlusTree<Integer, Integer> tree = new BPlusTreeImpl<>();
        tree.insert(1, 1);
        Integer v = tree.get(1);
        Assert.assertEquals(1, v.intValue());
    }

    @Test
    public void insert() {
    }
}