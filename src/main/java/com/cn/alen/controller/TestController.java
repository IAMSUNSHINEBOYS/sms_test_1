package com.cn.alen.controller;

import com.cn.alen.util.LogUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping()
public class TestController {
    @RequestMapping("/hello")
    @ResponseBody
    public String helle(){
        LogUtils.logInfo("hello");
        LogUtils.logInfo("修改测试提交GitHub");
        return "Hello ！！！";
    }

}
