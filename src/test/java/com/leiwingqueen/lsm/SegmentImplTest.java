package com.leiwingqueen.lsm;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

@Slf4j
public class SegmentImplTest {

    @Test
    public void persistAndGet() throws IOException {
        Segment segment = new SegmentImpl(TestConstant.TEST_PATH, 1, 1024);
        TreeMap<String, Command> memTable = new TreeMap<>();
        for (int i = 0; i < 100; i++) {
            memTable.put(String.valueOf(i), new Command(Command.OP_PUT, String.valueOf(i), String.valueOf(i)));
        }
        segment.persist(memTable);
        for (int i = 0; i < 100; i++) {
            Command command = segment.get(String.valueOf(i));
            Assert.assertEquals(String.valueOf(i), command.getValue());
        }
    }

    @Test
    public void persistAndScan() throws IOException {
        Segment segment = new SegmentImpl(TestConstant.TEST_PATH, 1, 1024);
        TreeMap<String, Command> memTable = new TreeMap<>();
        for (int i = 0; i < 100; i++) {
            memTable.put(String.valueOf(i), new Command(Command.OP_PUT, String.valueOf(i), String.valueOf(i)));
        }
        segment.persist(memTable);
        String from = "0";
        String to = "20";
        Collection<Command> collection = segment.scan(from, to);
        for (Command command : collection) {
            log.info("{}", command.getKey());
        }
        NavigableMap<String, Command> expected = memTable.subMap(from, true, to, true);
        Assert.assertEquals(expected.size(), collection.size());
        Iterator<Command> it1 = collection.iterator();
        Iterator<Command> it2 = expected.values().iterator();
        while (it1.hasNext()) {
            Assert.assertEquals(it2.next().getKey(), it1.next().getKey());
        }
    }
}