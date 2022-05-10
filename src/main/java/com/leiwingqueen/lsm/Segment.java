package com.leiwingqueen.lsm;

import org.apache.commons.lang3.tuple.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

public interface Segment {
    Command get(String key) throws IOException;

    Collection<Pair<String, Command>> scan(String left, String right);

    void persist(TreeMap<String, Command> memTable) throws IOException;
}
