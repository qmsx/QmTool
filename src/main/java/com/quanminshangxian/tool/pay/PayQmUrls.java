package com.quanminshangxian.tool.pay;

public final class PayQmUrls {

    private static final String BASE_URL = "https://pay.quanminshangxian.com/";
    private static final String API_BASE_URL = "https://api.pay.quanminshangxian.com/";
    /**
     * 获取accessToken
     */
    static String GET_ACCESS_TOKEN = API_BASE_URL + "api/getAccessToken";
    /**
     * 创建订单
     */
    static String CREATE_ORDER = API_BASE_URL + "order/createOrder?access_token=%s";
    /**
     * 支付宝电脑网站支付链接
     */
    static String ALIPAY_WEBSITE_PAY_URL = BASE_URL + "alipay/website?orderNo=%s";
    /**
     * 支付宝手机网站支付链接
     */
    static String ALIPAY_WAP_PAY_URL = BASE_URL + "alipay/wap?orderNo=%s";
    /**
     * 支付宝App支付参数
     */
    static String ALIPAY_APP_PAY_URL = API_BASE_URL + "alipay/getAppPayParams?access_token=%s";
    /**
     * 微信公众号支付链接
     */
    static String WX_GZ_PAY_URL = BASE_URL + "wx/gz?orderNo=%s";
    /**
     * 微信h5支付链接
     */
    static String WX_H5_PAY_URL = BASE_URL + "wx/h5?orderNo=%s";
    /**
     * 微信App支付参数
     */
    static String WX_APP_PAY_URL = API_BASE_URL + "wx/getAppPayParams?access_token=%s";

}
