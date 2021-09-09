package com.quanminshangxian.tool.oss;

public final class OssQmUrls {

    private static final String API_BASE_URL = "http://api.oss.quanminshangxian.com/";

    /**
     * 获取accessToken
     */
    static String GET_ACCESS_TOKEN = API_BASE_URL + "api/getAccessToken";
    /**
     * 上传对象( base64 )
     */
    static String UPLOAD = API_BASE_URL + "upload?access_token=%s";
    /**
     * 上传对象( multipart )
     */
    static String MULTIPART_UPLOAD = API_BASE_URL + "multipart/upload?access_token=%s";
    /**
     * 分块上传
     */
    static String CHUNK_UPLOAD = API_BASE_URL + "chunk/upload?access_token=%s";

}
