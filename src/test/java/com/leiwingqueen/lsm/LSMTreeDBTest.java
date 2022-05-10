package com.leiwingqueen.lsm;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

@Slf4j
public class LSMTreeDBTest {

    @Test
    public void put() throws IOException, InterruptedException {
        LSMTreeDB db = new LSMTreeDB("/Users/liyongquan/bitcast");
        db.start();
        for (int i = 0; i < 100; i++) {
            db.put(String.valueOf(i), String.valueOf(i));
            Thread.sleep(100);
        }
        for (int i = 0; i < 100; i++) {
            Optional<String> opt = db.get(String.valueOf(i));
            Assert.assertEquals(String.valueOf(i), opt.get());
        }
    }
}