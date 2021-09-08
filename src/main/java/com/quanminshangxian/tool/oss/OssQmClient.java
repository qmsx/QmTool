package com.quanminshangxian.tool.oss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.core.StringUtils;
import com.quanminshangxian.tool.file.FileUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OssQmClient {

    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();
    private String appid;
    private String appsecret;

    private OssQmClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static OssQmClient build(String appid, String appsecret) {
        return new OssQmClient(appid, appsecret);
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
                    getAccessTokenResponse.setStatus(ResponseCode.SUCCESS.code());
                    getAccessTokenResponse.setAccessToken(accessTokenCache.getAccessToken());
                    getAccessTokenResponse.setMsg(ResponseCode.SUCCESS.desc());
                    return getAccessTokenResponse;
                }
            }
        }
        JSONObject params = new JSONObject();
        params.put("appid", appid);
        params.put("appsecret", appsecret);
        String result = HttpUtils.sendPostRequest(OssQmUrls.GET_ACCESS_TOKEN, params.toJSONString());
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
                getAccessTokenResponse.setStatus(ResponseCode.SUCCESS.code());
                getAccessTokenResponse.setAccessToken(accessTokenCache.getAccessToken());
                getAccessTokenResponse.setMsg(ResponseCode.SUCCESS.desc());
                return getAccessTokenResponse;
            } else {
                String resMsg = resJson.getString("resMsg");
                GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
                getAccessTokenResponse.setStatus(ResponseCode.FAILURE.code());
                getAccessTokenResponse.setMsg(resMsg);
                return getAccessTokenResponse;
            }
        } else {
            GetAccessTokenResponse getAccessTokenResponse = new GetAccessTokenResponse();
            getAccessTokenResponse.setStatus(ResponseCode.FAILURE.code());
            getAccessTokenResponse.setMsg("接口返回空");
            return getAccessTokenResponse;
        }
    }

    /**
     * 上传对象 ( base64 )
     *
     * @return
     */
    public OssQmResponse upload(Long parentId, String name, String data) {
        if (data.length() > 1024 * 1024 * 10) {
            OssQmResponse ossResponse = new OssQmResponse();
            ossResponse.setStatus(0);
            ossResponse.setMsg("base64方式上传文件不能大于10M");
            return ossResponse;
        }
        return realUpload(parentId, name, data, true);
    }

    private OssQmResponse realUpload(Long parentId, String name, String data, boolean isRetry) {
        OssQmResponse ossResponse = new OssQmResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILURE.code()) {
            ossResponse.setStatus(ResponseCode.FAILURE.code());
            ossResponse.setMsg(getAccessTokenResponse.getMsg());
            return ossResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("parentId", parentId);
        params.put("name", name);
        params.put("data", data);
        String uploadUrl = String.format(OssQmUrls.UPLOAD, accessToken);
        String result = HttpUtils.sendPostRequest(uploadUrl, params.toJSONString());
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String resMsg = resJson.getString("resMsg");
            if (code == 200) {
                String url = resJson.getString("url");
                Integer width = resJson.getInteger("width");
                Integer height = resJson.getInteger("height");
                Integer times = resJson.getInteger("times");
                ossResponse.setStatus(ResponseCode.SUCCESS.code());
                ossResponse.setMsg("success");
                OssQmResponseData ossResponseData = new OssQmResponseData();
                ossResponseData.setUrl(url);
                ossResponseData.setWidth(width);
                ossResponseData.setHeight(height);
                ossResponseData.setTimes(times);
                ossResponse.setData(ossResponseData);
                return ossResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    realUpload(parentId, name, data, false);
                }
            } else {
                ossResponse.setStatus(ResponseCode.FAILURE.code());
                ossResponse.setMsg(resMsg);
                return ossResponse;
            }
        } else {
            ossResponse.setMsg("接口无响应");
        }
        return ossResponse;
    }

    /**
     * 上传对象 ( multipart )
     *
     * @return
     */
    public OssQmResponse multipartUpload(Long parentId, String filePath) {
        File tmpFile = new File(filePath);
        if (!tmpFile.exists()) {
            OssQmResponse ossResponse = new OssQmResponse();
            ossResponse.setStatus(0);
            ossResponse.setMsg("file not exist");
            return ossResponse;
        }
        long fileSize = tmpFile.length();
        //如果文件大于5M自动采用分片上传
        if (fileSize > 1024 * 1024 * 5) {
            return chunkUpload(parentId, filePath);
        }
        return realMultipartUpload(parentId, filePath, true);
    }

    private OssQmResponse realMultipartUpload(Long parentId, String filePath, boolean isRetry) {
        filePath = filePath.replaceAll("\\\\", "/");
        OssQmResponse ossResponse = new OssQmResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILURE.code()) {
            ossResponse.setStatus(ResponseCode.FAILURE.code());
            ossResponse.setMsg(getAccessTokenResponse.getMsg());
            return ossResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        String uploadUrl = String.format(OssQmUrls.MULTIPART_UPLOAD, accessToken);
        Map<String, String> params = new HashMap<>();
        params.put("parentId", String.valueOf(parentId));
        String result = HttpUtils.multiFormDataUpload(uploadUrl, filePath, params);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String resMsg = resJson.getString("resMsg");
            if (code == 200) {
                String url = resJson.getString("url");
                Integer width = resJson.getInteger("width");
                Integer height = resJson.getInteger("height");
                Integer times = resJson.getInteger("times");
                ossResponse.setStatus(ResponseCode.SUCCESS.code());
                ossResponse.setMsg("success");
                OssQmResponseData ossResponseData = new OssQmResponseData();
                ossResponseData.setUrl(url);
                ossResponseData.setWidth(width);
                ossResponseData.setHeight(height);
                ossResponseData.setTimes(times);
                ossResponse.setData(ossResponseData);
                return ossResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    realMultipartUpload(parentId, filePath, false);
                }
            } else {
                ossResponse.setStatus(ResponseCode.FAILURE.code());
                ossResponse.setMsg(resMsg);
                return ossResponse;
            }
        } else {
            ossResponse.setMsg("接口无响应");
        }
        return ossResponse;
    }

    /**
     * 上传对象 ( 分片上传 )
     *
     * @return
     */
    private OssQmResponse chunkUpload(Long parentId, String filePath) {
        File tmpFile = new File(filePath);
        if (!tmpFile.exists()) {
            OssQmResponse ossResponse = new OssQmResponse();
            ossResponse.setStatus(0);
            ossResponse.setMsg("file not exist");
            return ossResponse;
        }
        long fileSize = tmpFile.length();
        //切割文件,默认每个文件切割为5M
        int blockSize = 1024 * 1024 * 5;
        //如果文件小于5M转为multipart上传
        if (fileSize <= blockSize) {
            return multipartUpload(parentId, filePath);
        }
        filePath = filePath.replaceAll("\\\\", "/");
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
        OssQmResponse ossResponse = new OssQmResponse();
        String identifier = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            long totalSize = new File(filePath).length();
            String targetFolder = FileUtils.cut(filePath, blockSize);
            File[] files = new File(targetFolder).listFiles();
            if (files == null || files.length == 0) {
                ossResponse.setStatus(0);
                ossResponse.setMsg("file empty");
                return ossResponse;
            }
            for (File file : files) {
                //上传分片文件
                OssQmResponse chunkUploadResponse = realChunkUpload(parentId, totalSize, files.length, file.getAbsolutePath(), identifier, filename, true);
                int state = chunkUploadResponse.getStatus();
                if (state == 1) {
                    if (!StringUtils.isBlank(chunkUploadResponse.getData().getUrl())) {
                        //清除分片文件
                        FileUtils.delFolder(targetFolder);
                        return chunkUploadResponse;
                    }
                } else {
                    ossResponse.setStatus(ResponseCode.FAILURE.code());
                    ossResponse.setMsg("chunk upload failure");
                    return ossResponse;
                }
            }
            ossResponse.setStatus(ResponseCode.FAILURE.code());
            ossResponse.setMsg("unknown error");
            return ossResponse;
        } catch (Exception e) {
            e.printStackTrace();
            ossResponse.setStatus(ResponseCode.FAILURE.code());
            ossResponse.setMsg("cut failure");
            return ossResponse;
        }
    }

    private OssQmResponse realChunkUpload(Long parentId, long totalSize, int totalChunks, String chunkFilePath, String identifier, String filename, boolean isRetry) {
        OssQmResponse ossResponse = new OssQmResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILURE.code()) {
            ossResponse.setStatus(ResponseCode.FAILURE.code());
            ossResponse.setMsg(getAccessTokenResponse.getMsg());
            return ossResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        String chunkUploadUrl = String.format(OssQmUrls.CHUNK_UPLOAD, accessToken);
        Map<String, String> params = new HashMap<>();
        int chunkNumber = Integer.parseInt(chunkFilePath.substring(chunkFilePath.lastIndexOf("-") + 1));
        params.put("parentId", String.valueOf(parentId));
        params.put("identifier", identifier);
        params.put("filename", filename);
        params.put("chunkNumber", String.valueOf(chunkNumber));
        params.put("totalChunks", String.valueOf(totalChunks));
        params.put("totalSize", String.valueOf(totalSize));
        String result = HttpUtils.multiFormDataUpload(chunkUploadUrl, chunkFilePath, params);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String resMsg = resJson.getString("resMsg");
            if (code == 200) {
                String url = resJson.getString("url");
                Integer width = resJson.getInteger("width");
                Integer height = resJson.getInteger("height");
                Integer times = resJson.getInteger("times");
                ossResponse.setStatus(ResponseCode.SUCCESS.code());
                ossResponse.setMsg("success");
                OssQmResponseData ossResponseData = new OssQmResponseData();
                ossResponseData.setUrl(url);
                ossResponseData.setWidth(width);
                ossResponseData.setHeight(height);
                ossResponseData.setTimes(times);
                ossResponse.setData(ossResponseData);
                return ossResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    realChunkUpload(parentId, totalSize, totalChunks, chunkFilePath, identifier, filename, false);
                }
            } else {
                ossResponse.setStatus(ResponseCode.FAILURE.code());
                ossResponse.setMsg(resMsg);
                return ossResponse;
            }
        } else {
            ossResponse.setMsg("接口无响应");
        }
        return ossResponse;
    }

}
