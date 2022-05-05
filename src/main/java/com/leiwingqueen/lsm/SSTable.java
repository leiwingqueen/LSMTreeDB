package com.leiwingqueen.lsm;

import com.alibaba.fastjson.JSON;

import java.io.FileOutputStream;
import java.io.IOException;
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
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        segmentId++;
        String filename = FileUtil.buildFilename(path, String.valueOf(segmentId));
        FileChannel fileChannel = new FileOutputStream(filename).getChannel();
        for (Command command : memTable.values()) {
            byte[] json = JSON.toJSONBytes(command);
            buffer.putInt(json.length);
            buffer.put(json);
            if (buffer.position() >= partSize) {
                //写入一个索引
                fileChannel.write(buffer);
            }
            buffer.clear();
        }
        fileChannel.close();
    }
}
