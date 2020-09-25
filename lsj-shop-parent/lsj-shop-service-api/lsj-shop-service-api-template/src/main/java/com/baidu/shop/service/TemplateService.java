package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

@Api(tags = "template接口")
public interface TemplateService {

    @ApiOperation(value = "创建静态化HTML模块")
    @GetMapping(value = "template/createStaticHTMLTemplate")
    Result<JSONObject> createStaticHTMLTemplate(Integer spuId);

    @ApiOperation(value = "初始化静态化HTML模块")
    @GetMapping(value = "template/initStaticHTMLTemplate")
    Result<JSONObject> initStaticHTMLTemplate();

}
