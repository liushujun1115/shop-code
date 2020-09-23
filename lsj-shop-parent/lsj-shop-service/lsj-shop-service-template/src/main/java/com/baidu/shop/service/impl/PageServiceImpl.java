package com.baidu.shop.service.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.service.PageService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/9/23
 * @Version V1.0
 **/
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Override
    public Map<String, Object> getPageInfoBySpuId(Integer spuId) {
        Map<String, Object> map = new HashMap<>();
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);
        if(spuInfoResult.getCode() == 200){
            if (spuInfoResult.getData().size() == 1){
                //spu信息
                SpuDTO spuInfo = spuInfoResult.getData().get(0);
                map.put("spuInfo",spuInfo);
                //分类信息
                Result<List<CategoryEntity>> cateResult = categoryFeign.getCategoryByIdList(
                        String.join(
                                ","
                                , Arrays.asList(
                                        spuInfo.getCid1() + ""
                                        , spuInfo.getCid2() + ""
                                        , spuInfo.getCid3() + "")
                        )
                );
                if(cateResult.getCode() == 200){
                    map.put("categoryInfo",cateResult.getData());
                }
                //品牌信息
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getBrandId());
                Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
                if (brandInfoResult.getCode() == 200){
                    List<BrandEntity> brandList = brandInfoResult.getData().getList();
                    if(brandList.size() == 1){
                        map.put("brandInfo",brandList.get(0));
                    }
                }
                //分类信息

            }
        }
        return map;
    }
}
