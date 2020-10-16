package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.MrShopConstant;
import com.baidu.shop.constant.UserConstant;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.LuosimaoDuanxinUtil;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/13
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {
        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));
        userEntity.setCreated(new Date());
        userMapper.insertSelective(userEntity);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<UserEntity>> checkUsernameOrPhone(String value, Integer type) {
        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(value != null && type != null){
            if(type == UserConstant.USER_TYPE_USERNAME){
                criteria.andEqualTo("username",value);
            }else if(type == UserConstant.USER_TYPE_PHONE){
                criteria.andEqualTo("phone",value);
            }
        }
        List<UserEntity> userList = userMapper.selectByExample(example);
        return this.setResultSuccess(userList);
    }

    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);
        redisRepository.set(MrShopConstant.USER_PHONE_CODE_PRE + userDTO.getPhone(),code);
        redisRepository.expire(MrShopConstant.USER_PHONE_CODE_PRE + userDTO.getPhone(),120);
        //LuosimaoDuanxinUtil.sendSpeak(userDTO.getPhone(),code);
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkVaildCode(String phone, String validcode) {
        String redisVaildCode = redisRepository.get(MrShopConstant.USER_PHONE_CODE_PRE + phone);
        if(!validcode.equals(redisVaildCode)){
            return this.setResultError(HTTPStatus.PHONE_CODE_VALID_ERROR,"验证码输入错误");
        }
        return this.setResultSuccess();
    }
}
