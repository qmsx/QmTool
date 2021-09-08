package com.quanminshangxian.tool.pay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.core.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.CreateOrderResponse;
import com.quanminshangxian.tool.model.DataResponse;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PayQmClient {

    private String appid;
    private String appsecret;
    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();

    private PayQmClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static PayQmClient build(String appid, String appsecret) {
        return new PayQmClient(appid, appsecret);
    }

    /**
     * 获取访问accessToken
     *
     * @param appid
     * @param appsecret
     * @param isForceRetry 是否强制从服务端重新获取
     * @return
     */
    private static GetAccessTokenResponse getAccessToken(String appid, String appsecret, boolean isForceRetry) {
        if (!isForceRetry) {
            AccessTokenCache accessTokenCache = accessTokenCacheMap.get(appid);
            if (accessTokenCache != null) {
                Long expireTime = accessTokenCache.getExpireTime();
                if (System.currentTimeMillis() < expireTime) {
                    GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
                    getAccessTokenResponse.setStatus(ResponseCode.SUCCESS.code());
                    getAccessTokenResponse.setAccessToken(accessTokenCache.getAccessToken());
                    getAccessTokenResponse.setMsg(ResponseCode.SUCCESS.desc());
                    return getAccessTokenResponse;
                }
            }
        }
        JSONObject params = new JSONObject();
        params.put("appid", appid);
        params.put("appsecret", appsecret);
        String result = HttpUtils.sendPostRequest(PayQmUrls.GET_ACCESS_TOKEN, params.toJSONString());
        if (result != null) {//重试一次
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                String access_token = resJson.getString("access_token");
                //单位为秒
                int expiresIn = resJson.getIntValue("expiresIn");
                AccessTokenCache accessTokenCache = new AccessTokenCache();
                accessTokenCache.setAccessToken(access_token);
                //提前 5 秒获取新accessToken
                accessTokenCache.setExpireTime(System.currentTimeMillis() + expiresIn * 1000L - 5000L);
                accessTokenCacheMap.put(appid, accessTokenCache);
                //返回
                GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
                getAccessTokenResponse.setStatus(ResponseCode.SUCCESS.code());
                getAccessTokenResponse.setAccessToken(accessTokenCache.getAccessToken());
                getAccessTokenResponse.setMsg(ResponseCode.SUCCESS.desc());
                return getAccessTokenResponse;
            } else {
                String resMsg = resJson.getString("resMsg");
                GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
                getAccessTokenResponse.setStatus(ResponseCode.FAILURE.code());
                getAccessTokenResponse.setMsg(resMsg);
                return getAccessTokenResponse;
            }
        } else {
            GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
            getAccessTokenResponse.setStatus(ResponseCode.FAILURE.code());
            getAccessTokenResponse.setMsg("接口返回空");
            return getAccessTokenResponse;
        }
    }

    /**
     * 创建订单
     *
     * @return
     */
    public CreateOrderResponse createOrder(String orderInfo) {
        return createOrder(orderInfo, true);
    }

    private CreateOrderResponse createOrder(String orderInfo, boolean isRetry) {
        CreateOrderResponse createOrderResponse = new CreateOrderResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILURE.code()) {
            createOrderResponse.setStatus(ResponseCode.FAILURE.code());
            createOrderResponse.setMsg(getAccessTokenResponse.getMsg());
            return createOrderResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("order", orderInfo);
        String url = String.format(PayQmUrls.CREATE_ORDER, accessToken);
        String result = HttpUtils.sendPostRequest(url, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                String orderNo = resJson.getString("orderNo");
                createOrderResponse.setStatus(ResponseCode.SUCCESS.code());
                createOrderResponse.setOrderNo(orderNo);
                return createOrderResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    return createOrder(orderInfo, false);
                }
            } else {
                String resMsg = resJson.getString("resMsg");
                createOrderResponse.setStatus(ResponseCode.FAILURE.code());
                createOrderResponse.setMsg(resMsg);
                return createOrderResponse;
            }
        } else {
            createOrderResponse.setStatus(ResponseCode.FAILURE.code());
            createOrderResponse.setMsg("接口无响应");
        }
        return createOrderResponse;
    }

    /**
     * 获取支付宝电脑网站支付链接
     */
    public String getAlipayWebsitePayUrl(String orderNo) {
        return String.format(PayQmUrls.ALIPAY_WEBSITE_PAY_URL, orderNo);
    }

    /**
     * 获取支付宝手机网站支付链接
     */
    public String getAlipayWapPayUrl(String orderNo) {
        return String.format(PayQmUrls.ALIPAY_WAP_PAY_URL, orderNo);
    }

    /**
     * 获取支付宝App支付参数
     */
    public DataResponse getAlipayAppPayParams(String orderNo) {
        return getAlipayAppPayParams(orderNo, true);
    }

    private DataResponse getAlipayAppPayParams(String orderNo, boolean isRetry) {
        DataResponse alipayAppPayParamsResponse = new DataResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILURE.code()) {
            alipayAppPayParamsResponse.setStatus(ResponseCode.FAILURE.code());
            alipayAppPayParamsResponse.setMsg(getAccessTokenResponse.getMsg());
            return alipayAppPayParamsResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("orderNo", orderNo);
        String url = String.format(PayQmUrls.ALIPAY_APP_PAY_URL, accessToken);
        String result = HttpUtils.sendPostRequest(url, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                String data = resJson.getString("data");
                alipayAppPayParamsResponse.setStatus(ResponseCode.SUCCESS.code());
                alipayAppPayParamsResponse.setData(data);
                return alipayAppPayParamsResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    return getAlipayAppPayParams(orderNo, false);
                }
            } else {
                String resMsg = resJson.getString("resMsg");
                alipayAppPayParamsResponse.setStatus(ResponseCode.FAILURE.code());
                alipayAppPayParamsResponse.setMsg(resMsg);
                return alipayAppPayParamsResponse;
            }
        } else {
            alipayAppPayParamsResponse.setStatus(ResponseCode.FAILURE.code());
            alipayAppPayParamsResponse.setMsg("接口无响应");
        }
        return alipayAppPayParamsResponse;
    }

    /**
     * 获取微信公众号支付链接
     */
    public String getWxGzPayUrl(String orderNo) {
        return String.format(PayQmUrls.WX_GZ_PAY_URL, orderNo);
    }

    /**
     * 获取微信H5支付链接
     */
    public String getWxH5PayUrl(String orderNo) {
        return String.format(PayQmUrls.WX_H5_PAY_URL, orderNo);
    }

    /**
     * 获取微信App支付参数
     */
    public DataResponse getWxAppPayParams(String orderNo) {
        return getWxAppPayParams(orderNo, true);
    }

    private DataResponse getWxAppPayParams(String orderNo, boolean isRetry) {
        DataResponse wxAppPayParamsResponse = new DataResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILURE.code()) {
            wxAppPayParamsResponse.setStatus(ResponseCode.FAILURE.code());
            wxAppPayParamsResponse.setMsg(getAccessTokenResponse.getMsg());
            return wxAppPayParamsResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("orderNo", orderNo);
        String url = String.format(PayQmUrls.WX_APP_PAY_URL, accessToken);
        String result = HttpUtils.sendPostRequest(url, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                String data = resJson.getString("data");
                wxAppPayParamsResponse.setStatus(ResponseCode.SUCCESS.code());
                wxAppPayParamsResponse.setData(data);
                return wxAppPayParamsResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    return getWxAppPayParams(orderNo, false);
                }
            } else {
                String resMsg = resJson.getString("resMsg");
                wxAppPayParamsResponse.setStatus(ResponseCode.FAILURE.code());
                wxAppPayParamsResponse.setMsg(resMsg);
                return wxAppPayParamsResponse;
            }
        } else {
            wxAppPayParamsResponse.setStatus(ResponseCode.FAILURE.code());
            wxAppPayParamsResponse.setMsg("接口无响应");
        }
        return wxAppPayParamsResponse;
    }

}
