package com.myspringboot.commonspringboot.controller;



import com.myspringboot.commonspringboot.view.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yzy
 * @version 1.0
 * @since 2018/9/10 9:47
 */
@Controller
@RequestMapping("/test")
public class HelloController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

//    @Autowired
//    private IpOwnerService ipOwnerService;
//
    //@Autowired
//    private Jdbc jdbc;

////    @RequestMapping("/hello/{name}")
//    public JsonView hello(@PathVariable String name, String requestUri) {
//        logger.info(requestUri + ": " + name);
//        logger.error(requestUri + ": " + name);
//        return new JsonView("Hello, " + name + ": " + requestUri);
//    }
//
////    @RequestMapping("/testError")
//    public JsonView testError() throws Exception {
//        throw new Exception("未知错误！");
//    }



    @RequestMapping("hello1.do")
    public JsonView hello2(String name, String requestUri) {
        logger.info(requestUri + ": " + name);
        logger.error(requestUri + ": " + name);
        return new JsonView("Hello1, " + name + ": " + requestUri);
    }

//    @GetMapping
//    public JsonView hello2(@PathVariable String name, String requestUri) {
//        logger.info(requestUri + ": " + name);
//        logger.error(requestUri + ": " + name);
//        return new JsonView("Hello2, " + name + ": " + requestUri);
//    }
//
//    @PostMapping
//    public JsonView hello3(@PathVariable String name, String requestUri) {
//        logger.info(requestUri + ": " + name);
//        logger.error(requestUri + ": " + name);
//        return new JsonView("3, " + name + ": " + requestUri);
//    }

    @RequestMapping("/requestBody")
    public JsonView<Object> requestBody(HttpServletRequest request, @RequestBody Map<String, Object> params) {

        Map<String, Object> paramsMap = new HashMap<>();
        fillRequestParameter(paramsMap, request, true, true);
        paramsMap.put("requestBody", params);

        return new JsonView<>(paramsMap);
    }



}

