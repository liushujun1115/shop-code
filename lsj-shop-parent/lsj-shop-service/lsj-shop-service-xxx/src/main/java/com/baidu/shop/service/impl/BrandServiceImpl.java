package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        //分页
        if(ObjectUtil.isNotNull(brandDTO.getPage()) && ObjectUtil.isNotNull(brandDTO.getRows()))
            PageHelper.startPage(brandDTO.getPage(), brandDTO.getRows());

        //排序
        Example example = new Example(BrandEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtil.isNotEmpty(brandDTO.getName()))
            criteria.andLike("name","%"+brandDTO.getName()+"%");
        if(ObjectUtil.isNotNull(brandDTO.getId()))
            criteria.andEqualTo("id",brandDTO.getId());
        if(StringUtil.isNotEmpty(brandDTO.getSort()))
            example.setOrderByClause(brandDTO.getOrderByClause());


        //查询
        List<BrandEntity> list = brandMapper.selectByExample(example);

        //数据封装
        PageInfo<BrandEntity> info = new PageInfo<BrandEntity>(list);

        return this.setResultSuccess(info);
    }

    @Override
    @Transactional
    public Result<JsonObject> saveBrand(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

//        获取到品牌名称
//        获取到品牌名称第一个字符
//        将第一个字符转换为pinyin
//        获取拼音的首字母
//        统一转为大写

//        String name = brandEntity.getName();
//        char c = name.charAt(0);
//        String upperCase = PinyinUtil.getUpperCase(String.valueOf(c), PinyinUtil.TO_FIRST_CHAR_PINYIN);
//        brandEntity.setLetter(upperCase.charAt(0));

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0)), PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        brandMapper.insertSelective(brandEntity);

        this.insertCategoryAndBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> editBrand(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0)), PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        //通过brandId删除中间表数据
        this.deleteCategoryAndBrand(brandEntity.getId());

        //新增
        this.insertCategoryAndBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    @Override
    public Result<JsonObject> deleteBrand(Integer id) {
        brandMapper.deleteByPrimaryKey(id);
        this.deleteCategoryAndBrand(id);
        return this.setResultSuccess();
    }

    //通过brandId删除中间表数据
    private void deleteCategoryAndBrand(Integer id){

        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);
    }

    //新增关系数据
    //@Transactional
    private void insertCategoryAndBrand(BrandDTO brandDTO,BrandEntity brandEntity){

        if(brandDTO.getCategory().contains(",")){

//            通过split方法分割字符串得到Array
//            Arrays.asList将Array转换为List
//            使用JDK1,8的stream
//            使用map函数返回一个新的数据
//            collect 转换集合类型Stream<T>
//            Collectors.toList())将集合转换为List类型

//            String[] cidArr = brandDOT.getCategory().split(",");
//            List<String> list = Arrays.asList(cidArr);
//            ArrayList<CategoryBrandEntity> categoryBrandEntities = new ArrayList<>();
//            list.stream().map(cid -> {

            List<CategoryBrandEntity> categoryBrandEntities = Arrays.asList(brandDTO.getCategory().split(",")).stream().map(cid -> {

                CategoryBrandEntity entity = new CategoryBrandEntity();
                entity.setCategoryId(StringUtil.toInteger(cid));
                entity.setBrandId(brandEntity.getId());
                return entity;
            }).collect(Collectors.toList());
            //批量新增
            categoryBrandMapper.insertList(categoryBrandEntities);
        }else{
            CategoryBrandEntity entity = new CategoryBrandEntity();
            entity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            entity.setBrandId(brandEntity.getId());
            categoryBrandMapper.insertSelective(entity);
        }
    }

}
