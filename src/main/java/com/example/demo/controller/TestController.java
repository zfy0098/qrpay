package com.example.demo.controller;

import com.example.demo.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by a on 2017/6/1.
 */

@Controller
@RequestMapping(value = "/test")
@ResponseBody
public class TestController {

    @Autowired
    private TestService testService;

    @RequestMapping("/{id}")
    public Object test(@PathVariable String id){
        System.out.println(id);
        List<Map<String,Object>> list = testService.testlist(id);
        return list;
    }


    @RequestMapping("/merchant/{id}")
    public Object merchantlist(@PathVariable String id){
        return testService.merchantlist(new Object[]{id});
    }


}

