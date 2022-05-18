package com.leiwingqueen.lsm;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

public interface Segment {
    /**
     * reload sparse index
     *
     * @throws IOException
     */
    void reload() throws IOException;

    //TODO:need to provide contains function with bloomFilter
    Command get(String key) throws IOException;

    Collection<Command> scan(String left, String right) throws IOException;

    void persist(TreeMap<String, Command> memTable) throws IOException;
}
