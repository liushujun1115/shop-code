package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.service.PageService;
import com.baidu.shop.service.TemplateService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName TemplateServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/9/25
 * @Version V1.0
 **/
@RestController
public class TemplateServiceImpl extends BaseApiService implements TemplateService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private BrandFeign brandFeign;

    //注入静态化模版
    @Autowired
    private TemplateEngine templateEngine;

    //静态文件生成的路径
    @Value(value = "${mrshop.static.html.path}")
    private String staticHTMLPath;

    @Override
    public Result<JSONObject> createStaticHTMLTemplate(Integer spuId) {

        Map<String, Object> map = this.getPageInfoBySpuId(spuId);
        //创建模板引擎上下文
        Context context = new Context();
        //将所有准备的数据放到模板中
        context.setVariables(map);
        //创建文件 param1:文件路径 param2:文件名称
        File file = new File(staticHTMLPath, spuId + ".html");
        //构建文件输出流
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file, "UTF-8");
            //根据模板生成静态文件
            //param1:模板名称 params2:模板上下文[上下文中包含了需要填充的数据],文件输出流
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }finally {
            writer.close();
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initStaticHTMLTemplate() {

        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(new SpuDTO());
        if(spuInfo.getCode() == 200){
            List<SpuDTO> spuList = spuInfo.getData();

            spuList.stream().forEach(spu -> {
                createStaticHTMLTemplate(spu.getId());
            });
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> delHTMLBySpuId(Integer spuId) {
        File file = new File(staticHTMLPath + File.separator + spuId + ".html");
        if(!file.delete()){
            return this.setResultError("文件删除失败");
        }
        return this.setResultSuccess();
    }

    private Map<String, Object> getPageInfoBySpuId(Integer spuId) {
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
                Result<List<CategoryEntity>> cateResult = this.getCategory(spuInfo);
                map.put("categoryInfo",cateResult.getData());

                //品牌信息
                List<BrandEntity> brandList = this.getBrand(spuInfo.getBrandId());
                map.put("brandInfo",brandList.get(0));

                //通过spuId查询sku信息
                List<SkuDTO> skusList = this.getSkus(spuInfo.getId());
                map.put("skusList",skusList);

                //查询特有规格参数
                Map<Integer, String> specMap = this.getSpecialSpec(spuInfo.getCid3());
                map.put("specParamMap",specMap);

                //通过spuId查询spuDetail
                SpuDetailEntity spuDetailInfo = this.getSpuDetail(spuInfo.getId());
                map.put("spuDetailInfo",spuDetailInfo);

                //查询规格组和规格参数
                List<SpecGroupDTO> groupsAndParams = this.getGroupAndParam(spuInfo.getCid3());
                map.put("groupsAndParams",groupsAndParams);
            }
        }
        return map;
    }

    //查询分类
    private Result<List<CategoryEntity>> getCategory(SpuDTO spuInfo){
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
            return cateResult;
        }
        return null;
    }

    //查询品牌
    private List<BrandEntity> getBrand(Integer brandId){
        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setId(brandId);
        Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
        if (brandInfoResult.getCode() == HTTPStatus.OK){
            List<BrandEntity> brandList = brandInfoResult.getData().getList();
            if(brandList.size() == 1){
                return brandList;
            }
        }
        return null;
    }

    //查询sku
    private List<SkuDTO> getSkus(Integer spuId){
        Result<List<SkuDTO>> skusResult = goodsFeign.getSkuBySpuId(spuId);
        if (skusResult.getCode() == HTTPStatus.OK) {
            List<SkuDTO> skusList = skusResult.getData();
            return skusList;
        }
        return null;
    }

    //查询特有规格参数
    private Map<Integer, String> getSpecialSpec(Integer cid){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(cid);
        specParamDTO.setGeneric(false);
        Result<List<SpecParamEntity>> specParamInfoResult = specificationFeign.getSpecParamInfo(specParamDTO);
        if (specParamInfoResult.getCode() == HTTPStatus.OK){
            List<SpecParamEntity> specParamList = specParamInfoResult.getData();
            //将数据转换为Map方便页面操作
            Map<Integer, String> specMap = new HashMap<>();
            specParamList.stream().forEach(specParam -> {
                specMap.put(specParam.getId(),specParam.getName());
            });
            return specMap;
        }
        return null;
    }

    //通过spuId查询spuDetail
    private SpuDetailEntity getSpuDetail(Integer spuId){
        Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spuId);
        if (spuDetailResult.getCode() == HTTPStatus.OK) {
            SpuDetailEntity spuDetailInfo = spuDetailResult.getData();
            return spuDetailInfo;
        }
        return null;
    }

    //查询规格组和规格参数
    private List<SpecGroupDTO> getGroupAndParam(Integer cid){
        SpecGroupDTO specGroupDTO = new SpecGroupDTO();
        specGroupDTO.setCid(cid);
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
            return groupsAndParams;
        }
        return null;
    }
}
