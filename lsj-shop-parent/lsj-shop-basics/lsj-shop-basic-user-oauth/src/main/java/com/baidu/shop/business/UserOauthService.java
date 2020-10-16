package com.baidu.shop.business;

import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.entity.UserEntity;

public interface UserOauthService {
    String checkUser(UserEntity userEntity, JwtConfig jwtConfig);
}
