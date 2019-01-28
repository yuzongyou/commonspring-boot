package com.myspringboot.commonspringboot.view;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/10/26 10:25
 */
public class StatusJsonView<T> extends JsonView<T> {

    public StatusJsonView() {
    }

    public StatusJsonView(int code, String message, T data) {
        super(code, message, data);
    }

    public StatusJsonView(int code, String message, T data, boolean needXssCheck) {
        super(code, message, data, needXssCheck);
    }

    public StatusJsonView(int code, String message) {
        super(code, message);
    }

    public StatusJsonView(int code, String message, boolean needXssCheck) {
        super(code, message, needXssCheck);
    }

    public StatusJsonView(T data) {
        super(data);
    }

    public StatusJsonView(T data, boolean needXssCheck) {
        super(data, needXssCheck);
    }

    @Override
    public String getStatusCodeName() {
        return DEFAULT_AJAX_STATUS_CODE_NAME;
    }
}
