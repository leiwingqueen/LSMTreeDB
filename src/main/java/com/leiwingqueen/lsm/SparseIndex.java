package com.leiwingqueen.lsm;

import java.util.Map;

public class SparseIndex {
    private Map<String, SparseIndexItem> map;

    static class SparseIndexItem {
        long offset;
        int len;
    }
}
