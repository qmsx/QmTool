package com.quanminshangxian.tool.notice.qywx;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.core.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 企业微信工具类
 */
public class QyWxClient {

    private static final String WEB_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=%s";

    /**
     * 发送文字消息
     * <p>
     * {
     * "msgtype": "text",
     * "text": {
     * "content": "hello world"
     * }
     * }
     */
    public static QyWxResponse sendTextMsg(String key, String content, boolean isAtAll, List<String> atMobiles) {
        JSONObject msg = new JSONObject();
        msg.put("msgtype", "text");
        //设置内容
        JSONObject text = new JSONObject();
        text.put("content", content);
        //设置@
        if (isAtAll) {
            List<String> mobileList = new ArrayList<>();
            mobileList.add("@all");
            text.put("mentioned_mobile_list", mobileList);
        } else {
            if (atMobiles != null && atMobiles.size() > 0) {
                text.put("mentioned_mobile_list", atMobiles);
            }
        }
        msg.put("text", text);
        QyWxResponse wxResponse = new QyWxResponse();
        String url = String.format(WEB_URL, key);
        String result = HttpUtils.sendPostRequestForJson(url, msg.toJSONString());
        if (StringUtils.isBlank(result)) {
            wxResponse.setMsg("接口无响应");
            return wxResponse;
        }
        JSONObject resJson = JSON.parseObject(result);
        int errcode = resJson.getIntValue("errcode");
        if (errcode == 0) {
            wxResponse.setStatus(1);
            wxResponse.setMsg("发送成功");
            return wxResponse;
        }
        wxResponse.setMsg(result);
        return wxResponse;
    }

    /**
     * 发送markdown消息
     * <p>
     * {
     * "msgtype": "markdown",
     * "markdown": {
     * "content": "实时新增用户反馈<font color=\"warning\">132例</font>，请相关同事注意。\n
     * >类型:<font color=\"comment\">用户反馈</font>
     * >普通用户反馈:<font color=\"comment\">117例</font>
     * >VIP用户反馈:<font color=\"comment\">15例</font>"
     * }
     * }
     */
    public static QyWxResponse sendMarkdownMsg(String key, String content) {
        JSONObject msg = new JSONObject();
        msg.put("msgtype", "markdown");
        //设置内容
        JSONObject markdown = new JSONObject();
        markdown.put("content", content);
        msg.put("markdown", markdown);
        QyWxResponse wxResponse = new QyWxResponse();
        String url = String.format(WEB_URL, key);
        String result = HttpUtils.sendPostRequestForJson(url, msg.toJSONString());
        if (StringUtils.isBlank(result)) {
            wxResponse.setMsg("接口无响应");
            return wxResponse;
        }
        JSONObject resJson = JSON.parseObject(result);
        int errcode = resJson.getIntValue("errcode");
        if (errcode == 0) {
            wxResponse.setStatus(1);
            wxResponse.setMsg("发送成功");
            return wxResponse;
        }
        wxResponse.setMsg(result);
        return wxResponse;
    }

    /**
     * 发送图片消息
     * <p>
     * {
     * "msgtype": "image",
     * "image": {
     * "base64": "DATA",
     * "md5": "MD5"
     * }
     * }
     * <p>
     * 图片（base64编码前）最大不能超过2M，支持JPG,PNG格式
     */
    public static QyWxResponse sendImgMsg(String key, String base64, String md5) {
        QyWxResponse wxResponse = new QyWxResponse();
        if (base64.length() > 2 * 1024 * 1024) {
            wxResponse.setMsg("图片不能大于2M");
            return wxResponse;
        }
        JSONObject msg = new JSONObject();
        msg.put("msgtype", "image");
        //设置内容
        JSONObject image = new JSONObject();
        image.put("base64", base64);
        image.put("md5", md5);
        msg.put("image", image);
        String url = String.format(WEB_URL, key);
        String result = HttpUtils.sendPostRequestForJson(url, msg.toJSONString());
        if (StringUtils.isBlank(result)) {
            wxResponse.setMsg("接口无响应");
            return wxResponse;
        }
        JSONObject resJson = JSON.parseObject(result);
        int errcode = resJson.getIntValue("errcode");
        if (errcode == 0) {
            wxResponse.setStatus(1);
            wxResponse.setMsg("发送成功");
            return wxResponse;
        }
        wxResponse.setMsg(result);
        return wxResponse;
    }

    /**
     * 发送link
     * {
     * "msgtype": "news",
     * "news": {
     * "articles" : [
     * {
     * "title" : "中秋节礼品领取",
     * "description" : "今年中秋节公司有豪礼相送",
     * "url" : "www.qq.com",
     * "picurl" : "http://res.mail.qq.com/node/ww/wwopenmng/images/independent/doc/test_pic_msg1.png"
     * }
     * ]
     * }
     * }
     */
    public static QyWxResponse sendNewsMsg(String key, List<QyWxArticle> articles) {
        QyWxResponse wxResponse = new QyWxResponse();
        if (articles.size() > 8) {
            wxResponse.setMsg("图文消息不能超过8条");
            return wxResponse;
        }
        JSONObject msg = new JSONObject();
        msg.put("msgtype", "news");
        //设置内容
        JSONObject news = new JSONObject();
        news.put("articles", articles);
        msg.put("news", news);
        String url = String.format(WEB_URL, key);
        String result = HttpUtils.sendPostRequestForJson(url, msg.toJSONString());
        if (StringUtils.isBlank(result)) {
            wxResponse.setMsg("接口无响应");
            return wxResponse;
        }
        JSONObject resJson = JSON.parseObject(result);
        int errcode = resJson.getIntValue("errcode");
        if (errcode == 0) {
            wxResponse.setStatus(1);
            wxResponse.setMsg("发送成功");
            return wxResponse;
        }
        wxResponse.setMsg(result);
        return wxResponse;
    }

}
