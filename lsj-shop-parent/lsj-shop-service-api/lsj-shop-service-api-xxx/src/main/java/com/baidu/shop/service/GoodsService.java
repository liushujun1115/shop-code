package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "获取spu列表信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<PageInfo<SpuEntity>> getSpuInfo(SpuDTO spuDTO);

    @ApiOperation(value = "新增商品信息")
    @PostMapping(value = "goods/saveSpu")
    Result<JSONObject> saveSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "通过spuId查询spuDetali信息回显")
    @GetMapping(value = "goods/getDetailBySpuId")
    Result<SpuDetailEntity> getDetailBySpuId(Integer spuId);

    @ApiOperation(value = "通过spuId查询sku信息回显")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(Integer spuId);

    @ApiOperation(value = "修改商品信息")
    @PutMapping(value = "goods/saveSpu")
    Result<JSONObject> editSpu(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "删除商品信息")
    @DeleteMapping(value = "goods/removeSpu")
    Result<JsonObject> removeSpu(Integer spuId);

//    @ApiOperation(value = "修改上下架状态")
//    @GetMapping(value = "goods/upOrDown")
//    Result<JSONObject> upOrDown(Integer id,Integer saleable);

    @ApiOperation(value = "修改上下架状态")
    @PutMapping(value = "goods/upOrDown")
    Result<JSONObject> upOrDown(@RequestBody SpuDTO spuDTO);
}
