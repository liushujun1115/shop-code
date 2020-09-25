package com.baidu.shop.dto;

import com.baidu.shop.base.BaseDTO;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @ClassName SpecGroupDTO
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/9/3
 * @Version V1.0
 **/
@ApiModel(value = "规格组数据传输DTO")
@Data
public class SpecGroupDTO extends BaseDTO {

    @ApiModelProperty(name = "主键",example = "1")
    @NotNull(message = "规格主键不能为空",groups = {MingruiOperation.Update.class})
    private Integer id;

    @ApiModelProperty(name = "类型id",example = "1")
    @NotNull(message = "商品分类id，一个分类下有多个规格组",groups = {MingruiOperation.Add.class})
    private Integer cid;

    @ApiModelProperty(name = "规格组名称")
    @NotEmpty(message = "规格组名称不能为空",groups = {MingruiOperation.Add.class})
    private String name;

    @ApiModelProperty(hidden = true)
    private List<SpecParamEntity> specParams;

}
