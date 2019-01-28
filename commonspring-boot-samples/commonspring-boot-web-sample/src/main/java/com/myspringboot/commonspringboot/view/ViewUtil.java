package com.myspringboot.commonspringboot.view;


import com.alibaba.druid.support.json.JSONUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yzy
 */
public abstract class ViewUtil {

    private ViewUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Javascript 变量名称正则表达式
     */
    private static final String JAVASCRIPT_VAR_REGEX = "(?i)^[a-z][a-zA-Z0-9_\\.]*$";

    /**
     * 搜索指定参数名称的值
     *
     * @param request 请求
     * @param vars    参数名称劣币哦啊
     * @return 返回第一个不为空的值， 如果没有则返回null
     */
    private static String lookupByVars(HttpServletRequest request, String[] vars) {
        if (request == null || null == vars || vars.length < 1) {
            return null;
        }

        for (String var : vars) {
            String value = request.getParameter(var);
            if (!StringUtils.isEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 搜索 jsonp 回调函数值, 回调函数的名称可以在 application-[env].properties 文件中配置变量 view.jsonp.callback.vars， 中间使用英文逗号分割
     * <p>
     * 默认是 callback
     *
     * @param request 请求
     * @return 如果有的话就返回，没有就返回null， 如果存在但是名称不符合规范则返回null
     */
    public static String lookupJsonpCallback(HttpServletRequest request) {

    /*    String callback = lookupByVars(request, WebContext.getJsonpCallbackVars());

        if (!StringUtils.isEmpty(callback) && callback.trim().matches(JAVASCRIPT_VAR_REGEX)) {
            return callback.trim();
        }*/

        return null;
    }

    /**
     * 获取请求的所有请求变量名称
     *
     * @param request 请求
     * @return 返回一个非null集合
     */
    private static Set<String> getParamNames(HttpServletRequest request) {
        Set<String> names = new HashSet<>();

        if (null == request) {
            return names;
        }

        Enumeration<String> enumeration = request.getParameterNames();

        while (enumeration.hasMoreElements()) {
            names.add(enumeration.nextElement());
        }

        return names;
    }

    /**
     * 获取时间格式匹配规则，请求变量名称默认为 dateFormat，
     * <p>
     * 可在 application-[env].properties 文件中配置变量 view.json.dateFormat.vars， 中间使用英文逗号分割
     *
     * @param request 请求
     * @return 如果有的话就返回，没有就返回null， 如果存在但是名称不符合规范则抛出异常
     */
    public static String lookupDateFormatPattern(HttpServletRequest request) {
       /* String[] dateFormatVars = WebContext.getDateFormatVars();
        if (request == null || null == dateFormatVars || dateFormatVars.length < 1) {
            return null;
        }*/

        Set<String> paramNames = getParamNames(request);

        String defaultDateFormat = "yyyy-MM-dd HH:mm:ss";

    /*    for (String var : dateFormatVars) {
            if (paramNames.contains(var)) {
                String value = request.getParameter(var);
                if (StringUtil.isBlank(value)) {
                    return defaultDateFormat;
                }
                return value;
            }
        }*/
        return null;
    }

    /**
     * 获取 javascript 输出变量名称， 名称可以在 application-[env].properties 文件中配置变量 view.javascript.vars， 中间使用英文逗号分割
     * <p>
     * 默认是 var
     *
     * @param request 请求
     * @return 返回变量名称，如果
     */
    public static String lookupJavascriptVar(HttpServletRequest request) {
      /*  String javascriptVar = lookupByVars(request, WebContext.getJavascriptVars());

        if (StringUtil.isNotBlank(javascriptVar) && javascriptVar.trim().matches(JAVASCRIPT_VAR_REGEX)) {
            return javascriptVar.trim();
        }
*/
        return null;
    }

    /**
     * 获取 ajax 数据结果字符串
     *
     * @param data    数据对象
     * @param request 请求
     * @return 返回一个字符串
     */
    public static String getAjaxOutputString(Object data, HttpServletRequest request) {

        if (data instanceof CharSequence) {
            return getAjaxOutputText((String) data, request);
        }

        String dateFormat = lookupDateFormatPattern(request);

        String body;

        if (StringUtils.isEmpty(dateFormat)) {

            body =  JSONUtils.toJSONString(data);
        } else {
            //body = JsonUtils.toJsonWithDataFormat(data, dateFormat);
            body =  JSONUtils.toJSONString(data);
        }

        String jsonpCallback = lookupJsonpCallback(request);

        if (!StringUtils.isEmpty(jsonpCallback)) {
            return jsonpCallback + "(" + body + ");";
        }

        String javascriptVar = lookupJavascriptVar(request);

        if (!StringUtils.isEmpty(javascriptVar)) {
            return "var " + javascriptVar + "=" + body + ";";
        }

        return body;
    }

    /**
     * 获取 Ajax 字符串文本
     *
     * @param text    文本字符串
     * @param request 请求
     * @return 返回 ajax 输出文本
     */
    public static String getAjaxOutputText(String text, HttpServletRequest request) {

        String jsonpCallback = lookupJsonpCallback(request);

        if (!StringUtils.isEmpty(jsonpCallback)) {
            return jsonpCallback + "(" + JSONUtils.toJSONString(text) + ");";
        }

        String javascriptVar = lookupJavascriptVar(request);

        if (!StringUtils.isEmpty(javascriptVar)) {
            return "var " + javascriptVar + "=" + JSONUtils.toJSONString(text) + ";";
        }

        return text == null ? "null" : text;
    }
}
