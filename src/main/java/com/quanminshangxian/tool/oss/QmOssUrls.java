package com.quanminshangxian.tool.oss;

public final class QmOssUrls {

    private static final String API_BASE_URL = "http://api.oss.quanminshangxian.com";

    /**
     * 获取accessToken
     */
    static String GET_ACCESS_TOKEN = API_BASE_URL + "/api/getAccessToken";
    /**
     * 上传对象( base64 )
     */
    static String UPLOAD_BASE64 = API_BASE_URL + "/upload/base64?access_token=%s";
    /**
     * 上传对象( multipart )
     */
    static String UPLOAD_MULTIPART = API_BASE_URL + "/upload/multipart?access_token=%s";
    /**
     * 分块上传
     */
    static String UPLOAD_CHUNK = API_BASE_URL + "/upload/chunk?access_token=%s";
    /**
     * 获取对象的外网链接
     */
    static String GET_NET_URL = API_BASE_URL + "/oss/getNetUrl?access_token=%s";
    /**
     * 对象重命名
     */
    static String RENAME_URL = API_BASE_URL + "/oss/rename?access_token=%s";
    /**
     * 删除对象
     */
    static String DELETE_URL = API_BASE_URL + "/oss/delete?access_token=%s";

}
