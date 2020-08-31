package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dot.BrandDOT;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

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

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDOT brandDOT) {

        //分页
        PageHelper.startPage(brandDOT.getPage(), brandDOT.getRows());

        //排序
        Example example = new Example(BrandEntity.class);
        if(StringUtil.isNotEmpty(brandDOT.getSort()))
            example.setOrderByClause(brandDOT.getOrderByClause());
        if(StringUtil.isNotEmpty(brandDOT.getName()))
            example.createCriteria().andLike("name","%"+brandDOT.getName()+"%");


        List<BrandEntity> list = brandMapper.selectByExample(example);

        PageInfo<BrandEntity> info = new PageInfo<BrandEntity>(list);

        return this.setResultSuccess(info);
    }

    @Override
    @Transactional
    public Result<JsonObject> saveBrand(BrandDOT brandDOT) {
        brandMapper.insertSelective(BaiduBeanUtil.copyProperties(brandDOT,BrandEntity.class));
        return this.setResultSuccess();
    }

}
