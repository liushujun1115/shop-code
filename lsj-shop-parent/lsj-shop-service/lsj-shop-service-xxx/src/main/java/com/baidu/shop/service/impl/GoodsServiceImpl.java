package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.component.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;

    @Autowired
    private BrandService brandService;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private MrRabbitMQ mrRabbitMQ;

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {

        if(ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        //构建条件查询
        Example example = new Example(SpuEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtil.isNotEmpty(spuDTO.getTitle()))
            criteria.andLike("title","%"+spuDTO.getTitle()+"%");
        if(ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)
            criteria.andEqualTo("saleable",spuDTO.getSaleable());
        if (ObjectUtil.isNotNull(spuDTO.getId()))
            criteria.andEqualTo("id",spuDTO.getId());

        //排序
        if (StringUtil.isNotEmpty(spuDTO.getSort()))
            example.setOrderByClause(spuDTO.getOrderByClause());

        List<SpuEntity> list = spuMapper.selectByExample(example);
        List<SpuDTO> spuList = list.stream().map(spuEntity -> {

            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);
            //品牌名称
            this.getBrandName(spuEntity,spuDTO1);
            //分类名称
            this.getCategoryName(spuDTO1);

            return spuDTO1;
        }).collect(Collectors.toList());

        PageInfo<SpuEntity> info = new PageInfo<>(list);

        return this.setResult(HTTPStatus.OK,info.getTotal()+"",spuList);
    }

    // @Transactional  //jvm 虚拟机栈 -->入栈和出栈的问题
    @Override
    public Result<JSONObject> saveSpu(SpuDTO spuDTO) {
        Integer spuId = this.saveSpuTransaction(spuDTO);
        //发送消息
        mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_SAVE);
        return this.setResultSuccess();
    }

    @Transactional
    public Integer saveSpuTransaction(SpuDTO spuDTO){
        //JDK的动态代理
        //cglib动态代理
        //aspectj动态代理
        Date date = new Date();
        //新增spu
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);

        Integer spuId = spuEntity.getId();
        //新增detail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuId);
        spuDetailMapper.insertSelective(spuDetailEntity);

        this.saveSkusAndStocks(spuDTO.getSkus(),spuId,date);
        return spuEntity.getId();
    }

    @Override
    public Result<SpuDetailEntity> getDetailBySpuId(Integer spuId) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
    public Result<List<SkuDTO>> getSkuBySpuId(Integer spuId) {
        List<SkuDTO> list = skuMapper.selectSkuAndStockBySpuId(spuId);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<JSONObject> editSpu(SpuDTO spuDTO) {
        this.editSpuTranscation(spuDTO);
        mrRabbitMQ.send(spuDTO.getId() + "",MqMessageConstant.SPU_ROUT_KEY_UPDATE);
        return this.setResultSuccess();
    }

    @Transactional
    public void editSpuTranscation(SpuDTO spuDTO){
//    1. 修改spu
        Date date = new Date();
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);
//    2. 修改spuDetail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuDTO.getId());
        spuDetailMapper.updateByPrimaryKeySelective(spuDetailEntity);
//    3. 通过spuId查询出来将要被删除的sku 先删除再新增
        List<Long> skuIdArr = this.getSkuIdArrBySpu(spuDTO.getId());
        skuMapper.deleteByIdList(skuIdArr);
        stockMapper.deleteByIdList(skuIdArr);
//    7. 将新的数据新增到数据库
        this.saveSkusAndStocks(spuDTO.getSkus(),spuDTO.getId(),date);
    }

    @Override
    public Result<JsonObject> removeSpu(Integer spuId) {
        this.removeSpuTranscation(spuId);
        mrRabbitMQ.send(spuId + "",MqMessageConstant.SPU_ROUT_KEY_DELETE);
        return this.setResultSuccess();
    }

    @Transactional
    public void removeSpuTranscation(Integer spuId) {
        //删除spu
        spuMapper.deleteByPrimaryKey(spuId);
        //删除spuDetail
        spuDetailMapper.deleteByPrimaryKey(spuId);
        //删除sku 先通过spuId查询要删除的sku
        List<Long> skuIdArr = this.getSkuIdArrBySpu(spuId);
        if(skuIdArr.size() > 0){
            //删除sku
            skuMapper.deleteByIdList(skuIdArr);
            //删除stock 同样先查询要删除的sku
            stockMapper.deleteByIdList(skuIdArr);
        }
    }

    @Override
    @Transactional
    public Result<JSONObject> upOrDown(SpuDTO spuDTO) {
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setId(spuDTO.getId());
        if(spuEntity.getSaleable() == 1){
            spuEntity.setSaleable(0);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("下架成功");
        }else{
            spuEntity.setSaleable(1);
            spuMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("上架成功");
        }
    }

    @Override
    public Result<SkuEntity> getSkuBySkuId(Long skuId) {
        SkuEntity skuEntity = skuMapper.selectByPrimaryKey(skuId);
        return this.setResultSuccess(skuEntity);
    }

//    @Override
//    @Transactional
//    public Result<JSONObject> upOrDown(Integer id,Integer saleable) {
//        SpuEntity spuEntity = new SpuEntity();
//        spuEntity.setId(id);
//        spuEntity.setSaleable(saleable);
//        spuMapper.updateByPrimaryKeySelective(spuEntity);
//        return this.setResultSuccess("修改状态成功");
//    }

    //通过spuId查询要删除的sku
    private List<Long> getSkuIdArrBySpu(Integer spuId){
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        return skuEntities.stream().map(sku -> sku.getId()).collect(Collectors.toList());
    }

    //新增sku/stock
    private void saveSkusAndStocks(List<SkuDTO> skus,Integer spuId,Date date){
        skus.stream().forEach(skuDTO -> {
            //新增sku
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            //新增stock
            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);

        });
    }

    //设置品牌名称
    private void getBrandName(SpuEntity spuEntity,SpuDTO spuDTO1){

        BrandDTO brandDTO = new BrandDTO();
        brandDTO.setId(spuEntity.getBrandId());
        Result<PageInfo<BrandEntity>> brandInfo = brandService.getBrandInfo(brandDTO);

        if (ObjectUtil.isNotNull(brandInfo)) {
            PageInfo<BrandEntity> data = brandInfo.getData();
            List<BrandEntity> list1 = data.getList();
            if (!list1.isEmpty() && list1.size() == 1) {
                spuDTO1.setBrandName(list1.get(0).getName());
            }
        }
    }

    //设置分类名称
    private void getCategoryName(SpuDTO spuDTO1){
        String caterogyName = categoryMapper.selectByIdList(
            Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()))
            .stream().map(category -> category.getName())
            .collect(Collectors.joining("/"));

        spuDTO1.setCategoryName(caterogyName);
    }

}
