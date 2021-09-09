package com.quanminshangxian.tool.notice.dingding;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.core.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.SendResponse;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.util.List;

/**
 * 钉钉工具类
 */
public class DingdingClient {

    private static final String WEB_URL = "https://oapi.dingtalk.com/robot/send?access_token=%s&timestamp=%s&sign=%s";

    /**
     * 发送文字消息
     * <p>
     * {
     * "msgtype": "text",
     * "text": {
     * "content": "hello world"
     * },
     * "at": {
     * "atMobiles": [
     * "135****2584",
     * ],
     * "isAtAll": false
     * }
     * }
     */
    public static SendResponse sendTextMsg(String accessToken, String secret, String content, boolean isAtAll, List<String> atMobiles) {
        JSONObject msg = new JSONObject();
        msg.put("msgtype", "text");
        //设置内容
        JSONObject text = new JSONObject();
        text.put("content", content);
        msg.put("text", text);
        //设置@
        if (isAtAll) {
            JSONObject at = new JSONObject();
            at.put("isAtAll", true);
            msg.put("at", at);
        } else {
            if (atMobiles != null && atMobiles.size() > 0) {
                JSONObject at = new JSONObject();
                at.put("atMobiles", atMobiles);
                msg.put("at", at);
            }
        }
        SendResponse dingdingResponse = new SendResponse();
        Long timestamp = System.currentTimeMillis();
        String sign = getSign(timestamp, secret);
        if (sign == null) {
            dingdingResponse.setMsg("签名失败");
            return dingdingResponse;
        }
        String url = String.format(WEB_URL, accessToken, timestamp, sign);
        String result = HttpUtils.sendPostRequestForJson(url, msg.toJSONString());
        if (StringUtils.isBlank(result)) {
            dingdingResponse.setMsg("接口无响应");
            return dingdingResponse;
        }
        JSONObject resJson = JSON.parseObject(result);
        int errcode = resJson.getIntValue("errcode");
        if (errcode == 0) {
            dingdingResponse.setStatus(ResponseCode.SUCCESS.code());
            dingdingResponse.setMsg("发送成功");
            return dingdingResponse;
        }
        dingdingResponse.setMsg(result);
        return dingdingResponse;
    }

    /**
     * 发送link
     * {
     * "msgtype": "link",
     * "link": {
     * "text": "hello world",
     * "title": "title",
     * "picUrl": "",
     * "messageUrl": "https://**.com/link"
     * }
     * }
     */
    public static SendResponse sendLinkMsg(String accessToken, String secret, String content, String title, String picUrl, String messageUrl) {
        JSONObject msg = new JSONObject();
        msg.put("msgtype", "link");
        //设置内容
        JSONObject link = new JSONObject();
        link.put("text", content);
        link.put("title", title);
        link.put("picUrl", picUrl);
        link.put("messageUrl", messageUrl);
        msg.put("link", link);
        SendResponse dingdingResponse = new SendResponse();
        Long timestamp = System.currentTimeMillis();
        String sign = getSign(timestamp, secret);
        if (sign == null) {
            dingdingResponse.setMsg("签名失败");
            return dingdingResponse;
        }
        String url = String.format(WEB_URL, accessToken, timestamp, sign);
        String result = HttpUtils.sendPostRequestForJson(url, msg.toJSONString());
        if (StringUtils.isBlank(result)) {
            dingdingResponse.setMsg("接口无响应");
            return dingdingResponse;
        }
        JSONObject resJson = JSON.parseObject(result);
        int errcode = resJson.getIntValue("errcode");
        if (errcode == 0) {
            dingdingResponse.setStatus(ResponseCode.SUCCESS.code());
            dingdingResponse.setMsg("发送成功");
            return dingdingResponse;
        }
        dingdingResponse.setMsg(result);
        return dingdingResponse;
    }

    /**
     * 获取签名
     *
     * @param timestamp
     * @return
     * @throws Exception
     */
    private static String getSign(Long timestamp, String secret) {
        try {
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            return URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
