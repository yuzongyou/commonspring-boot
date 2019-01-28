package com.myspringboot.commonspringboot.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 抽象基础文本视图，提供基本的一些功能，如 xss 支持
 *
 * @author yzy
 */
public abstract class AbstractTextView extends ModelAndView {

    /**
     * 日志
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 是否已经进行了 XSS 检查
     */
    private boolean isXssChecked = false;

    public boolean isXssChecked() {
        return isXssChecked;
    }

    protected void setXssChecked(boolean isXssChecked) {
        this.isXssChecked = isXssChecked;
    }

    public AbstractTextView() {
        this.setView(new SecurityAbstractUrlBasedView(this));
    }

    /**
     * 获取输出的内容编码， 默认是 UTF-8
     *
     * @return 返回内容编码
     */
    public abstract String getContentEncoding();

    /**
     * 输出的内容类型
     *
     * @return 返回内容类型
     */
    public abstract String getContentType();

    /**
     * 获取要输出的内容
     *
     * @param model    模型
     * @param request  请求
     * @param response 响应
     * @return 返回 json 内容
     */
    public abstract String getBody(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response);
}
