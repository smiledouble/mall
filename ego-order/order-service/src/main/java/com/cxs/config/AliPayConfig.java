package com.cxs.config;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/7 19:50
 */
public class AliPayConfig {

    //↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 支付宝网关      测试环境
    public static String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016092600599907";

    // 商户私钥，您的PKCS8格式RSA2私钥 使用工具生成的
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6WU1AczuGyjG2V7t3gAb/K+pBXSJ18iaeezTIFeABPB4LK2l+yADZWCbkAeatKuu7CfahEnHf9mXTfZG2n1kQKlSR00peP5IQQz6PEnxawEvRF4RJyq5bQfoWKIKvl5T9/Oeym0+/cChyQ2KK0Mp77iofcFGvlyrUEmCDXi31HH9SG9f1Jnn1/4/EdvUwLRf7fk1Pk+zLHe0PasNHVdWLmx74ZUrZf7svmu7wJJCRZ8qVf4IlF8eqgGXXEhXwisX+tXen+dlz/1C4v9EQfV7seU9yRnCgulMDbiQhkm+Zek29gMnTLTwPwFQ0M1zOB7HmzKfCTM6boQe64kLp+U0nAgMBAAECggEAeDSQy4lJlbop9SiEqiIw4TtPwdg1GYYZeVOaiZLqy4EfFC9tvrV/LrRZaez8AypTo3Ra3i4KvOCxcIpoizgg+HeF1AdKyp8RNmJaIWpYQyYWAgu8bulKLV3S16ZMHr6WAny4bRMYjzHnNUXYdBkY9HZ+P4dxeeX2X5R3I2aGGLysL6f10YPvX8SoZo7VbU5jup2YjND5LPDun1aSk1prPidlxwiKdhX+VOjqj/SB9t+eeu0xK+naeX3gt/Cnc9ZnT84koszyi7cK0LX8TSEJFCEXgK/C0PLpBulgAZ80CxE5IZ67P/nqayro1K9yZDRa6KuoHZvbvlBeNIeKuOg70QKBgQD30iTWy+6Lk5ubas/st1H3eiojYwP1z2VRqhpZ0UWe/pGnAezQTyTd7PVYdYnzt/ov8EPBqiej9H/ZqqmCgJL7uM+KrKeuFRXYqgnr0Z8IV7zo7hojUbLOqz7es5CczTm6pHQxyBBIVwbE+p9arcsiXd6noXWdXSw8/2Q77Ew0bwKBgQDAf8a4b8LzM0ipMvSyNA2w4F6HqX5ZYABCZrSpqbk9NvPyjKvn4eXX9MtwPRjPx9O6vJwmcxunERHrdZHigJEyCMa+KQZn5f2NWbRCqhlM2s9ntel81bBrailIYPlm8jmobTRcnA13K3O4auABZr/Wzxq/HSckZJmFoGtWlsL+yQKBgG6942RxAQoyTejnlD6ttN0oJEhn6M8hfJ8/qkLUt43TKHc+7vovQ8RALWr4K0tTvWNKtJyIUup7Sd6U2ubMWeJOtb+8Y7nxBjAp3AEbTTvHKU3ViKybx5F/CLsv9sF7G/8BmkLcqhoJeabwb+FvpESZOfQivJIznp8raTk14N/VAoGBAKL2SQvwiVCLtNKQcbi++mwntb8RTQZqkC2mdwE+zrO05tYKIqm+gm7NLMCK6ItWNrdaXrLqNZm8iLgVovfmMYTzCxUG9AZXHMxiCYl6KMr4XicFSHHHEYANsn80oU52dztPn8lazsd5AJE54QMuse8vCwyJqI5Vn+YRBW2bWd6JAoGAEdQFUemC5wGIeGGvS2uRn2V0VO7hFR58Mqk5VV4Fy8pj67+TKoBQZ7FpxQ7GYaNMWfMI3iL/eOx2ro9kmBqMyS3lTkVEMfroRkgrQsxwUXA0i/UcXMzPNVPNiV/0qDueSgnk2zGVuKq81FZ9flePr+GPfkeoWDENSqDcPQTSWA4=";

    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApYpMNGznPNFMsL4fr6c5YFo2QWo4veG/udQMZmJQpTtXVlblqXSIgRsTy2EdCDkRyDqMVKj2sMmzv1cIyZqgZOw96g7Z4IKTDuGOkWxz4+dMJX1qnhQbfkvk+tvYTU2sLZ25CDAg4UEHfB5DpGZCru+SeIrCR6lN/ZEfIYKqNlCq4BcHOBD5pXvs1eIOfSmOGfYLgKfWtJP8da0BNYt3EbYgUDRTnagBnqvGUR5eZs226w+OhgS9kWXJ2suuMdkkBfOjNkyOf4n0heNiXYIIDdODUGkuuU+Ph9IkGjRz32u4hZxsZndfJdDpWhxaZYsY9NuLHw5TspdWlgSrXd2ePwIDAQAB";
    // 载网页上看，不是工具生成的 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/notify_url.jsp";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://工程公网访问地址/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";


    // 支付宝网关
    public static String log_path = "C:\\";


    //↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     *
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis() + ".txt");
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
