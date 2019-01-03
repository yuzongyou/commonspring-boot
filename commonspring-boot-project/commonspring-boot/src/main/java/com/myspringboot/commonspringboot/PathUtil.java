package com.myspringboot.commonspringboot;

/**
 * yzy
 */
public class PathUtil {
    private PathUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 合理化路径， 将 \\ 转成 /
     *
     * @param path 路径
     * @return 返回替换 \\ // 的地址
     */
    public static String normalizePath(String path) {
        if (null != path) {
            return path.replaceAll("[/\\\\]+", "/");
        }
        return null;
    }
}
