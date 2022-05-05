package com.leiwingqueen.lsm;

public class FileUtil {
    public static String buildFilename(String path, String filename) {
        StringBuilder sb = new StringBuilder(path);
        if (path.charAt(path.length() - 1) != '/') {
            sb.append('/');
        }
        sb.append(filename + ".log");
        return sb.toString();
    }
}
