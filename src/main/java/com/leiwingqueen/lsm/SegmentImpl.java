package com.leiwingqueen.lsm;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.*;

@Slf4j
public class SegmentImpl implements Segment {
    private String path;
    private int segmentId;
    private int partSize;
    private SparseIndex sparseIndex;
    private RandomAccessFile reader;

    public SegmentImpl(String path, int segmentId, int partSize) {
        this.path = path;
        this.segmentId = segmentId;
        this.partSize = partSize;
    }

    @Override
    public void reload() throws IOException {
        String filename = FileUtil.buildFilename(path, String.valueOf(segmentId));
        reader = new RandomAccessFile(filename, "r");
        if (reader.length() < SegmentMetaData.META_DATA_SIZE) {
            log.error("read segment metadata fail");
            throw new EOFException("read segment metadata fail");
        }
        byte[] buffer = new byte[SegmentMetaData.META_DATA_SIZE];
        reader.read(buffer, 0, SegmentMetaData.META_DATA_SIZE);
        SegmentMetaData metaData = SegmentMetaData.readBytes(buffer);
        log.info("read metaData...{}", metaData);
        reader.seek(metaData.getIndexOffset());
        byte[] indexBuffer = new byte[metaData.getIndexLen()];
        reader.read(indexBuffer);
        this.sparseIndex = SparseIndex.parse(indexBuffer);
    }

    @Override
    public Command get(String key) throws IOException {
        log.info("get key...key:{}", key);
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
            Command cmd = JSON.parseObject(buffer, 0, commandSize, Charset.forName("utf8"), Command.class);
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
    public Collection<Command> scan(String left, String right) throws IOException {
        if (left.compareTo(right) > 0) {
            return Collections.emptyList();
        }
        SparseIndex.SparseIndexItem first = sparseIndex.findFirst(left);
        SparseIndex.SparseIndexItem last = sparseIndex.findFirst(right);
        if (last == null) {
            return Collections.emptyList();
        }
        long offset = 0;
        if (first != null) {
            offset = first.offset;
        }
        long end = last.offset + last.len;
        reader.seek(offset);
        byte[] buffer = new byte[Constant.COMMAND_MAX_SIZE];
        List<Command> res = new ArrayList<>();
        while (offset < end) {
            int commandSize = reader.readInt();
            reader.read(buffer, 0, commandSize);
            Command cmd = JSON.parseObject(buffer, 0, commandSize, Charset.forName("utf8"), Command.class);
            if (cmd.getKey().compareTo(right) > 0) {
                break;
            } else if (cmd.getKey().compareTo(left) >= 0) {
                res.add(cmd);
            }
            offset += 4 + commandSize;
        }
        return res;
    }

    @Override
    public void persist(TreeMap<String, Command> memTable) throws IOException {
        String filename = FileUtil.buildFilename(path, String.valueOf(segmentId));
        RandomAccessFile writer = new RandomAccessFile(filename, "rw");
        this.reader = new RandomAccessFile(filename, "r");
        writer.seek(SegmentMetaData.META_DATA_SIZE);
        long offset = SegmentMetaData.META_DATA_SIZE;
        int size = 0;
        int dataSize = 0;
        this.sparseIndex = new SparseIndex();
        String sparseIndexKey = "";
        for (Command command : memTable.values()) {
            if (StringUtils.isBlank(sparseIndexKey)) {
                sparseIndexKey = command.getKey();
            }
            byte[] json = JSON.toJSONBytes(command);
            writer.writeInt(json.length);
            writer.write(json);
            int len = 4 + json.length;
            size += len;
            dataSize += len;
            if (size >= partSize) {
                //write sparse index
                sparseIndex.addIndex(sparseIndexKey, offset, size);
                offset += size;
                size = 0;
                sparseIndexKey = "";
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
