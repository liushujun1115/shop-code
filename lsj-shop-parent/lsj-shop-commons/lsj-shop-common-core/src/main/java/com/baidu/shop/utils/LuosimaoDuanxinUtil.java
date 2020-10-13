package com.baidu.shop.utils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import lombok.extern.slf4j.Slf4j;
import javax.ws.rs.core.MediaType;

/**
 * @ClassName LuosimaoDuanxinUtil
 * @Description: 螺丝帽第三方平台发送短信
 * @Author liushujun
 * @Date 2020/10/13
 * @Version V1.0
 **/
@Slf4j
public class LuosimaoDuanxinUtil {

    //短信平台的APIkey
    private static final String DUANXIN_API_KEY = "key-ba7ce1049ea56db3c8d20b8b43f88238";//需要使用自己的APIkey
    private static final String SPEAK_API_KEY = "key-db6cb13c8d235d6cb45ccbfaf13602aa";//需要使用自己的APIkey
    private static final String SEND_DUANXIN_URL = "http://sms-api.luosimao.com/v1/send.json";//发送短信的接口
    private static final String SEND_SPEAK_URL = "http://voice-api.luosimao.com/v1/verify.json";//发送语音的接口
    private static final String STATUS_URL = "http://sms-api.luosimao.com/v1/status.json";//查看余额的接口

    public static String sendCode(String phone,String code){

        // just replace key here
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(
                "api",DUANXIN_API_KEY));
        WebResource webResource = client.resource(SEND_DUANXIN_URL);
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("mobile", phone);
        formData.add("message", "验证码：" + code + "【铁壳测试】");//注意此处不能修改
        ClientResponse response =  webResource.type(MediaType.APPLICATION_FORM_URLENCODED).
                post(ClientResponse.class, formData);
        String textEntity = response.getEntity(String.class);
        int status = response.getStatus();
        log.info(textEntity);
        log.info("---------发送短信验证状态------" + status);
        return textEntity;
    }

    public static String sendSpeak(String phone,String code){
        // just replace key here
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(
                "api",SPEAK_API_KEY));
        WebResource webResource = client.resource(
                SEND_SPEAK_URL);
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        formData.add("mobile", phone);
        formData.add("code", code);
        ClientResponse response =  webResource.type(MediaType.APPLICATION_FORM_URLENCODED).
                post(ClientResponse.class, formData);
        String textEntity = response.getEntity(String.class);
        int status = response.getStatus();
        log.info(textEntity);
        log.info("---------发送语音验证状态------" + status);

        return textEntity;
    }

    private static String getStatus(){
        Client client = Client.create();
        client.addFilter(new HTTPBasicAuthFilter(
                "api",DUANXIN_API_KEY));
        WebResource webResource = client.resource( STATUS_URL );
        MultivaluedMapImpl formData = new MultivaluedMapImpl();
        ClientResponse response =  webResource.get( ClientResponse.class );
        String textEntity = response.getEntity(String.class);
        int status = response.getStatus();

        log.info(textEntity);
        log.info(status + "");
        return textEntity;
    }
}
