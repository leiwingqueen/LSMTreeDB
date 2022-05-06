package com.leiwingqueen.lsm;


import com.leiwingqueen.bitcast.BitcastDB;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LSMTree java实现
 * https://yetanotherdevblog.com/lsm/
 */
public class LSMTreeDB {
    public static final int PART_SIZE = 1024;
    public static final int MEM_TABLE_MAX_SIZE = 1024;
    private String path;
    private TreeMap<String, Command> memTable;
    //实现的过程才慢慢理解，其实这个是为了实现WOC所需要的中间表,memTable先dump一份数据到immutableMemTable，
    // 然后再copy到ssTable，这样才能在实现不对memTable加锁的情况下并行持久化到磁盘
    private TreeMap<String, Command> immutableMemTable;
    private SSTable ssTable;
    private volatile boolean running;

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LSMTreeDB(String path) {
        this.path = path;
        this.memTable = new TreeMap<>();
        this.immutableMemTable = new TreeMap<>();
        this.ssTable = new SSTable(0, PART_SIZE, path);
        this.running = false;
    }

    public void start() {
        this.running = true;
        final LSMTreeDB db = this;
        Thread checkPersist = new Thread(() -> {
            while (db.running) {
                try {
                    db.memTablePersist();
                    Thread.sleep(1000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        checkPersist.start();
    }

    public void stop() {
        this.running = false;
    }


    public void put(String key, String value) throws IOException {
        try {
            lock.writeLock().lock();
            memTable.put(key, new Command(Command.OP_PUT, key, value));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(String key) throws IOException {
        try {
            lock.writeLock().lock();
            memTable.put(key, new Command(Command.OP_RM, key, ""));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<String> get(String key) throws IOException {
        try {
            lock.readLock().lock();
            if (!memTable.containsKey(key)) {
                return Optional.empty();
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<Pair<String, String>> scan(String left, String right) {
        return null;
    }

    public void memTablePersist() throws IOException {
        if (memTable.size() <= MEM_TABLE_MAX_SIZE) {
            return;
        }
        doMemTablePersist();
    }

    /**
     * memTable持久化
     */
    private void doMemTablePersist() throws IOException {
        //memTable->immutableMemTable
        lock.writeLock().lock();
        for (Map.Entry<String, Command> entry : memTable.entrySet()) {
            String key = entry.getKey();
            Command command = entry.getValue();
            immutableMemTable.put(key, command);
        }
        memTable.clear();
        lock.writeLock().unlock();
        ssTable.persistent(immutableMemTable);
        lock.writeLock().lock();
        immutableMemTable.clear();
        lock.writeLock().unlock();
    }
}
