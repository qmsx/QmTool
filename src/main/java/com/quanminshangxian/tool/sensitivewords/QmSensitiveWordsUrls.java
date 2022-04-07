package com.quanminshangxian.tool.sensitivewords;

public class QmSensitiveWordsUrls {

    private static final String API_BASE_URL = "http://api.sensitive.words.quanminshangxian.com";

    //获取accessToken
    static final String GET_ACCESS_TOKEN = API_BASE_URL + "/api/getAccessToken";
    //发送短信
    static final String WORD_CHECK = API_BASE_URL + "/word/check?access_token=%s";

}
