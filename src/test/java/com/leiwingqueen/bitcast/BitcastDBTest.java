package com.leiwingqueen.bitcast;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

@Slf4j
public class BitcastDBTest {

    @Test
    public void put() throws IOException {
        BitcastDB db = new BitcastDB("/Users/liyongquan/bitcast");
        db.start();
        for (int i = 0; i < 1024; i++) {
            db.put(String.valueOf(i), String.valueOf(i));
        }
        db.remove("0");
        Assert.assertEquals(false, db.get("0").isPresent());
        for (int i = 1; i < 10; i++) {
            Assert.assertEquals(String.valueOf(i), db.get(String.valueOf(i)).get());
        }
    }
}