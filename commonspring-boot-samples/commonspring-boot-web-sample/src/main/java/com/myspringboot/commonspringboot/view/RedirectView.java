package com.myspringboot.commonspringboot.view;

/**
 * @author
 * @version 1.0
 * @since 2018/10/12 18:58
 */
public class RedirectView extends org.springframework.web.servlet.view.RedirectView {

    public RedirectView() {
    }

    public RedirectView(String url) {
        super(url);
    }

    public RedirectView(String url, boolean contextRelative) {
        super(url, contextRelative);
    }

    public RedirectView(String url, boolean contextRelative, boolean http10Compatible) {
        super(url, contextRelative, http10Compatible);
    }

    public RedirectView(String url, boolean contextRelative, boolean http10Compatible, boolean exposeModelAttributes) {
        super(url, contextRelative, http10Compatible, exposeModelAttributes);
    }
}
