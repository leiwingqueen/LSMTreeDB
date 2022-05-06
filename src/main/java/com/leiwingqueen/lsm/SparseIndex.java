package com.leiwingqueen.lsm;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

public class SparseIndex {
    public static final int DEF_SIZE = 1024;
    private List<SparseIndexItem> indexItems;

    public SparseIndex() {
        indexItems = new ArrayList<>(DEF_SIZE);
    }

    public void addIndex(String key, long offset, int len) {
        indexItems.add(new SparseIndexItem(key, offset, len));
    }

    public byte[] toByteArray() {
        return JSON.toJSONBytes(indexItems);
    }

    public List<SparseIndexItem> getIndexItems() {
        return indexItems;
    }

    public void setIndexItems(List<SparseIndexItem> indexItems) {
        this.indexItems = indexItems;
    }

    static class SparseIndexItem {
        String key;
        long offset;
        int len;

        public SparseIndexItem(String key, long offset, int len) {
            this.key = key;
            this.offset = offset;
            this.len = len;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public int getLen() {
            return len;
        }

        public void setLen(int len) {
            this.len = len;
        }
    }
}
