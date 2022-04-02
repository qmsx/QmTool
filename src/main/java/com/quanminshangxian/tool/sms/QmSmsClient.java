package com.quanminshangxian.tool.sms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.common.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;
import com.quanminshangxian.tool.model.SendResponse;
import com.quanminshangxian.tool.oss.QmOssClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QmSmsClient {
    private static final Logger log = LoggerFactory.getLogger(QmSmsClient.class);

    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();
    private String appid;
    private String appsecret;

    private QmSmsClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static QmSmsClient build(String appid, String appsecret) {
        return new QmSmsClient(appid, appsecret);
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
        String result = HttpUtils.doPostRequest(QmSmsUrls.GET_ACCESS_TOKEN, params.toJSONString());
        log.info(result);
        if (result != null) {//重试一次
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String access_token = dataJson.getString("access_token");
                int expiresIn = dataJson.getIntValue("expiresIn");
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
     * 发送短信
     *
     * @return
     */
    public SendResponse send(String tplId, String mobile, String var) {
        SendResponse smsResponse = new SendResponse();

        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                smsResponse.setStatus(ResponseCode.FAILED.code);
                smsResponse.setMsg(getAccessTokenResponse.getMsg());
                return smsResponse;
            }
            String accessToken = getAccessTokenResponse.getAccessToken();
            JSONObject params = new JSONObject();
            params.put("tplId", tplId);
            params.put("mobile", mobile);
            params.put("var", var);
            String url = String.format(QmSmsUrls.SEND_SMS, accessToken);
            String result = HttpUtils.doPostRequest(url, params.toJSONString());
            if (!StringUtils.isBlank(result)) {
                JSONObject resJson = JSON.parseObject(result);
                int code = resJson.getIntValue("code");
                String msg = resJson.getString("msg");
                if (code == 200) {
                    smsResponse.setStatus(ResponseCode.SUCCESS.code);
                    smsResponse.setMsg(msg);
                    return smsResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    smsResponse.setStatus(ResponseCode.FAILED.code);
                    smsResponse.setMsg(msg);
                    return smsResponse;
                }
            } else {
                smsResponse.setMsg("接口无响应");
                return smsResponse;
            }
        }
        return smsResponse;
    }

}
