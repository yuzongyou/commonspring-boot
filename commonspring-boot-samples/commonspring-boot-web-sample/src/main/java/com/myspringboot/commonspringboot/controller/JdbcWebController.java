package com.myspringboot.commonspringboot.controller;


import com.github.yuzongyou.jdbc.Jdbc;
import com.myspringboot.commonspringboot.view.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jdbcWeb")
public class JdbcWebController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(JdbcWebController.class);

    @Autowired
    protected Jdbc commonJdbc;

    @RequestMapping("select1.do")
    public JsonView hello2(String name, String requestUri) {
        List<Map<String, Object>> list= commonJdbc.getJdbcTemplate().queryForList("select *  from app_msg_list");
        for(Map<String, Object> map:list){
            System.out.println("map："+map.toString());
        }
        return new JsonView("Hello1, " + name + ": " + requestUri);
    }


}
