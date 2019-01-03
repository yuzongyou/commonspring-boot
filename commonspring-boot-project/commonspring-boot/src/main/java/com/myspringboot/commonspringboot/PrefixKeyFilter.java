package com.myspringboot.commonspringboot;


import org.springframework.util.StringUtils;

/**
 * @author yzy
 */
public class PrefixKeyFilter implements KeyFilter {

    private final String prefix;

    public PrefixKeyFilter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean filter(String key) {
        return StringUtils.isEmpty(key) || key.matches(prefix);
    }
}
