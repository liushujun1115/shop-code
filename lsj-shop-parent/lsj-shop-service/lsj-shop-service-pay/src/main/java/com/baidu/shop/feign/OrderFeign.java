package com.baidu.shop.feign;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "order-server",contextId = "OrderService")
public interface OrderFeign {  //这里不能直接集成OrderService 因为feign调用带有两个参数的post方法会报错

    @GetMapping(value = "order/getOrderByOrderId")
    Result<OrderInfo> getOrderByOrderId(@RequestParam Long orderId);
}
