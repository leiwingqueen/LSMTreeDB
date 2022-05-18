package com.leiwingqueen.lsm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

@Slf4j
public class LSMTreeDBTest {

    public static final int THREAD_COUNT = 10;

    @Test
    public void persistAndGet() throws IOException {
        LSMTreeDB db = new LSMTreeDB(TestConstant.TEST_PATH);
        db.start();
        for (int i = 0; i < 100; i++) {
            db.put(String.valueOf(i), String.valueOf(i));
        }
        for (int i = 0; i < 50; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(String.valueOf(i), opt.get());
        }
        db.memTablePersist();
        for (int i = 50; i < 100; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(String.valueOf(i), opt.get());
        }
    }

    @Test
    public void persistAndScan() throws IOException {
        LSMTreeDB db = new LSMTreeDB(TestConstant.TEST_PATH);
        db.start();
        TreeMap<String, String> treeMap = new TreeMap<>();
        for (int i = 0; i < 100; i++) {
            db.put(String.valueOf(i), String.valueOf(i));
            treeMap.put(String.valueOf(i), String.valueOf(i));
        }
        db.memTablePersist();
        String from = "0";
        String to = "20";
        Collection<Pair<String, String>> collection = db.scan(from, to);
        for (Pair<String, String> pair : collection) {
            log.info("key:{},value:{}", pair.getKey(), pair.getValue());
        }

        NavigableMap<String, String> expected = treeMap.subMap(from, true, to, true);
        Assert.assertEquals(expected.size(), collection.size());
        Iterator<Pair<String, String>> it1 = collection.iterator();
        Iterator<Map.Entry<String, String>> it2 = expected.entrySet().iterator();
        while (it1.hasNext()) {
            Assert.assertEquals(it2.next().getKey(), it1.next().getKey());
        }
    }

    //test shutdown the database and reload
    @Test
    public void shutdownAndReload_persist() throws IOException {
        LSMTreeDB db = new LSMTreeDB(TestConstant.TEST_PATH);
        db.start();
        for (int i = 0; i < 100; i++) {
            db.put(String.valueOf(i), String.valueOf(i));
        }
        for (int i = 50; i < 100; i++) {
            db.remove(String.valueOf(i));
        }
        db.memTablePersist();
        db.stop();
        db.start();
        for (int i = 0; i < 50; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(String.valueOf(i), opt.get());
        }
        for (int i = 50; i < 100; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(false, opt.isPresent());
        }
    }

    @Test
    public void shutdownAndReload_notPersist() throws IOException {
        LSMTreeDB db = new LSMTreeDB(TestConstant.TEST_PATH);
        db.start();
        for (int i = 0; i < 100; i++) {
            db.put(String.valueOf(i), String.valueOf(i));
        }
        for (int i = 50; i < 100; i++) {
            db.remove(String.valueOf(i));
        }
        db.stop();
        db.start();
        for (int i = 0; i < 50; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(String.valueOf(i), opt.get());
        }
        for (int i = 50; i < 100; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(false, opt.isPresent());
        }
    }

    @Test
    public void concurrent_readwrite() throws IOException {
        LSMTreeDB db = new LSMTreeDB(TestConstant.TEST_PATH);
        db.start();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < 100; i++) {
            final String key = String.valueOf(i);
            executor.execute(() -> {
                try {
                    db.put(key, key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        for (int i = 50; i < 100; i++) {
            db.remove(String.valueOf(i));
        }
        db.stop();
        db.start();
        for (int i = 0; i < 50; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(String.valueOf(i), opt.get());
        }
        for (int i = 50; i < 100; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(false, opt.isPresent());
        }
    }
}