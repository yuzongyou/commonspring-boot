package com.myspringboot.commonspringboot;


import org.springframework.util.StringUtils;

/**
 * @author yzy
 */
public class RegexKeyFilter implements KeyFilter {

    private final String regex;

    public RegexKeyFilter(String regex) {
        this.regex = regex;
    }

    @Override
    public boolean filter(String key) {
        return StringUtils.isEmpty(key) || key.startsWith(regex);
    }
}
