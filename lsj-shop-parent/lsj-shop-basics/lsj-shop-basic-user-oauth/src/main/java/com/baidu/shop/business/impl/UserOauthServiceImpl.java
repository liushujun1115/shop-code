package com.baidu.shop.business.impl;

import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserOauthMapper;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName UserOauthServiceImpl
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Service
public class UserOauthServiceImpl implements UserOauthService {

    @Resource
    private UserOauthMapper userOauthMapper;

    @Override
    public String checkUser(UserEntity userEntity, JwtConfig jwtConfig) {
        String token = null;
        //构建一个条件查询 用作动态查询example和实体类对应
        //example类中有很多方法 可以作为where的查询条件
        Example example = new Example(UserEntity.class);
        //createCriteria 创建标准查询
        example.createCriteria().andEqualTo("username",userEntity.getUsername());
        List<UserEntity> list = userOauthMapper.selectByExample(example);
        //判断肯定只有一个用户进行登录
        if(list.size() == 1){
            UserEntity entity = list.get(0);//获得当前用户
            //通过BCrypt加密工具类 比较密码 第一个参数数据库密码 第二个参数该用户传的密码
            if(BCryptUtil.checkpw(userEntity.getPassword(),entity.getPassword())) {
                //创建tocken
                try {
                    //jwt身份验证规范   创建token     info只有两个参数 用户id和name                          获得私钥                   获得超时时间
                    token = JwtUtils.generateToken(new UserInfo(entity.getId(),entity.getUsername()),jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return token;
    }
}
