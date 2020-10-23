package com.baidu.shop.business;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.OrderDTO;
import com.baidu.shop.dto.OrderInfo;
import com.baidu.shop.entity.OrderEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "订单接口")
public interface OrderService {

    @ApiOperation(value = "创建订单")
    @PostMapping(value = "order/createOrder")
    Result<String> createOrder(@RequestBody OrderDTO orderDTO, @CookieValue(name = "MRSHOP_TOKEN") String token);

    @ApiOperation(value = "通过订单id获取订单信息")
    @GetMapping(value = "order/getOrderByOrderId")
    Result<OrderInfo> getOrderByOrderId(@RequestParam Long orderId);
}
