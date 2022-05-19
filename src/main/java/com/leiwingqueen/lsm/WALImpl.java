package com.leiwingqueen.lsm;

import com.alibaba.fastjson.JSON;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;

public class WALImpl implements WAL {
    public static final String FILE_NAME = "wal";
    public static final String TMP_FILE_NAME = "wal.tmp";
    private String path;
    RandomAccessFile writer;
    RandomAccessFile reader;

    @Override
    public void write(Command command) throws IOException {
        byte[] json = JSON.toJSONBytes(command);
        this.writer.writeInt(json.length);
        this.writer.write(json);
    }

    @Override
    public Optional<Command> read() throws IOException {
        int size;
        try {
            size = this.reader.readInt();
        } catch (EOFException e) {
            return Optional.empty();
        }
        if (size > 0) {
            byte[] buffer = new byte[size];
            this.reader.read(buffer);
            Command command = JSON.parseObject(buffer, Command.class);
            return Optional.of(command);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void readSeek(long pos) throws IOException {
        this.reader.seek(pos);
    }

    /**
     * clear the wal log
     */
    public void clear() throws IOException {
        this.reader.close();
        this.writer.close();
        File cur = new File(FileUtil.buildFilename(path, FILE_NAME));
        File rename = new File(TMP_FILE_NAME);
        cur.renameTo(rename);
        rename.delete();
        this.writer = new RandomAccessFile(FileUtil.buildFilename(path, FILE_NAME), "rw");
        this.reader = new RandomAccessFile(FileUtil.buildFilename(path, FILE_NAME), "r");
    }

    public WALImpl(String path) throws IOException {
        this.path = path;
        this.writer = new RandomAccessFile(FileUtil.buildFilename(path, FILE_NAME), "rw");
        this.reader = new RandomAccessFile(FileUtil.buildFilename(path, FILE_NAME), "r");
        writer.seek(writer.length());
    }
}
