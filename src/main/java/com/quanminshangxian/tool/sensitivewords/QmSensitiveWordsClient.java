package com.quanminshangxian.tool.sensitivewords;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.common.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;
import com.quanminshangxian.tool.model.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QmSensitiveWordsClient {
    private static final Logger log = LoggerFactory.getLogger(QmSensitiveWordsClient.class);

    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();
    private String appid;
    private String appsecret;

    private QmSensitiveWordsClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static QmSensitiveWordsClient build(String appid, String appsecret) {
        return new QmSensitiveWordsClient(appid, appsecret);
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
        String result = HttpUtils.doPostRequestForJson(QmSensitiveWordsUrls.GET_ACCESS_TOKEN, params.toJSONString());
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
     * 敏感文字检测
     *
     * @return
     */
    public SendResponse wordCheck(String content) {
        SendResponse sendResponse = new SendResponse();

        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg(getAccessTokenResponse.getMsg());
                return sendResponse;
            }
            String accessToken = getAccessTokenResponse.getAccessToken();
            JSONObject params = new JSONObject();
            params.put("content", content);
            String url = String.format(QmSensitiveWordsUrls.WORD_CHECK, accessToken);
            String result = HttpUtils.doPostRequestForJson(url, params.toJSONString());
            if (!StringUtils.isBlank(result)) {
                JSONObject resJson = JSON.parseObject(result);
                int code = resJson.getIntValue("code");
                String msg = resJson.getString("msg");
                if (code == 200) {
                    sendResponse.setStatus(ResponseCode.SUCCESS.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else if (code == 509) {
                    JSONObject dataJson = resJson.getJSONObject("data");
                    sendResponse.setStatus(509);
                    sendResponse.setData(dataJson);
                    return sendResponse;
                } else {
                    sendResponse.setStatus(ResponseCode.FAILED.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                }
            } else {
                sendResponse.setMsg("接口无响应");
                return sendResponse;
            }
        }
        return sendResponse;
    }

}
