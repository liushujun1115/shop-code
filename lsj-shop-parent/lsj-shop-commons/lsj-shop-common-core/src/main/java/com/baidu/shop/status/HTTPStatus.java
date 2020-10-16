package com.baidu.shop.status;

/**
 * @ClassName HTTPStatus
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/8/27
 * @Version V1.0
 **/
public class HTTPStatus {

    public static final int OK = 200;//成功

    public static final int ERROR = 500;//失败

    public static final int OPERATION_ERROR = 5001;//操作失败

    public static final int PARAMS_VALIDATE_ERROR = 5002;//参数校验失败

    public static final int PHONE_CODE_VALID_ERROR = 5003;//手机验证码校验失败

    public static final int VALID_USER_PASSWORD_ERROR = 403;//用户名密码校验失败
}
