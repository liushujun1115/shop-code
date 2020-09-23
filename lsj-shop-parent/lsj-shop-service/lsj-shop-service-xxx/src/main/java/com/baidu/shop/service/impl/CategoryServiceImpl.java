package com.baidu.shop.service.impl;

import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.service.CategoryService;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);

        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JsonObject> saveCategory(CategoryEntity categoryEntity) {
        //通过新增节点的父id 将父节点的isparent状态改为1
        CategoryEntity parentIdEntity = new CategoryEntity();
        parentIdEntity.setId(categoryEntity.getParentId());
        parentIdEntity.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(parentIdEntity);

        categoryMapper.insertSelective(categoryEntity);
        return this.setResultSuccess("新增成功");
    }

    @Transactional
    @Override
    public Result<JsonObject> editCategory(CategoryEntity categoryEntity) {
        categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        return this.setResultSuccess("修改成功");
    }

    @Transactional
    @Override
    public Result<JsonObject> deleteCategory(Integer id) {

        //验证传入的id是否有效
        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if(categoryEntity == null){
            return this.setResultError("当前id不存在");
        }
        //判断当前节点是否为父节点
        if(categoryEntity.getIsParent() == 1){
            return this.setResultError("当前节点为父节点,不能删除");
        }

        //如果分类被品牌绑定,不能删除
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> list1 = categoryBrandMapper.selectByExample(example1);
        if(list1.size() > 0) return this.setResultError("当前分类被品牌绑定,不能删除");


        Example example2 = new Example(SpecGroupEntity.class);
        example2.createCriteria().andEqualTo("cid",id);
        List<SpecGroupEntity> list2 = specGroupMapper.selectByExample(example2);
        if(list2.size() > 0) return this.setResultError("当前分类被规格组绑定,不能删除");

        //构建条件查询 通过当前被删除节点的父级节点parentid查询数据
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);
        //如果查询出来的数据只有一条
        if(list.size() == 1){
            //将父节点isparent状态改为0
            CategoryEntity entity = new CategoryEntity();
            entity.setId(categoryEntity.getParentId());
            entity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(entity);
        }

        categoryMapper.deleteByPrimaryKey(id);

        return this.setResultSuccess("删除成功");
    }

    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {
        List<CategoryEntity> list = categoryMapper.getByBrandId(brandId);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<List<CategoryEntity>> getCategoryByIdList(String cidsStr) {
//        String[] split = cidsStr.split(",");
//        List<String> strings = Arrays.asList(split);
//        strings.stream().map()
        List<Integer> cidList = Arrays.asList(cidsStr.split(","))
                .stream().map(cidStr -> Integer.parseInt(cidStr)).collect(Collectors.toList());
        List<CategoryEntity> list = categoryMapper.selectByIdList(cidList);
        return this.setResultSuccess(list);
    }


}
