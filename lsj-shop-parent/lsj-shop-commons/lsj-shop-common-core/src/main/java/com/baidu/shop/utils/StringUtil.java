package com.baidu.shop.utils;

/**
 * @ClassName StringUtil
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/8/31
 * @Version V1.0
 **/
public class StringUtil {

    //判断字符串类型不为空且不为null
    public static Boolean isNotEmpty(String str){

        return null != str && !"".equals(str);
    }

    //判断字符串类型为空或为null
    public static Boolean isEmpty(String str){

        return null == str || "".equals(str);
    }

    //将String类型转换为Integer
    public static Integer toInteger(String str){
        if(isNotEmpty(str)) return Integer.parseInt(str);
        return 0;
    }
}
