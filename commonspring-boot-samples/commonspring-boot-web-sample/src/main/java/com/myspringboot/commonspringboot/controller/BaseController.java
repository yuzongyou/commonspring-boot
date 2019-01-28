package com.myspringboot.commonspringboot.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/11/14 11:19
 */
public class BaseController {

    protected void fillRequestParameter(Map<String, Object> paramsMap, HttpServletRequest request, boolean fillCookie, boolean fillHeader) {

        if (null == paramsMap) {
            paramsMap = new HashMap<>();
        }

        // 填充请求参数
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getParameter(name);
            paramsMap.put(name, value);
        }

        if (fillCookie) {
            // 填充 Cookie
            Map<String, String> cookieMap = new HashMap<>();
            Cookie[] cookies = request.getCookies();
            if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    cookieMap.put(cookie.getName(), cookie.getValue());
                }
            }
            paramsMap.put("cookies", cookieMap);
        }

        if (fillHeader) {
            // 填充headers
            Map<String, String> headerMap = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                headerMap.put(name, value);
            }
            paramsMap.put("headers", headerMap);
        }

    }
}
