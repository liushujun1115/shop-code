package com.baidu.shop.service.impl;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.PageService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/9/23
 * @Version V1.0
 **/
@Service
public class PageServiceImpl implements PageService {

//    @Autowired
    private GoodsFeign goodsFeign;

//    @Autowired
    private BrandFeign brandFeign;

//    @Autowired
    private CategoryFeign categoryFeign;

//    @Autowired
    private SpecificationFeign specificationFeign;

    @Override
    public Map<String, Object> getPageInfoBySpuId(Integer spuId) {
        Map<String, Object> map = new HashMap<>();
        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuInfoResult = goodsFeign.getSpuInfo(spuDTO);
        if(spuInfoResult.getCode() == HTTPStatus.OK){
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
                if(cateResult.getCode() == HTTPStatus.OK){
                    map.put("categoryInfo",cateResult.getData());
                }
                //品牌信息
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getBrandId());
                Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
                if (brandInfoResult.getCode() == HTTPStatus.OK){
                    List<BrandEntity> brandList = brandInfoResult.getData().getList();
                    if(brandList.size() == 1){
                        map.put("brandInfo",brandList.get(0));
                    }
                }
                //通过spuId查询sku信息
                Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuInfo.getId());
                if (skusResult.getCode() == HTTPStatus.OK) {
                    List<SkuDTO> skusList = skusResult.getData();
                    map.put("skusList",skusList);
                }
                //查询特有规格参数
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getCid3());
                specParamDTO.setGeneric(false);
                Result<List<SpecParamEntity>> specParamInfoResult = specificationFeign.getSpecParamInfo(specParamDTO);
                if (specParamInfoResult.getCode() == HTTPStatus.OK){
                    List<SpecParamEntity> specParamList = specParamInfoResult.getData();
                    //将数据转换为Map方便页面操作
                    Map<Integer, String> specMap = new HashMap<>();
                    specParamList.stream().forEach(specParam -> {
                        specMap.put(specParam.getId(),specParam.getName());
                    });
                    map.put("specParamMap",specMap);
                }
                //通过spuId查询spuDetail
                Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spuInfo.getId());
                if (spuDetailResult.getCode() == HTTPStatus.OK) {
                    SpuDetailEntity spuDetailInfo = spuDetailResult.getData();
                    map.put("spuDetailInfo",spuDetailInfo);
                }
                //查询规格组和规格参数
                SpecGroupDTO specGroupDTO = new SpecGroupDTO();
                specGroupDTO.setCid(spuInfo.getCid3());
                Result<List<SpecGroupEntity>> specGroupResult = specificationFeign.getSpecGroupInfo(specGroupDTO);

                if (specGroupResult.getCode() == HTTPStatus.OK) {
                    List<SpecGroupEntity> specGroupInfo = specGroupResult.getData();//规格组

                    List<SpecGroupDTO> groupsAndParams = specGroupInfo.stream().map(specGroup -> {

                        SpecGroupDTO sgd = BaiduBeanUtil.copyProperties(specGroup, SpecGroupDTO.class);
                        //规格参数--通用参数
                        SpecParamDTO paramDTO = new SpecParamDTO();
                        paramDTO.setGroupId(specGroup.getId());
                        paramDTO.setGeneric(true);

                        Result<List<SpecParamEntity>> specParamInfo = specificationFeign.getSpecParamInfo(paramDTO);
                        if (specParamInfo.getCode() == HTTPStatus.OK) {
                            sgd.setSpecParams(specParamInfo.getData());
                        }
                        return sgd;
                    }).collect(Collectors.toList());
                    map.put("groupsAndParams",groupsAndParams);
                }
            }
        }
        return map;
    }
}
