package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.Car;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @ClassName CarService
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/19
 * @Version V1.0
 **/
@Api(tags = "购物车接口")
public interface CarService {

    @ApiOperation(value = "添加商品到购物车")
    @PostMapping(value = "car/addCar")
    Result<JSONObject> addCar(@RequestBody Car car);
}
