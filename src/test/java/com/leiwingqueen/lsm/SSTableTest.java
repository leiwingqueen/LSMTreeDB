package com.leiwingqueen.lsm;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.util.TreeMap;

@Slf4j
public class SSTableTest {
    @Test
    public void persistent() throws IOException {
        SSTableImpl ssTable = new SSTableImpl(0, 100, "/Users/liyongquan/lsm");
        TreeMap<String, Command> map = new TreeMap<>();
        for (int i = 0; i < 100; i++) {
            map.put(String.valueOf(i), new Command(Command.OP_PUT, String.valueOf(i), String.valueOf(i)));
        }
        ssTable.persistent(map);
    }
}