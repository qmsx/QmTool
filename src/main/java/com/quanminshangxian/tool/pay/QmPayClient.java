package com.quanminshangxian.tool.pay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.common.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.CreateOrderResponse;
import com.quanminshangxian.tool.model.CommonResponse;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QmPayClient {

    private String appid;
    private String appsecret;
    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();

    private QmPayClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static QmPayClient build(String appid, String appsecret) {
        return new QmPayClient(appid, appsecret);
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
                    getAccessTokenResponse.setStatus(ResponseCode.SUCCESS.code);
                    getAccessTokenResponse.setAccessToken(accessTokenCache.getAccessToken());
                    getAccessTokenResponse.setMsg(ResponseCode.SUCCESS.desc);
                    return getAccessTokenResponse;
                }
            }
        }
        JSONObject params = new JSONObject();
        params.put("appid", appid);
        params.put("appsecret", appsecret);
        String result = HttpUtils.postRequest(QmPayUrls.GET_ACCESS_TOKEN, params.toJSONString());
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
                getAccessTokenResponse.setStatus(ResponseCode.SUCCESS.code);
                getAccessTokenResponse.setAccessToken(accessTokenCache.getAccessToken());
                getAccessTokenResponse.setMsg(ResponseCode.SUCCESS.desc);
                return getAccessTokenResponse;
            } else {
                String msg = resJson.getString("msg");
                GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
                getAccessTokenResponse.setStatus(ResponseCode.FAILED.code);
                getAccessTokenResponse.setMsg(msg);
                return getAccessTokenResponse;
            }
        } else {
            GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
            getAccessTokenResponse.setStatus(ResponseCode.FAILED.code);
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
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            createOrderResponse.setStatus(ResponseCode.FAILED.code);
            createOrderResponse.setMsg(getAccessTokenResponse.getMsg());
            return createOrderResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("order", orderInfo);
        String url = String.format(QmPayUrls.CREATE_ORDER, accessToken);
        String result = HttpUtils.postRequest(url, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                String orderNo = resJson.getString("orderNo");
                createOrderResponse.setStatus(ResponseCode.SUCCESS.code);
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
                String msg = resJson.getString("msg");
                createOrderResponse.setStatus(ResponseCode.FAILED.code);
                createOrderResponse.setMsg(msg);
                return createOrderResponse;
            }
        } else {
            createOrderResponse.setStatus(ResponseCode.FAILED.code);
            createOrderResponse.setMsg("接口无响应");
        }
        return createOrderResponse;
    }

    /**
     * 获取支付宝电脑网站支付链接
     */
    public String getAlipayWebsitePayUrl(String orderNo) {
        return String.format(QmPayUrls.ALIPAY_WEBSITE_PAY_URL, orderNo);
    }

    /**
     * 获取支付宝手机网站支付链接
     */
    public String getAlipayWapPayUrl(String orderNo) {
        return String.format(QmPayUrls.ALIPAY_WAP_PAY_URL, orderNo);
    }

    /**
     * 获取支付宝App支付参数
     */
    public CommonResponse getAlipayAppPayParams(String orderNo) {
        return getAlipayAppPayParams(orderNo, true);
    }

    private CommonResponse getAlipayAppPayParams(String orderNo, boolean isRetry) {
        CommonResponse alipayAppPayParamsResponse = new CommonResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            alipayAppPayParamsResponse.setStatus(ResponseCode.FAILED.code);
            alipayAppPayParamsResponse.setMsg(getAccessTokenResponse.getMsg());
            return alipayAppPayParamsResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("orderNo", orderNo);
        String url = String.format(QmPayUrls.ALIPAY_APP_PAY_URL, accessToken);
        String result = HttpUtils.postRequest(url, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                String data = resJson.getString("data");
                alipayAppPayParamsResponse.setStatus(ResponseCode.SUCCESS.code);
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
                String msg = resJson.getString("msg");
                alipayAppPayParamsResponse.setStatus(ResponseCode.FAILED.code);
                alipayAppPayParamsResponse.setMsg(msg);
                return alipayAppPayParamsResponse;
            }
        } else {
            alipayAppPayParamsResponse.setStatus(ResponseCode.FAILED.code);
            alipayAppPayParamsResponse.setMsg("接口无响应");
        }
        return alipayAppPayParamsResponse;
    }

    /**
     * 获取微信公众号支付链接
     */
    public String getWxGzPayUrl(String orderNo) {
        return String.format(QmPayUrls.WX_GZ_PAY_URL, orderNo);
    }

    /**
     * 获取微信H5支付链接
     */
    public String getWxH5PayUrl(String orderNo) {
        return String.format(QmPayUrls.WX_H5_PAY_URL, orderNo);
    }

    /**
     * 获取微信App支付参数
     */
    public CommonResponse getWxAppPayParams(String orderNo) {
        return getWxAppPayParams(orderNo, true);
    }

    private CommonResponse getWxAppPayParams(String orderNo, boolean isRetry) {
        CommonResponse wxAppPayParamsResponse = new CommonResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            wxAppPayParamsResponse.setStatus(ResponseCode.FAILED.code);
            wxAppPayParamsResponse.setMsg(getAccessTokenResponse.getMsg());
            return wxAppPayParamsResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("orderNo", orderNo);
        String url = String.format(QmPayUrls.WX_APP_PAY_URL, accessToken);
        String result = HttpUtils.postRequest(url, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                String data = resJson.getString("data");
                wxAppPayParamsResponse.setStatus(ResponseCode.SUCCESS.code);
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
                String msg = resJson.getString("msg");
                wxAppPayParamsResponse.setStatus(ResponseCode.FAILED.code);
                wxAppPayParamsResponse.setMsg(msg);
                return wxAppPayParamsResponse;
            }
        } else {
            wxAppPayParamsResponse.setStatus(ResponseCode.FAILED.code);
            wxAppPayParamsResponse.setMsg("接口无响应");
        }
        return wxAppPayParamsResponse;
    }

}
