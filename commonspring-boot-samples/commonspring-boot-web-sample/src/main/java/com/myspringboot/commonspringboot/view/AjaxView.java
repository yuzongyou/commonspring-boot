package com.myspringboot.commonspringboot.view;

import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yzy
 */
public class AjaxView<T> extends AbstractTextView {

    public static final int DEFAULT_CODE = 200;

    public static final boolean DEFAULT_NEED_XSS_CHECK = false;

    public static final String DEFAULT_AJAX_STATUS_CODE_NAME = "status";

    private static String ajaxStatusCodeName = DEFAULT_AJAX_STATUS_CODE_NAME;

    public static String getAjaxStatusCodeName() {
        return ajaxStatusCodeName;
    }

    public static void setAjaxStatusCodeName(String ajaxStatusCodeName) {
        AjaxView.ajaxStatusCodeName = ajaxStatusCodeName;
    }

    public String getStatusCodeName() {
        return getAjaxStatusCodeName();
    }

    /** 业务响应代码 */
    private int code = DEFAULT_CODE;

    /** 业务响应信息 */
    private String message;

    /** 业务相应数据 */
    private T data;

    /** 是否需要进行 XSS 检查 */
    private boolean needXssCheck = DEFAULT_NEED_XSS_CHECK;

    public AjaxView() {
    }

    public AjaxView(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public AjaxView(int code, String message, T data, boolean needXssCheck) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.needXssCheck = needXssCheck;
    }

    public AjaxView(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public AjaxView(int code, String message, boolean needXssCheck) {
        this.code = code;
        this.message = message;
        this.needXssCheck = needXssCheck;
    }

    public AjaxView(T data) {
        this.data = data;
    }

    public AjaxView(T data, boolean needXssCheck) {
        this.data = data;
        this.needXssCheck = needXssCheck;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
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
            checkDataXssAndFilter();
            super.setXssChecked(true);
        }

        Object dataResult = buildDataObject();

        return ViewUtil.getAjaxOutputString(dataResult, request);
    }

    /**
     * 构造数据对象
     *
     * @return 返回数据对象
     */
    protected Object buildDataObject() {

        Map<String, Object> dataMap = new HashMap<>(3);

        dataMap.put(getStatusCodeName(), code);
        dataMap.put("message", message);
        dataMap.put("data", data);

        return dataMap;
    }

    private void checkDataXssAndFilter() {
    }
}
