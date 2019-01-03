package com.myspringboot.commonspringboot;

/**
 * @author yzy
 */
public interface KeyFilter {

    /**
     * 是否是指定的key
     *
     * @param key 要检查的key
     * @return 需要则返回 true， 不需要则返回 false
     */
    boolean filter(String key);
}
