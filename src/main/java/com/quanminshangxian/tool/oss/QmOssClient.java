package com.quanminshangxian.tool.oss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.common.StringUtils;
import com.quanminshangxian.tool.file.FileUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.mail.qm.QmMailClient;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QmOssClient {

    private static final Logger log = LoggerFactory.getLogger(QmOssClient.class);

    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();
    private String appid;
    private String appsecret;

    private QmOssClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static QmOssClient build(String appid, String appsecret) {
        return new QmOssClient(appid, appsecret);
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
        String result = HttpUtils.postRequest(QmOssUrls.GET_ACCESS_TOKEN, params.toJSONString());
        log.info("GET_ACCESS_TOKEN result:" + result);
        if (result != null) {//重试一次
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String access_token = dataJson.getString("access_token");
                //单位为秒
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
     * 上传对象 ( base64 )
     *
     * @return
     */
    public QmOssUploadResponse uploadBase64(QmOssBase64RequestParam qmOssBase64RequestParam) {
        String base64 = qmOssBase64RequestParam.getBase64();
        if (base64.length() > 1024 * 1024 * 5) {
            QmOssUploadResponse ossResponse = new QmOssUploadResponse();
            ossResponse.setStatus(0);
            ossResponse.setMsg("base64方式上传文件不能大于5M");
            return ossResponse;
        }
        return realUploadBase64(qmOssBase64RequestParam, true);
    }

    private QmOssUploadResponse realUploadBase64(QmOssBase64RequestParam qmOssBase64RequestParam, boolean isRetry) {
        QmOssUploadResponse ossResponse = new QmOssUploadResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            ossResponse.setStatus(ResponseCode.FAILED.code);
            ossResponse.setMsg(getAccessTokenResponse.getMsg());
            return ossResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        JSONObject params = new JSONObject();
        params.put("accessAuth", qmOssBase64RequestParam.getAccessAuth());
        params.put("parentId", qmOssBase64RequestParam.getParentId());
        params.put("name", qmOssBase64RequestParam.getOssName());
        params.put("data", qmOssBase64RequestParam.getBase64());
        String uploadUrl = String.format(QmOssUrls.UPLOAD_BASE64, accessToken);
        String result = HttpUtils.postRequest(uploadUrl, params.toJSONString());
        log.info("uploadBase64 result:" + result);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String msg = resJson.getString("msg");
            if (code == 200) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String ossId = dataJson.getString("ossId");
                String url = dataJson.getString("url");
                Integer width = dataJson.getInteger("width");
                Integer height = dataJson.getInteger("height");
                Integer times = dataJson.getInteger("times");
                ossResponse.setStatus(ResponseCode.SUCCESS.code);
                ossResponse.setMsg("success");
                QmOssResponseData ossResponseData = new QmOssResponseData();
                ossResponseData.setOssId(ossId);
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
                    realUploadBase64(qmOssBase64RequestParam, false);
                }
            } else {
                ossResponse.setStatus(ResponseCode.FAILED.code);
                ossResponse.setMsg(msg);
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
    public QmOssUploadResponse uploadMultipart(QmOssFileRequestParam qmOssFileRequestParam) {
        String filePath = qmOssFileRequestParam.getFilePath();
        String parentId = qmOssFileRequestParam.getParentId();
        File tmpFile = new File(filePath);
        if (!tmpFile.exists()) {
            QmOssUploadResponse ossResponse = new QmOssUploadResponse();
            ossResponse.setStatus(0);
            ossResponse.setMsg("file not exist");
            return ossResponse;
        }
        long fileSize = tmpFile.length();
        //如果文件大于5M自动采用分片上传
        if (fileSize > 1024 * 1024 * 5) {
            return uploadChunk(qmOssFileRequestParam);
        }
        return realUploadMultipart(qmOssFileRequestParam, true);
    }

    private QmOssUploadResponse realUploadMultipart(QmOssFileRequestParam qmOssFileRequestParam, boolean isRetry) {
        String filePath = qmOssFileRequestParam.getFilePath();
        String parentId = qmOssFileRequestParam.getParentId();
        filePath = filePath.replaceAll("\\\\", "/");
        QmOssUploadResponse ossResponse = new QmOssUploadResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            ossResponse.setStatus(ResponseCode.FAILED.code);
            ossResponse.setMsg(getAccessTokenResponse.getMsg());
            return ossResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        String uploadUrl = String.format(QmOssUrls.UPLOAD_MULTIPART, accessToken);
        Map<String, String> params = new HashMap<>();
        params.put("accessAuth", qmOssFileRequestParam.getAccessAuth());
        params.put("parentId", parentId);
        String result = HttpUtils.multiFormDataUpload(uploadUrl, filePath, params);
        log.info("uploadMultipart result:" + result);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String msg = resJson.getString("msg");
            if (code == 200) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String ossId = dataJson.getString("ossId");
                String url = dataJson.getString("url");
                Integer width = dataJson.getInteger("width");
                Integer height = dataJson.getInteger("height");
                Integer times = dataJson.getInteger("times");
                ossResponse.setStatus(ResponseCode.SUCCESS.code);
                ossResponse.setMsg("success");
                QmOssResponseData ossResponseData = new QmOssResponseData();
                ossResponseData.setOssId(ossId);
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
                    realUploadMultipart(qmOssFileRequestParam, false);
                }
            } else {
                ossResponse.setStatus(ResponseCode.FAILED.code);
                ossResponse.setMsg(msg);
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
    private QmOssUploadResponse uploadChunk(QmOssFileRequestParam qmOssFileRequestParam) {
        String filePath = qmOssFileRequestParam.getFilePath();
        String parentId = qmOssFileRequestParam.getParentId();
        File tmpFile = new File(filePath);
        if (!tmpFile.exists()) {
            QmOssUploadResponse ossResponse = new QmOssUploadResponse();
            ossResponse.setStatus(0);
            ossResponse.setMsg("file not exist");
            return ossResponse;
        }
        long fileSize = tmpFile.length();
        //切割文件,默认每个文件切割为5M
        int blockSize = 1024 * 1024 * 5;
        //如果文件小于5M转为multipart上传
        if (fileSize <= blockSize) {
            return uploadMultipart(qmOssFileRequestParam);
        }
        filePath = filePath.replaceAll("\\\\", "/");
        String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
        QmOssUploadResponse ossResponse = new QmOssUploadResponse();
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
                QmOssUploadResponse chunkUploadResponse = realUploadChunk(qmOssFileRequestParam, totalSize, files.length, file.getAbsolutePath(), identifier, filename, true);
                int state = chunkUploadResponse.getStatus();
                if (state == 1) {
                    if (!StringUtils.isBlank(chunkUploadResponse.getData().getUrl())) {
                        //清除分片文件
                        FileUtils.delFolder(targetFolder);
                        return chunkUploadResponse;
                    }
                } else {
                    ossResponse.setStatus(ResponseCode.FAILED.code);
                    ossResponse.setMsg("chunk upload failure");
                    return ossResponse;
                }
            }
            ossResponse.setStatus(ResponseCode.FAILED.code);
            ossResponse.setMsg("unknown error");
            return ossResponse;
        } catch (Exception e) {
            e.printStackTrace();
            ossResponse.setStatus(ResponseCode.FAILED.code);
            ossResponse.setMsg("cut failure");
            return ossResponse;
        }
    }

    private QmOssUploadResponse realUploadChunk(QmOssFileRequestParam qmOssFileRequestParam, long totalSize, int totalChunks, String chunkFilePath, String identifier, String filename, boolean isRetry) {
        QmOssUploadResponse ossResponse = new QmOssUploadResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            ossResponse.setStatus(ResponseCode.FAILED.code);
            ossResponse.setMsg(getAccessTokenResponse.getMsg());
            return ossResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        String chunkUploadUrl = String.format(QmOssUrls.UPLOAD_CHUNK, accessToken);
        Map<String, String> params = new HashMap<>();
        int chunkNumber = Integer.parseInt(chunkFilePath.substring(chunkFilePath.lastIndexOf("-") + 1));
        params.put("parentId", qmOssFileRequestParam.getParentId());
        params.put("identifier", identifier);
        params.put("filename", filename);
        params.put("chunkNumber", String.valueOf(chunkNumber));
        params.put("totalChunks", String.valueOf(totalChunks));
        params.put("totalSize", String.valueOf(totalSize));
        String result = HttpUtils.multiFormDataUpload(chunkUploadUrl, chunkFilePath, params);
        log.info("uploadChunk result:" + result);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String msg = resJson.getString("msg");
            if (code == 200) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String ossId = dataJson.getString("ossId");
                String url = dataJson.getString("url");
                Integer width = dataJson.getInteger("width");
                Integer height = dataJson.getInteger("height");
                Integer times = dataJson.getInteger("times");
                ossResponse.setStatus(ResponseCode.SUCCESS.code);
                ossResponse.setMsg("success");
                QmOssResponseData ossResponseData = new QmOssResponseData();
                ossResponseData.setOssId(ossId);
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
                    realUploadChunk(qmOssFileRequestParam, totalSize, totalChunks, chunkFilePath, identifier, filename, false);
                }
            } else {
                ossResponse.setStatus(ResponseCode.FAILED.code);
                ossResponse.setMsg(msg);
                return ossResponse;
            }
        } else {
            ossResponse.setMsg("接口无响应");
        }
        return ossResponse;
    }

    /**
     * 获取对象的外网访问链接
     */
    public QmOssGetNetUrlResponse getOssNetUrl(String ossId, int expireIn) {
        return getOssNetUrl(ossId, expireIn, true);
    }

    private QmOssGetNetUrlResponse getOssNetUrl(String ossId, int expireIn, boolean isRetry) {
        QmOssGetNetUrlResponse qmOssGetNetUrlResponse = new QmOssGetNetUrlResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            qmOssGetNetUrlResponse.setStatus(ResponseCode.FAILED.code);
            qmOssGetNetUrlResponse.setMsg(getAccessTokenResponse.getMsg());
            return qmOssGetNetUrlResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        String getNetUrl = String.format(QmOssUrls.GET_NET_URL, accessToken);
        JSONObject params = new JSONObject();
        params.put("ossId", ossId);
        params.put("expireIn", String.valueOf(expireIn));
        String result = HttpUtils.postRequest(getNetUrl, params.toJSONString());
        log.info("getOssNetUrl result:" + result);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String msg = resJson.getString("msg");
            if (code == 200) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String url = dataJson.getString("url");
                qmOssGetNetUrlResponse.setStatus(ResponseCode.SUCCESS.code);
                qmOssGetNetUrlResponse.setMsg("success");
                qmOssGetNetUrlResponse.setUrl(url);
                return qmOssGetNetUrlResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    getOssNetUrl(ossId, expireIn, false);
                }
            } else {
                qmOssGetNetUrlResponse.setStatus(ResponseCode.FAILED.code);
                qmOssGetNetUrlResponse.setMsg(msg);
                return qmOssGetNetUrlResponse;
            }
        } else {
            qmOssGetNetUrlResponse.setMsg("接口无响应");
        }
        return qmOssGetNetUrlResponse;
    }

    /**
     * 对象重命名
     */
    public QmOssRenameResponse rename(String ossId, String ossName) {
        return rename(ossId, ossName, true);
    }

    private QmOssRenameResponse rename(String ossId, String ossName, boolean isRetry) {
        QmOssRenameResponse qmOssRenameResponse = new QmOssRenameResponse();
        GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
        int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
        if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
            qmOssRenameResponse.setStatus(ResponseCode.FAILED.code);
            qmOssRenameResponse.setMsg(getAccessTokenResponse.getMsg());
            return qmOssRenameResponse;
        }
        String accessToken = getAccessTokenResponse.getAccessToken();
        String renameUrl = String.format(QmOssUrls.RENAME_URL, accessToken);
        JSONObject params = new JSONObject();
        params.put("ossId", ossId);
        params.put("ossName", ossName);
        String result = HttpUtils.postRequest(renameUrl, params.toJSONString());
        log.info("rename result:" + result);
        if (!StringUtils.isBlank(result)) {
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            String msg = resJson.getString("msg");
            if (code == 200) {
                qmOssRenameResponse.setStatus(ResponseCode.SUCCESS.code);
                qmOssRenameResponse.setMsg(msg);
                return qmOssRenameResponse;
            } else if (code == 301) {
                //如果服务端返回失效,则强制重新获取
                getAccessToken(appid, appsecret, true);
                if (isRetry) {
                    //重试后不再重试
                    rename(ossId, ossName, false);
                }
            } else {
                qmOssRenameResponse.setStatus(ResponseCode.FAILED.code);
                qmOssRenameResponse.setMsg(msg);
                return qmOssRenameResponse;
            }
        } else {
            qmOssRenameResponse.setMsg("接口无响应");
        }
        return qmOssRenameResponse;
    }

}
