package com.baidu.filter;

import com.baidu.config.JwtConfig;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.ObjectUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName LoginFilter
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/17
 * @Version V1.0
 **/
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtConfig jwtConfig;

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 5;//优先级
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //获取请求的url
        String requestURI = request.getRequestURI();
        logger.debug("----------------------" + requestURI);
        //如果当前请求是登录请求,不执行拦截器.
        //excludePath是不拦截请求的路径 如果包含当前请求 为true就会拦截 所以加!为false
        return !jwtConfig.getExcludePath().contains(requestURI);
    }

    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        logger.info("拦截到请求" + request.getRequestURI());
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtConfig.getCookieName());
        logger.info("token信息" + token);
        if(token != null){
            try {
                //通过公钥解密，如果成功，就放行，失败就拦截
                JwtUtils.getInfoFromToken(token,jwtConfig.getPublicKey());
            } catch (Exception e) {
                logger.info("解析失败  拦截" + token);
                // 校验出现异常，返回403
                context.setSendZuulResponse(false);
                context.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
            }
        }else{
            logger.info("没有token");
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
        }
        return null;
    }

}
