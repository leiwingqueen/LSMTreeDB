package com.leiwingqueen.lsm;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LSMTree java implement
 * https://yetanotherdevblog.com/lsm/
 */
@Slf4j
public class LSMTreeDB {
    public static final int PART_SIZE = 1024;
    public static final int MEM_TABLE_MAX_SIZE = 10;
    private String path;
    private TreeMap<String, Command> memTable;
    //实现的过程才慢慢理解，其实这个是为了实现WOC所需要的中间表,memTable先dump一份数据到immutableMemTable，
    // 然后再copy到ssTable，这样才能在实现不对memTable加锁的情况下并行持久化到磁盘
    private TreeMap<String, Command> immutableMemTable;
    private SSTableImpl ssTable;
    private volatile boolean running;

    private WAL wal;

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LSMTreeDB(String path) throws IOException {
        this.path = path;
        this.memTable = new TreeMap<>();
        this.immutableMemTable = new TreeMap<>();
        this.ssTable = new SSTableImpl(0, PART_SIZE, path);
        this.running = false;
        this.wal = new WALImpl(path);
    }

    public void start() throws IOException {
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
        reload();
    }

    private void reload() throws IOException {
        wal.readSeek(0);
        while (true) {
            Optional<Command> opt = wal.read();
            if (opt.isEmpty()) {
                break;
            }
            Command command = opt.get();
            this.memTable.put(command.getKey(), command);
        }
        ssTable.reload();
    }

    public void stop() {
        this.running = false;
        memTable.clear();
        immutableMemTable.clear();
        ssTable.destroy();
    }


    public void put(String key, String value) throws IOException {
        try {
            lock.writeLock().lock();
            Command command = new Command(Command.OP_PUT, key, value);
            memTable.put(key, command);
            wal.write(command);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(String key) throws IOException {
        try {
            lock.writeLock().lock();
            Command command = new Command(Command.OP_RM, key, "");
            memTable.put(key, command);
            wal.write(command);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<String> get(String key) throws IOException {
        try {
            lock.readLock().lock();
            Command command = null;
            //memTable > immutableMemTable > SSTable
            if (memTable.containsKey(key)) {
                command = memTable.get(key);
            } else if (immutableMemTable.containsKey(key)) {
                command = immutableMemTable.get(key);
            } else {
                //查找SSTable
                command = ssTable.get(key);
            }
            if (command == null || Command.OP_RM.equals(command.getOp())) {
                return Optional.empty();
            }
            if (Command.OP_PUT.equals(command.getOp())) {
                return Optional.of(command.getValue());
            } else if (Command.OP_RM.equals(command.getOp())) {
                return Optional.empty();
            } else {
                throw new IllegalArgumentException("命令异常");
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<Pair<String, String>> scan(String left, String right) throws IOException {
        TreeMap<String, Command> map = new TreeMap<>();
        for (Command command : memTable.subMap(left, true, right, true).values()) {
            map.put(command.getKey(), command);
        }
        for (Command command : immutableMemTable.subMap(left, true, right, true).values()) {
            if (!map.containsKey(command.getKey())) {
                map.put(command.getKey(), command);
            }
        }
        for (Command command : ssTable.scan(left, right)) {
            if (!map.containsKey(command.getKey())) {
                map.put(command.getKey(), command);
            }
        }
        List<Pair<String, String>> list = new ArrayList<>(map.size());
        for (Command command : map.values()) {
            if (command == null || Command.OP_RM.equals(command.getOp())) {
                continue;
            }
            if (Command.OP_PUT.equals(command.getOp())) {
                list.add(Pair.of(command.getKey(), command.getValue()));
            } else {
                throw new IllegalArgumentException("命令异常");
            }
        }
        return list;
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
        log.info("memTable persist[start]...");
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
        log.info("memTable persist[finish]...");
    }
}
