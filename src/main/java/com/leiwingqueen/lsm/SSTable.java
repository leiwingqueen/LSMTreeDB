package com.leiwingqueen.lsm;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.TreeMap;

public class SSTable {
    public static final int BUFFER_SIZE = 1024;
    private int segmentId;
    private int partSize;
    private String path;

    public SSTable(int segmentId, int partSize, String path) {
        this.segmentId = segmentId;
        this.partSize = partSize;
        this.path = path;
    }

    /**
     * 持久化memTable到Segment
     *
     * @param memTable
     */
    public void persistent(TreeMap<String, Command> memTable) throws IOException {
        segmentId++;
        String filename = FileUtil.buildFilename(path, String.valueOf(segmentId));
        RandomAccessFile writer = new RandomAccessFile(filename, "rw");
        long pos = 0;
        int size = 0;
        for (Command command : memTable.values()) {
            byte[] json = JSON.toJSONBytes(command);
            writer.write(json.length);
            writer.write(json);
            int len = 4 + json.length;
            pos += len;
            size += len;
            if (size >= partSize) {
                size = 0;
            }
        }
        if (size > 0) {
            //TODO:写入
        }
        writer.close();
    }
}
