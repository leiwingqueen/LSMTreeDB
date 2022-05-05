package com.leiwingqueen.lsm;


import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.TreeMap;

/**
 * LSMTree java实现
 * https://yetanotherdevblog.com/lsm/
 */
public class LSMTreeDB {

    private TreeMap<String, Command> memTable;

    public void put(String key, String value) throws IOException {
        memTable.put(key, new Command(Command.OP_PUT, key, value));
    }

    public void remove(String key) throws IOException {
        memTable.put(key, new Command(Command.OP_RM, key, ""));
    }

    public Optional<String> get(String key) throws IOException {
        if (!memTable.containsKey(key)) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public Collection<Pair<String, String>> scan(String left, String right) {
        return null;
    }
}
