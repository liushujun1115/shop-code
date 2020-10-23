package com.baidu.shop.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @ClassName AlipayConfig
 * @Description: TODO
 * @Author liushujun
 * @Date 2020/10/22
 * @Version V1.0
 **/
public class AlipayConfig {

//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016102600766677";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCkXerwUFJqUorY7xN0jtfwZMJsjNRI+7VLdU3fij/fP4qtPrHEqppSFbaHkZG4SxaCGoEunCBvb/BkRrCukfflzZSZhcw6ZLurp0pzy2BPM/hqHL1Omg3K4im3GgyyG3t//+VPXJHlYDJHa/RMqvwChpvFXXoXbye5Hdy0XZB0fr4Z5wVPdj5tz98rxNaLNn70AM/fsKngZkqfwFOTIqqtKo0fDIZbedLMUEhhgxEvhQzIx3auw4v5mVlOLLKtDqgdDN4X3C2BIRgfCZv5nGhpV9ES2CqrKHwuUxw2G1ndZCPa6BwBcEZNcOJqE0WfxrcVMRpI+OD6o+t4UsMQMXB5AgMBAAECggEAZRPUXIAojnXAKPOstSr6pDxyS6q3CUKxFHxIrUUpTjKXJz68t0JqsDvmCax9PiX+63c9JdqhH6Zo4GGQWWouVkV8MoTTOL2lo0O3R0o0KcC97RNLX0DhZnh6Sr1cPVMlOWu2Kn20wqfLg5xhmZw0aaE5vb9kS3DxrqcsiEZZhSlHk5/tctB9inxosZDJgPrzkeoQqduBagFN6uY0O+0mlod5A7+2ask2xoKvMZ5Jfy6+aiEOLqfSiyLXJeV7paVmTXAqZsYQN46qPdGhmPglKC1LGEs4k4U3NnCOkk6TQJc8xBRpHTM3vnLqoRYDEnivmaWemXpn0yqrRmLQsJyIAQKBgQDPYubbuh7lrO6Qm9XLVLn7vlrHTR0/oiYARARVymW9xxenGkRB2hlhHq2buGGfE7szOuCdLQUD3K4A7xrawWyMgg+f1ykO/7FVyGmBOuLJBeOmsQcIuH/oeG3H7WVhWFsxXb2PMKqDMlv/ZDdUPFtyVLycauUv95BTa2EgxYOUgQKBgQDK5XMtq1uWbPHNT6EFbxfHQoUm0kxnYAoYZ5Li7UdiSVfYsnFIHDQpED97m7frJMtM9yIPt4vIscboeBe9tGoljCKbXiHQ9li9mMA/4A/u8H3dB5Cqo51xjSsBNN9ZzNXXwEzBK+jVBUo2Mk6B4ax6aWYfLU5QAaAI6C19H4p/+QKBgQC5AsB1UWty9n21oHBwftuUjFMr7c2kESHZptQ/PNrbHRwqxTITlWCC1f2HKExewBmHMIVlct5AfcT1rFnGwjv2dak+rZJBgTZREwOceE4NjCV1dgSScRYa7CTz8QM6frqMJdaQQZg8htav1lXN3jKuoo3I9UwVKMQmx2rU85/ZAQKBgQC8MZ9n2O2W1W5jNVzxoV244SaIH7/mktpKUKeZ/OSdcdIdmUYh0KdA/2UizgkF/pbjyMa7NVBe0ybgMaQzvchHE3h2DBXpsNYW5jxMAxREWKUmAwJYhHJhBMPC1rvkm24uNJv9ATFxhPqU0oynRB+rW1/xwyBEbX13RYQL7tmUYQKBgFC3lpqqyKbbAiy4GOXxngFLm3LH2j5qugSRT8L/CiiEKtenQ3rhGHE5VkQJb7U0ctbacYUyAsiIMcZ6sAT/ShaX4syxCyC41y7yPbfGRyoGy5MVoC1hT8yrE4DtU/VT/DNYbo1fgK4LFos3E/vgeSpkorRH/sVZpzsaF+tMPY/m";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkUq9lutTRJiTj7qA3OAT4v3b8uFwTO2WEU1vY/xSj1kmqmHLIhNQQlDKl9nCt36YyBDTbnsWZ0r227sEbqwCQpTTZ23foKbJXyarErYSWNYVhgcbydGd4gnpOq6dJESGtQJpzbK3OVXh9dGWGuiC+GGhTQBsVzMdcTLRlW27Ajx2SDhbIsopNoYSEEM2VgUDPv8TsUZyw6DzcPOdNDUoI0xCsVDgYLxvY9G+Y66tTV0/Cg1uwMwjinTixzKTJwci0U6m9ENafe5iIr0TUC1iZlkhvF7/E8V3UXaD8W6QFJmC6VNakVaA35hoEsDr2/MsZ8o2kn1rXzKegbPdbM/QvwIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8900/pay/returnNotify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://localhost:8900/pay/returnHTML";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 支付宝网关
    public static String log_path = "D:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
