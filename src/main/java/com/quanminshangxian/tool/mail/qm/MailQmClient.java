package com.quanminshangxian.tool.mail.qm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.core.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.mail.MaillResponse;
import com.quanminshangxian.tool.model.AccessTokenCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class MailQmClient {

    private static Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();

    /**
     * 获取访问accessToken
     *
     * @param appid
     * @param appsecret
     * @param isForceRetry 是否强制从服务端重新获取
     * @return
     */
    private static String getAccessToken(String appid, String appsecret, boolean isForceRetry) {
        if (!isForceRetry) {
            AccessTokenCache accessTokenCache = accessTokenCacheMap.get(appid);
            if (accessTokenCache != null) {
                Long expireTime = accessTokenCache.getExpireTime();
                if (System.currentTimeMillis() < expireTime) {
                    return accessTokenCache.getAccessToken();
                }
            }
        }
        JSONObject params = new JSONObject();
        params.put("appid", appid);
        params.put("appsecret", appsecret);
        System.out.println("net url:" + MailQmlUrls.GET_ACCESS_TOKEN);
        String result = HttpUtils.sendPostRequest(MailQmlUrls.GET_ACCESS_TOKEN, params.toJSONString());
        System.out.println(result);
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
                return access_token;
            } else {
                System.out.println("email获取access_token失败");
            }
        }
        return null;
    }

    /**
     * 发送普通邮件
     *
     * @return
     */
    public static MaillResponse send(String appid, String appsecret, String sendMail, String receiveEmail, String subject, String content) {
        return send(appid, appsecret, sendMail, receiveEmail, subject, content, true);
    }

    private static MaillResponse send(String appid, String appsecret, String sendMail, String receiveEmail, String subject, String content, boolean isRetry) {
        MaillResponse emailResponse = new MaillResponse();
        String accessToken = getAccessToken(appid, appsecret, false);
        JSONObject params = new JSONObject();
        if (!StringUtils.isBlank(sendMail)) {
            params.put("sendMail", sendMail);
        }
        params.put("receiveMail", receiveEmail);
        params.put("subject", subject);
        params.put("content", content);
        String url = String.format(MailQmlUrls.SEND_EMAIL, accessToken);
        System.out.println("net url:" + url);
        String result = HttpUtils.sendPostRequest(url, params.toJSONString());
        System.out.println(result);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String resMsg = resJson.getString("resMsg");
            if (code == 200) {
                emailResponse.setStatus(1);
                emailResponse.setMsg(resMsg);
                return emailResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    send(appid, appsecret, sendMail, receiveEmail, subject, content, false);
                }
            } else {
                emailResponse.setStatus(0);
                emailResponse.setMsg(resMsg);
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
    public MaillResponse tplSend(String appid, String appsecret, String tplId, String sendMail, String receiveEmail, String var) {
        return tplSend(appid, appsecret, tplId, sendMail, receiveEmail, var, true);
    }

    private MaillResponse tplSend(String appid, String appsecret, String tplId, String sendMail, String receiveEmail, String var, boolean isRetry) {
        String accessToken = getAccessToken(appid, appsecret, false);
        JSONObject params = new JSONObject();
        params.put("tplId", tplId);
        if (!StringUtils.isBlank(sendMail)) {
            params.put("sendMail", sendMail);
        }
        params.put("receiveMail", receiveEmail);
        params.put("var", var);
        String url = String.format(MailQmlUrls.SEND_TPL_EMAIL, accessToken);
        System.out.println("net url:" + url);
        String result = HttpUtils.sendPostRequest(url, params.toJSONString());
        MaillResponse emailResponse = new MaillResponse();
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String resMsg = resJson.getString("resMsg");
            if (code == 200) {
                emailResponse.setStatus(1);
                emailResponse.setMsg(resMsg);
                return emailResponse;
            } else if (code == 301) {
                //如果服务端返回失效则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    tplSend(appid, appsecret, tplId, sendMail, receiveEmail, var, false);
                }
            } else {
                emailResponse.setStatus(0);
                emailResponse.setMsg(resMsg);
                return emailResponse;
            }
        } else {
            emailResponse.setMsg("接口无响应");
        }
        return emailResponse;
    }

}
