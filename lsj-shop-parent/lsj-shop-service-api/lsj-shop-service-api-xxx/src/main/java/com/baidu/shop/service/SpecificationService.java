package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "规格接口")
public interface SpecificationService {

    @ApiOperation(value = "查询规格组")
    @GetMapping(value = "specgroup/getSpecGroupInfo")
    Result<List<SpecGroupEntity>> getSpecGroupInfo(SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "新增规格组")
    @PostMapping(value = "specgroup/saveSpecGroup")
    Result<JsonObject> saveSpecGroup(@Validated({MingruiOperation.Add.class}) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "修改规格组")
    @PutMapping(value = "specgroup/saveSpecGroup")
    Result<JsonObject> editSpecGroup(@Validated({MingruiOperation.Update.class}) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "删除规格组")
    @DeleteMapping(value = "specgroup/deleteSpecGroup")
    Result<JsonObject> removeSpecGroup(Integer id);
}
