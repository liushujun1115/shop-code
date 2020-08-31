package com.baidu.shop.entity;

import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName CategoryEntity
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/8/27
 * @Version V1.0
 **/
@Data
@Table(name = "tb_category")
@ApiModel(value = "分类实体类")
public class CategoryEntity {

    @Id
    @ApiModelProperty(value = "分类主键",example = "1")
    //此处要引出来一个分组的概念,就是当前参数校验属于哪个组
    //有的操作不需要验证此参数就比如说新增就不需要校验id,但是修改需要
    @NotNull(message = "id不能为空",groups = {MingruiOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "分类名称")
    //新增和修改都需要此参数
    @NotEmpty(message = "分类名称不能为空",groups = {MingruiOperation.Add.class,MingruiOperation.Update.class})
    private String name;

    @ApiModelProperty(value = "父级名称",example = "1")
    @NotNull(message = "父级id不能为null",groups = {MingruiOperation.Add.class})
    private Integer parentId;

    @ApiModelProperty(value = "是否为父级节点",example = "1")
    @NotNull(message = "是否为父级节点不能为null",groups = {MingruiOperation.Add.class})
    private Integer isParent;

    @ApiModelProperty(value = "排序",example = "1")
    @NotNull(message = "排序不能为null",groups = {MingruiOperation.Add.class})
    private Integer sort;
}
