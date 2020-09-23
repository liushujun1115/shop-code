package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
@Slf4j
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private BrandFeign brandFeign;

    @Autowired
    private CategoryFeign categoryFeign;
    
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Result<JSONObject> clearGoodsEsData() {

        IndexOperations index = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(index.exists()){
            index.delete();
            log.info("索引删除成功");
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> infoGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(!indexOperations.exists()){
            indexOperations.create();
            log.info("索引创建成功");
            indexOperations.createMapping(GoodsDoc.class);
            log.info("映射创建成功");
        }
        //查询数据
        List<GoodsDoc> goodsDocs = this.esGoodsInfo();
        elasticsearchRestTemplate.save(goodsDocs);
        log.info("新增数据成功");
        return this.setResultSuccess();
    }

    @Override
    public GoodsResponse search(String search,Integer page) {

        if(StringUtil.isEmpty(search)) throw new RuntimeException("查询内容不能为null");
        //searchHits 得到查询结果
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(this.getSearchQueryBuilder(search,page).build(), GoodsDoc.class);
        //构建高亮
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());
        //搜索
        List<GoodsDoc> goodsList = highLightHit.stream().map(searchHit -> searchHit.getContent() ).collect(Collectors.toList());
        //总条数 & 总页数
        long total = searchHits.getTotalHits();
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        //聚合 获得品牌,分类集合
        Aggregations aggregations = searchHits.getAggregations();
        List<BrandEntity> brandList = this.getBrandList(aggregations);
        Map<Integer, List<CategoryEntity>> map = this.getCategoryList(aggregations);

        List<CategoryEntity> categoryList = null;
        Integer hotCid = 0;//热度最高的cid

        for(Map.Entry<Integer, List<CategoryEntity>> mapEntry : map.entrySet()){
            hotCid = mapEntry.getKey();
            categoryList = mapEntry.getValue();
        }

        Map<String, List<String>> specParamValueMap = this.getSpecParam(hotCid, search);

        return new GoodsResponse(total,totalPage,brandList,categoryList,goodsList,specParamValueMap);
    }

    //聚合查询规格参数
    private Map<String, List<String>> getSpecParam(Integer hotCid,String search){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);

        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParamInfo(specParamDTO);

        if(specParamResult.getCode() == 200){
            List<SpecParamEntity> specParamList = specParamResult.getData();
            //聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search, "brandName", "categoryName", "title"));
            queryBuilder.withPageable(PageRequest.of(0,1));//分页必须要查一条数据
            specParamList.stream().forEach(specParam -> {
                queryBuilder.addAggregation(AggregationBuilders.terms(specParam.getName()).field("specs."+specParam.getName()+".keyword"));
            });
            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);
            Map<String, List<String>> map = new HashMap<>();
            Aggregations aggregations = searchHits.getAggregations();
            specParamList.stream().forEach(specParam -> {
                Terms terms = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
                map.put(specParam.getName(),valueList);
            });
            return map;
        }
        return null;
    }

    //构建查询
    private NativeSearchQueryBuilder getSearchQueryBuilder(String search,Integer page){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //match只能查询一个字段  multiMatch可以查询多个字段
        queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search, "brandName", "categoryName", "title"));
        //构建聚合查询 terms查询  field通过哪个字段
        queryBuilder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId"));
        //构建高亮字段
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));
        //分页
        queryBuilder.withPageable(PageRequest.of(page-1,10));
        return queryBuilder;
    }

    //通过聚合 获得分类集合
    private Map<Integer, List<CategoryEntity>> getCategoryList(Aggregations aggregations){

        Terms cid_agg = aggregations.get("cid_agg");
        List<? extends Terms.Bucket> cidBuckets = cid_agg.getBuckets();

        //热度最高的cid
        List<Integer> hotCidArr = Arrays.asList(0);
        List<Long> maxCount = Arrays.asList(0L);

        Map<Integer, List<CategoryEntity>> map = new HashMap<>();

        List<String> cidList = cidBuckets.stream().map(cidBucket -> {

            if(cidBucket.getDocCount() > maxCount.get(0)){
                maxCount.set(0,cidBucket.getDocCount());
                hotCidArr.set(0,cidBucket.getKeyAsNumber().intValue());
            }
            return cidBucket.getKeyAsString();
        }).collect(Collectors.toList());

        String cidsStr = String.join(",", cidList);
        Result<List<CategoryEntity>> categoryResult =  categoryFeign.getCategoryByIdList(cidsStr);

        map.put(hotCidArr.get(0),categoryResult.getData());
        return map;
    }

    //通过聚合 获得品牌集合
    private List<BrandEntity> getBrandList(Aggregations aggregations){
        Terms brand_agg = aggregations.get("brand_agg");
        //返回一个id的集合-->通过id的集合去查询数据
        //可以通过new StringBuffer
        List<String> brandList = brand_agg.getBuckets()
                .stream().map(brandBucket -> brandBucket.getKeyAsString()).collect(Collectors.toList());
        //将List集合转换成,分隔的string字符串
        // String.join(",", cidList); 通过,分隔list集合 --> 返回,拼接的string字符串
        Result<List<BrandEntity>> brandResult =  brandFeign.getBrandByIdList(String.join(",", brandList));
        return brandResult.getData();
    }

    //mysql数据迁移到es做数据准备
    private List<GoodsDoc> esGoodsInfo() {
        //查询spu
        SpuDTO spuDTO = new SpuDTO();
//        spuDTO.setPage(1);
//        spuDTO.setRows(5);

        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);

        //查询出来多个数据是多个spu
        List<GoodsDoc> goodsDocs = new ArrayList<>();

        if(spuInfo.getCode() == HTTPStatus.OK){
            //spu数据
            List<SpuDTO> spuList = spuInfo.getData();

            spuList.stream().forEach(spu -> {

                GoodsDoc goodsDoc = new GoodsDoc();
                //spu信息填充
                //BaiduBeanUtil.copyProperties() 理论上可以用 但是字段类型不同 需要转换
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());

                //通过spuID查询skuList和price 将price放入List<Long> price 将sku放入String sku
                Map<List<Long>, List<Map<String, Object>>> skus = this.getSkusAndPriceList(spu.getId());
                skus.forEach((key,value) -> {
                    goodsDoc.setPrice(key);
                    goodsDoc.setSkus(JSONUtil.toJsonString(value));
                });

                //通过cid3查询规格参数 放入Map<String, Object> specs中
                Map<String, Object> specMap = this.getSpecMap(spu);

                goodsDoc.setSpecs(specMap);
                goodsDocs.add(goodsDoc);
            });
        }
        return goodsDocs;
    }

    //通过spuID查询skuList和price
    private Map<List<Long>, List<Map<String, Object>>> getSkusAndPriceList(Integer spuId){
        Map<List<Long>, List<Map<String, Object>>> skus = new HashMap<>();

        Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuId(spuId);

        List<Long> priceList = new ArrayList<>();
        List<Map<String, Object>> skuListMap = null;

        if(skuResult.getCode() == HTTPStatus.OK){
            //获取到sku数据
            List<SkuDTO> skuList = skuResult.getData();
            //遍历sku数据集合并填充信息
            skuListMap = skuList.stream().map(sku -> {
                Map<String, Object> map = new HashMap<>();

                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("images", sku.getImages());
                map.put("price", sku.getPrice());

                priceList.add(sku.getPrice().longValue());

                return map;
            }).collect(Collectors.toList());
        }
        skus.put(priceList,skuListMap);
        return skus;
    }

    //通过cid3查询规格参数
    private Map<String, Object> getSpecMap(SpuDTO spuDTO){

        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuDTO.getCid3());
        Result<List<SpecParamEntity>> specParamInfo = specificationFeign.getSpecParamInfo(specParamDTO);

        Map<String, Object> specMap = new HashMap<>();

        if(specParamInfo.getCode() == HTTPStatus.OK){
            //获得规格参数信息
            List<SpecParamEntity> paramList = specParamInfo.getData();

            //通过spuid查询spuDetail detail里有通用和特殊规格参数的值
            Result<SpuDetailEntity> spuDetailResult = goodsFeign.getDetailBySpuId(spuDTO.getId());

            //因为spu和 spudetail 是 one ---> one
            if(spuDetailResult.getCode() == HTTPStatus.OK){
                SpuDetailEntity spuDetailInfo = spuDetailResult.getData();

                //通用规格参数
                String genericSpecStr = spuDetailInfo.getGenericSpec();
                //使用工具类将string字符串 转为 Map
                Map<String, String> genericSpecMap = JSONUtil.toMapValueString(genericSpecStr);

                //特有规格参数
                String specialSpecStr = spuDetailInfo.getSpecialSpec();
                Map<String, List<String>> specialSpecMap = JSONUtil.toMapValueStrList(specialSpecStr);

                paramList.stream().forEach(param -> {
                    //判断是否为sku通用属性
                    if(param.getGeneric()){
                        //判断通用属性是否是数字类型参数 是否用于搜索过滤 数值类型参数区间不为空
                        if(param.getNumeric() && param.getSearching() && param.getSegments() != null){
                            specMap.put(param.getName(),this.chooseSegment(genericSpecMap.get(param.getId()+""),param.getSegments(),param.getUnit()));
                        }else{
                            specMap.put(param.getName(),genericSpecMap.get(param.getId()+""));
                        }
                    }else{
                        specMap.put(param.getName(),specialSpecMap.get(param.getId() + ""));
                    }
                });
            }
        }
        return specMap;
    }

    //将字符类型转换为区间
    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }

}
