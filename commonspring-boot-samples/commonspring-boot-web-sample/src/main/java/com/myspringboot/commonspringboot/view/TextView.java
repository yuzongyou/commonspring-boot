package com.myspringboot.commonspringboot.view;

import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author yzy
 */
public class TextView extends AbstractTextView {

    private String text;

    /** 是否需要进行 XSS 检查 */
    private boolean needXssCheck = false;

    public TextView() {
    }

    public TextView(String text) {
        this.text = text;
    }

    public TextView(String text, boolean needXssCheck) {
        this.text = text;
        this.needXssCheck = needXssCheck;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isNeedXssCheck() {
        return needXssCheck;
    }

    public void setNeedXssCheck(boolean needXssCheck) {
        this.needXssCheck = needXssCheck;
    }

    @Override
    public String getContentEncoding() {
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8";
    }

    @Override
    public String getBody(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

        if (needXssCheck) {
            checkXssAndFilter();
        }

        return ViewUtil.getAjaxOutputString(text, request);
    }

    private void checkXssAndFilter() {
        // Do nothing
    }
}
