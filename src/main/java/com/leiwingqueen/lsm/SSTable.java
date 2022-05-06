package com.leiwingqueen.lsm;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.TreeMap;

public class SSTable {
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
        writer.seek(SegmentMetaData.META_DATA_SIZE);
        long offset = SegmentMetaData.META_DATA_SIZE;
        int size = 0;
        int dataSize = 0;
        SparseIndex sparseIndex = new SparseIndex();
        String sparseIndexKey = "";
        for (Command command : memTable.values()) {
            if (StringUtils.isBlank(sparseIndexKey)) {
                sparseIndexKey = command.getKey();
            }
            byte[] json = JSON.toJSONBytes(command);
            writer.write(json.length);
            writer.write(json);
            int len = 4 + json.length;
            size += len;
            dataSize += len;
            if (size >= partSize) {
                //写入稀疏索引
                sparseIndex.addIndex(sparseIndexKey, offset, size);
                offset += size;
                size = 0;
            }
        }
        if (size > 0) {
            //写入稀疏索引
            sparseIndex.addIndex(sparseIndexKey, offset, size);
        }
        //稀疏索引持久化
        byte[] indexData = sparseIndex.toByteArray();
        writer.write(indexData);
        //写入元信息
        SegmentMetaData metaData = new SegmentMetaData(SegmentMetaData.META_DATA_SIZE, dataSize,
                SegmentMetaData.META_DATA_SIZE + dataSize, indexData.length);
        writer.seek(0);
        writer.write(metaData.toByteArray());
        writer.close();
    }
}
