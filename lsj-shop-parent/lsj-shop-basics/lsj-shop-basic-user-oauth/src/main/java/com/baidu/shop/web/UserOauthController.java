package com.baidu.shop.web;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.StringUtil;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @ClassName UserOauthController
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/15
 * @Version V1.0
 **/
@RestController
@RequestMapping(value = "oauth")
public class UserOauthController extends BaseApiService {

    @Autowired
    private UserOauthService userOauthService;

    @Autowired
    private JwtConfig jwtConfig;

    @PostMapping(value = "login")
    public Result<JsonObject> login(@RequestBody UserEntity userEntity
            , HttpServletRequest request, HttpServletResponse response){

        //校验用户名密码是否正确
        String token = userOauthService.checkUser(userEntity,jwtConfig);

        if(StringUtil.isEmpty(token)){//判断上述方法如果为空就错误
            return this.setResultError(HTTPStatus.VALID_USER_PASSWORD_ERROR,"用户名或密码错误");
        }
        //将token令牌写入cookie                  获得cookieName            获得令牌   获得超时时间
        CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),token,jwtConfig.getExpire());
        return this.setResultSuccess();
    }

    //校验用户是否登录,如果已登录前台页面'请登录' 展示为 '用户名'
    @GetMapping(value = "verify")
    public Result<UserInfo> verify(@CookieValue(value = "MRSHOP_TOKEN") String token
            , HttpServletResponse response,HttpServletRequest request){

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token,jwtConfig.getPublicKey());

            //每当用户在页面进行新的操作,应该刷新token的过期时间
            //解析token可以证明用户是正确的登录状态,重新生成token,这样登陆状态又刷新,30分钟后过期
            String newToken = JwtUtils.generateToken(userInfo,jwtConfig.getPrivateKey(),jwtConfig.getExpire());

            //将新的token写入cookie,用户过期时间延长
            CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),newToken,jwtConfig.getCookieMaxAge(),true);
            return this.setResultSuccess(userInfo);
        } catch (Exception e) {//如果有异常,说明token有问题
            //e.printStackTrace();
            return this.setResultError(HTTPStatus.VALID_USER_PASSWORD_ERROR,"用户失效");
        }
    }
}
