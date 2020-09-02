package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dot.BrandDOT;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "品牌接口")
public interface BrandService {

    @ApiOperation(value = "查询品牌列表")
    @GetMapping(value = "brand/getBrandInfo")
    Result<PageInfo<BrandEntity>> getBrandInfo(BrandDOT brandDOT);

    @ApiOperation(value = "新增品牌")
    @PostMapping(value = "brand/save")
    Result<JsonObject> saveBrand(@RequestBody @Validated({MingruiOperation.Add.class}) BrandDOT brandDOT);

    @ApiOperation(value = "修改品牌")
    @PutMapping(value = "brand/save")
    Result<JsonObject> editBrand(@RequestBody @Validated({MingruiOperation.Update.class}) BrandDOT brandDOT);

    @ApiOperation(value = "删除品牌")
    @DeleteMapping(value = "brand/delete")
    Result<JsonObject> deleteBrand(Integer id);
}
