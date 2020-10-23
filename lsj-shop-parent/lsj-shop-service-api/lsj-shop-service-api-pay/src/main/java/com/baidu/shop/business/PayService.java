package com.baidu.shop.business;

import com.baidu.shop.dto.PayInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "支付接口")
public interface PayService {


    @ApiOperation(value = "请求支付")
    @GetMapping(value = "pay/requestPay")//请求支付
    void requestPay(HttpServletResponse response,PayInfoDTO payInfoDTO,@CookieValue(value = "MRSHOP_TOKEN") String token);

    @ApiOperation(value = "通知接口")
    @GetMapping(value = "pay/returnNotify")
    void returnNotify(HttpServletRequest request);

    @ApiOperation(value = "跳转成功页面接口")
    @GetMapping(value = "pay/returnHTML")//跳转成功页面接口
    void returnHTML(HttpServletRequest request,HttpServletResponse response);
}
