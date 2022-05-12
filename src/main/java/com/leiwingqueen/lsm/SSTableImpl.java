package com.leiwingqueen.lsm;

import java.io.IOException;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.TreeMap;

public class SSTableImpl {
    private int segmentId;
    private int partSize;
    private String path;
    Deque<Segment> segments;

    public SSTableImpl(int segmentId, int partSize, String path) {
        this.segmentId = segmentId;
        this.partSize = partSize;
        this.path = path;
        this.segments = new LinkedList<>();
    }

    public Command get(String key) throws IOException {
        for (Segment segment : segments) {
            Command command = segment.get(key);
            if (command != null) {
                return command;
            }
        }
        return null;
    }

    public Collection<Command> scan(String left, String right) throws IOException {
        TreeMap<String, Command> map = new TreeMap<>();
        for (Segment segment : segments) {
            Collection<Command> commands = segment.scan(left, right);
            for (Command command : commands) {
                if (!map.containsKey(command.getKey())) {
                    map.put(command.getKey(), command);
                }
            }
        }
        return map.values();
    }

    /**
     * 持久化memTable到Segment
     *
     * @param memTable
     */
    public void persistent(TreeMap<String, Command> memTable) throws IOException {
        segmentId++;
        Segment segment = new SegmentImpl(path, segmentId, partSize);
        segment.persist(memTable);
        segments.offerFirst(segment);
    }
}
