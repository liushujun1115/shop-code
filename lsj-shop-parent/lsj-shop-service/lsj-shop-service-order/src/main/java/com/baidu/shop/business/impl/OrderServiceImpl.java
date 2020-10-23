package com.baidu.shop.business.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OrderService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.MrShopConstant;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.OrderDTO;
import com.baidu.shop.dto.OrderInfo;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.OrderDetailEntity;
import com.baidu.shop.entity.OrderEntity;
import com.baidu.shop.entity.OrderStatusEntity;
import com.baidu.shop.mapper.OrderDetailMapper;
import com.baidu.shop.mapper.OrderMapper;
import com.baidu.shop.mapper.OrderStatusMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.IdWorker;
import com.baidu.shop.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/21
 * @Version V1.0
 **/
@RestController
public class OrderServiceImpl extends BaseApiService implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;//雪花算法

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private RedisRepository redisRepository;

    @Transactional
    @Override
    public Result<String> createOrder(OrderDTO orderDTO,String token) { //Long类型会丢失精度

        long orderId = idWorker.nextId();//通过雪花算法生成订单id
        try {

            UserInfo userInfo = JwtUtils.getInfoFromToken(token,jwtConfig.getPublicKey());

            Date date = new Date();
            //order 生成订单
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setUserId(userInfo.getId() + "");
            orderEntity.setOrderId(orderId);
//            orderEntity.setTotalPay();//总金额
//            orderEntity.setActualPay();//实付金额
            orderEntity.setPaymentType(orderDTO.getPayType());//支付类型
            orderEntity.setSourceType(2);//订单来源 写死的pc端,如果项目健全,这个值应该是常量
            orderEntity.setInvoiceType(1);//发票类型 同上
            orderEntity.setBuyerRate(1);//买家是否评价 无
            orderEntity.setBuyerNick(userInfo.getUsername());
            orderEntity.setBuyerMessage("nice");//买家留言
            orderEntity.setCreateTime(date);

            List<Long> longs = Arrays.asList(0L);
            //order_detail 订单详情
            List<OrderDetailEntity> orderDetailList = Arrays.asList(orderDTO.getSkuIds().split(",")).stream().map(skuIdStr -> {

                //通过skuid查询redis中sku数据
                Car car = redisRepository.getHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(), skuIdStr, Car.class);
                if (car == null) {
                    throw new RuntimeException("数据异常");
                }
                OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
                orderDetailEntity.setSkuId(Long.valueOf(skuIdStr));
                orderDetailEntity.setTitle(car.getTitle());
                orderDetailEntity.setPrice(car.getPrice());
                orderDetailEntity.setOrderId(orderId);
                orderDetailEntity.setNum(car.getNum());
                orderDetailEntity.setImage(car.getImage());
                orderDetailEntity.setOwnSpec(car.getOwnSpec());
                longs.set(0,car.getPrice() * car.getNum() + longs.get(0));

                return orderDetailEntity;
            }).collect(Collectors.toList());

            orderEntity.setTotalPay(longs.get(0));//总金额
            orderEntity.setActualPay(longs.get(0));//实付金额

            //status 订单状态
            OrderStatusEntity orderStatusEntity = new OrderStatusEntity();
            orderStatusEntity.setCreateTime(date);
            orderStatusEntity.setStatus(1);//订单已创建,未支付
            orderStatusEntity.setOrderId(orderId);

            //入库
            orderMapper.insertSelective(orderEntity);
            orderDetailMapper.insertList(orderDetailList);//批量新增
            orderStatusMapper.insertSelective(orderStatusEntity);

            //通过用户id和skuId删除购物车中的数据
            Arrays.asList(orderDTO.getSkuIds().split(",")).stream().forEach(skuIdStr -> {
                redisRepository.delHash(MrShopConstant.USER_GOODS_CAR_PRE + userInfo.getId(),skuIdStr);
            });
            //此时要保证redis和mysql双写一致性
            //更新库存-->用户已经下单了,这个时候减库存
//            Arrays.asList(orderDTO.getSkuIds().split(",")).stream().forEach(skuIdStr -> {
//
//            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResult(HTTPStatus.OK,"",orderId+"");
    }

    @Override
    public Result<OrderInfo> getOrderByOrderId(Long orderId) {

        OrderEntity orderEntity = orderMapper.selectByPrimaryKey(orderId);

        OrderInfo orderInfo = BaiduBeanUtil.copyProperties(orderEntity,OrderInfo.class);

        Example example = new Example(OrderDetailEntity.class);
        example.createCriteria().andEqualTo("orderId",orderInfo.getOrderId());
        List<OrderDetailEntity> orderDetailList = orderDetailMapper.selectByExample(example);
        orderInfo.setOrderDetailList(orderDetailList);

        OrderStatusEntity orderStatusEntity = orderStatusMapper.selectByPrimaryKey(orderInfo.getOrderId());
        orderInfo.setOrderStatusEntity(orderStatusEntity);

        return this.setResultSuccess(orderInfo);
    }

}

