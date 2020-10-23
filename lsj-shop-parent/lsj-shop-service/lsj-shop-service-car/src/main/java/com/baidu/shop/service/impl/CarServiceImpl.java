package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.MrShopConstant;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.SkuEntity;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.CarService;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @ClassName CarServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/19
 * @Version V1.0
 **/
@RestController
@Slf4j
public class CarServiceImpl extends BaseApiService implements CarService {

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<JSONObject> addCar(Car car,String token) {

        try {
            //获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token,jwtConfig.getPublicKey());

            //通过userId和skuId获取商品信息
            Car redisCar = redisRepository.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    , car.getSkuId() + "", Car.class);

            Car saveCar = null;
            //当前用户购物车(redis)中有要添加商品信息
            if(ObjectUtil.isNotNull(redisCar)){
                //将用户要添加商品的数量与购物车(redis)中原有商品数量相加
                redisCar.setNum(car.getNum() + redisCar.getNum());
                saveCar = redisCar;
                log.debug("当前用户购物车中有要添加商品的信息,重新设置num : {}",redisCar.getNum());
            }else{//原用户购物车(redis)中没有要添加商品信息

                Result<SkuEntity> skuResult = goodsFeign.getSkuBySkuId(car.getSkuId());
                if (skuResult.getCode() == 200) {
                    SkuEntity skuEntity = skuResult.getData();
                    car.setTitle(skuEntity.getTitle());
                    car.setImage(StringUtil.isNotEmpty(skuEntity.getImages()) ? skuEntity.getImages().split(",")[0] : "");
                    car.setPrice(Long.valueOf(skuEntity.getPrice()));
//                    Map<String, Object> stringObjectMap = JSONUtil.toMap(skuEntity.getOwnSpec());
                    //key:id
                    //value: 规格参数值
                    //遍历map
                    //feign调用通过paramId查询info的接口
                    //重新组装map
                    //将map转为json字符串
                    car.setOwnSpec(skuEntity.getOwnSpec());
                    saveCar = car;
                    log.debug("添加商品到购物车redis hashkey : {}, mapkey : {}, value : {}",MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(),car.getSkuId(),JSONUtil.toJsonString(car));
                }
            }
            boolean b = redisRepository.setHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    , car.getSkuId() + "", JSONUtil.toJsonString(saveCar));

            log.debug("是否成功" + b);

            log.debug("新增数据到redis成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> mergeCar(String clientCarList, String token) {//合并购物车

        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(clientCarList);
        List<Car> carList = com.alibaba.fastjson.JSONObject.parseArray(jsonObject.get("clientCarList").toString(), Car.class);

        carList.stream().forEach(car -> {
            this.addCar(car,token);
        });
        return this.setResultSuccess();
    }

    @Override
    public Result<List<Car>> getUserGoodsCar(String token) {
        try {
            List<Car> carList = new ArrayList<>();
            //获取当前登录用户
            UserInfo userInfo = JwtUtils.getInfoFromToken(token,jwtConfig.getPublicKey());
            //通过用户id从redis获取购物车数据
            Map<String, String> map = redisRepository.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId());
            map.forEach((key,value) -> {
                carList.add(JSONUtil.toBean(value,Car.class));
            });
            return this.setResultSuccess(carList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultError("内部错误");
    }

    @Override
    public Result<JSONObject> carNumUpdate(Long skuId, Integer type, String token) {

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token,jwtConfig.getPublicKey());
            Car redisCar = redisRepository.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    , skuId + "", Car.class);
            redisCar.setNum(type == MrShopConstant.CAR_OPERATION_INCREMENT ? redisCar.getNum() + 1 : redisCar.getNum() -1);
            redisRepository.setHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    ,skuId + "", JSONUtil.toJsonString(redisCar));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess();
    }
}
