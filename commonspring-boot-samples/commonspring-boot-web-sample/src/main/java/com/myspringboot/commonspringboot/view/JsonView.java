package com.myspringboot.commonspringboot.view;

import org.springframework.http.MediaType;

/**
 * JsonView Json 视图
 *
 * @author yzy
 */
public class JsonView<T> extends AjaxView<T> {

    public JsonView() {
    }

    public JsonView(int code, String message, T data) {
        super(code, message, data);
    }

    public JsonView(int code, String message, T data, boolean needXssCheck) {
        super(code, message, data, needXssCheck);
    }

    public JsonView(int code, String message) {
        super(code, message);
    }

    public JsonView(int code, String message, boolean needXssCheck) {
        super(code, message, needXssCheck);
    }

    public JsonView(T data) {
        super(data);
    }

    public JsonView(T data, boolean needXssCheck) {
        super(data, needXssCheck);
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_UTF8_VALUE;
    }
}
