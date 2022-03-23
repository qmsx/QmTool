package com.quanminshangxian.tool.form;

public final class QmFormUrls {

    private static final String API_BASE_URL = "http://api.form.quanminshangxian.com/";
    /**
     * 获取accessToken
     */
    static String GET_ACCESS_TOKEN = API_BASE_URL + "api/getAccessToken";
    /**
     * 表单添加数据
     */
    static String FORM_ADD_DATA = API_BASE_URL + "form/addData?access_token=%s";

}
