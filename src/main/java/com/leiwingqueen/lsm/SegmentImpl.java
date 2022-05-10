package com.leiwingqueen.lsm;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.TreeMap;

public class SegmentImpl implements Segment {
    private String path;
    private int segmentId;
    private int partSize;
    private SparseIndex sparseIndex;
    private RandomAccessFile reader;

    @Override
    public Command get(String key) throws IOException {
        SparseIndex.SparseIndexItem index = sparseIndex.findFirst(key);
        if (index == null) {
            return null;
        }
        reader.seek(index.offset);
        int remind = index.len;
        byte[] buffer = new byte[Constant.COMMAND_MAX_SIZE];
        while (remind > 0) {
            int commandSize = reader.readInt();
            reader.read(buffer, 0, commandSize);
            Command cmd = JSON.parseObject(buffer, Command.class);
            if (cmd.getKey().equals(key)) {
                return cmd;
            } else if (cmd.getKey().compareTo(key) > 0) {
                return null;
            }
            remind -= 4 + commandSize;
        }
        return null;
    }

    @Override
    public Collection<Pair<String, Command>> scan(String left, String right) {
        return null;
    }

    @Override
    public void persist(TreeMap<String, Command> memTable) throws IOException {
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
