package com.quanminshangxian.tool.mail.qm;

import com.alibaba.fastjson.JSON;
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


public final class QmMailClient {

    private static final Logger log = LoggerFactory.getLogger(QmMailClient.class);

    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();

    private String appid;
    private String appsecret;

    private QmMailClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static QmMailClient build(String appid, String appsecret) {
        return new QmMailClient(appid, appsecret);
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
        String result = HttpUtils.postRequest(QmMailUrls.GET_ACCESS_TOKEN, params.toJSONString());
        log.info("GET_ACCESS_TOKEN result:" + result);
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
     * 发送普通邮件
     *
     * @return
     */
    public SendResponse send(String sendMail, String receiveEmail, String subject, String content) {
        return realSend(sendMail, receiveEmail, subject, content, true);
    }

    private SendResponse realSend(String sendMail, String receiveEmail, String subject, String content, boolean isRetry) {
        SendResponse emailResponse = new SendResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            emailResponse.setStatus(ResponseCode.FAILED.code);
            emailResponse.setMsg(getAccessTokenResponse.getMsg());
            return emailResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        if (!StringUtils.isBlank(sendMail)) {
            params.put("sendMail", sendMail);
        }
        params.put("receiveMail", receiveEmail);
        params.put("subject", subject);
        params.put("content", content);
        String url = String.format(QmMailUrls.SEND_EMAIL, accessToken);
        String result = HttpUtils.postRequest(url, params.toJSONString());
        log.info("send email result:" + result);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String msg = resJson.getString("msg");
            if (code == 200) {
                emailResponse.setStatus(ResponseCode.SUCCESS.code);
                emailResponse.setMsg(msg);
                return emailResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    realSend(sendMail, receiveEmail, subject, content, false);
                }
            } else {
                emailResponse.setStatus(ResponseCode.FAILED.code);
                emailResponse.setMsg(msg);
                return emailResponse;
            }
        } else {
            emailResponse.setMsg("接口无响应");
        }
        return emailResponse;
    }

    /**
     * 发送模板邮件
     *
     * @return
     */
    public SendResponse tplSend(String appid, String appsecret, String tplId, String sendMail, String receiveEmail, String var) {
        return tplSend(appid, appsecret, tplId, sendMail, receiveEmail, var, true);
    }

    private SendResponse tplSend(String appid, String appsecret, String tplId, String sendMail, String receiveEmail, String var, boolean isRetry) {
        SendResponse emailResponse = new SendResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            emailResponse.setStatus(ResponseCode.FAILED.code);
            emailResponse.setMsg(getAccessTokenResponse.getMsg());
            return emailResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("tplId", tplId);
        if (!StringUtils.isBlank(sendMail)) {
            params.put("sendMail", sendMail);
        }
        params.put("receiveMail", receiveEmail);
        params.put("var", var);
        String url = String.format(QmMailUrls.SEND_TPL_EMAIL, accessToken);
        String result = HttpUtils.postRequest(url, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String msg = resJson.getString("msg");
            if (code == 200) {
                emailResponse.setStatus(ResponseCode.SUCCESS.code);
                emailResponse.setMsg(msg);
                return emailResponse;
            } else if (code == 301) {
                //如果服务端返回失效则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    tplSend(appid, appsecret, tplId, sendMail, receiveEmail, var, false);
                }
            } else {
                emailResponse.setStatus(ResponseCode.FAILED.code);
                emailResponse.setMsg(msg);
                return emailResponse;
            }
        } else {
            emailResponse.setMsg("接口无响应");
        }
        return emailResponse;
    }

}
