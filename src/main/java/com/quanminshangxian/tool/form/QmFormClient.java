package com.quanminshangxian.tool.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.common.StringUtils;
import com.quanminshangxian.tool.form.request.QmFormImpExcelRequestParam;
import com.quanminshangxian.tool.form.request.QmFormImpTextRequestParam;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.SendResponse;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QmFormClient {
    private static final Logger log = LoggerFactory.getLogger(QmFormClient.class);

    private String appid;
    private String appsecret;
    private static final Map<String, AccessTokenCache> accessTokenCacheMap = new ConcurrentHashMap<String, AccessTokenCache>();

    private QmFormClient(String appid, String appsecret) {
        this.appid = appid;
        this.appsecret = appsecret;
    }

    public static QmFormClient build(String appid, String appsecret) {
        return new QmFormClient(appid, appsecret);
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
        } else {
            //强制刷新前，先清空缓存
            accessTokenCacheMap.put(appid, null);
        }
        JSONObject params = new JSONObject();
        params.put("appid", appid);
        params.put("appsecret", appsecret);
        String result = HttpUtils.doPostRequestForJson(QmFormUrls.GET_ACCESS_TOKEN, params.toJSONString());
        if (result != null) {//重试一次
            JSONObject resJson = JSON.parseObject(result);
            int code = resJson.getIntValue("code");
            if (code == 200) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String access_token = dataJson.getString("access_token");
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
     * 添加数据
     *
     * @return
     */
    public SendResponse addData(String formCode, String dataCombType, List<Object> fieldDataList) {
        SendResponse sendResponse = new SendResponse();

        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg(getAccessTokenResponse.getMsg());
                return sendResponse;
            }
            String accessToken = getAccessTokenResponse.getAccessToken();
            Map<String, Object> params = new HashMap<>();
            params.put("formCode", formCode);
            params.put("dataCombType", dataCombType);
            params.put("fieldData", fieldDataList);
            String url = String.format(QmFormUrls.FORM_ADD_DATA, accessToken);
            log.info(url);
            log.info(JSON.toJSONString(params));
            String result = HttpUtils.doPostRequestForJson(url, JSONObject.toJSONString(params));
            if (!StringUtils.isBlank(result)) {
                JSONObject resJson = JSON.parseObject(result);
                int code = resJson.getIntValue("code");
                String msg = resJson.getString("msg");
                if (code == 200) {
                    sendResponse.setStatus(ResponseCode.SUCCESS.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    sendResponse.setStatus(ResponseCode.FAILED.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                }
            } else {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg("接口无响应");
                return sendResponse;
            }
        }
        return sendResponse;
    }

    /**
     * 导入文本数据文件
     */
    public SendResponse impText(QmFormImpTextRequestParam qmFormImpTextRequestParam) {
        SendResponse sendResponse = new SendResponse();
        String filePath = qmFormImpTextRequestParam.getFilePath();
        File tmpFile = new File(filePath);
        if (!tmpFile.exists()) {
            sendResponse.setStatus(0);
            sendResponse.setMsg("file not exist");
            return sendResponse;
        }
        long fileSize = tmpFile.length();
        if (fileSize > 1024 * 1024 * 1024) {
            //客户端限制大小暂定1GB
            sendResponse.setMsg("文件不能大于1GB");
            return sendResponse;
        }
        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            filePath = filePath.replaceAll("\\\\", "/");
            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg(getAccessTokenResponse.getMsg());
                return sendResponse;
            }
            String accessToken = getAccessTokenResponse.getAccessToken();
            String uploadUrl = String.format(QmFormUrls.FORM_IMP_TEXT, accessToken);
            Map<String, String> params = new HashMap<>();
            params.put("formCode", qmFormImpTextRequestParam.getFormCode());
            params.put("dataCombType", String.valueOf(qmFormImpTextRequestParam.getDataCombType()));
            params.put("separator", qmFormImpTextRequestParam.getSeparator());
            params.put("filePath", qmFormImpTextRequestParam.getFilePath());
            String result = HttpUtils.multiFormDataUpload(uploadUrl, filePath, params, null);
            log.info("uploadMultipart result:" + result);
            if (!StringUtils.isBlank(result)) {
                JSONObject resJson = JSON.parseObject(result);
                int code = resJson.getIntValue("code");
                String msg = resJson.getString("msg");
                if (code == 200) {
                    sendResponse.setStatus(ResponseCode.SUCCESS.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    sendResponse.setStatus(ResponseCode.FAILED.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                }
            } else {
                sendResponse.setMsg("接口无响应");
                return sendResponse;
            }
        }
        return sendResponse;
    }

    /**
     * 导入excel数据文件
     */
    public SendResponse impExcel(QmFormImpExcelRequestParam qmFormImpExcelRequestParam) {
        SendResponse sendResponse = new SendResponse();
        String filePath = qmFormImpExcelRequestParam.getFilePath();
        File tmpFile = new File(filePath);
        if (!tmpFile.exists()) {
            sendResponse.setStatus(0);
            sendResponse.setMsg("file not exist");
            return sendResponse;
        }
        long fileSize = tmpFile.length();
        if (fileSize > 1024 * 1024 * 1024) {
            //客户端限制大小暂定1GB
            sendResponse.setMsg("文件不能大于1GB");
            return sendResponse;
        }
        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            filePath = filePath.replaceAll("\\\\", "/");
            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg(getAccessTokenResponse.getMsg());
                return sendResponse;
            }
            String accessToken = getAccessTokenResponse.getAccessToken();
            String uploadUrl = String.format(QmFormUrls.FORM_IMP_EXCEL, accessToken);
            Map<String, String> params = new HashMap<>();
            params.put("formCode", qmFormImpExcelRequestParam.getFormCode());
            params.put("dataCombType", String.valueOf(qmFormImpExcelRequestParam.getDataCombType()));
            params.put("separator", qmFormImpExcelRequestParam.getSeparator());
            params.put("filePath", qmFormImpExcelRequestParam.getFilePath());
            String result = HttpUtils.multiFormDataUpload(uploadUrl, filePath, params, null);
            log.info("uploadMultipart result:" + result);
            if (!StringUtils.isBlank(result)) {
                JSONObject resJson = JSON.parseObject(result);
                int code = resJson.getIntValue("code");
                String msg = resJson.getString("msg");
                if (code == 200) {
                    sendResponse.setStatus(ResponseCode.SUCCESS.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    sendResponse.setStatus(ResponseCode.FAILED.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                }
            } else {
                sendResponse.setMsg("接口无响应");
                return sendResponse;
            }
        }
        return sendResponse;
    }

    /**
     * 获取文本文件导出路径
     *
     * @param formCode     表单代码
     * @param expiresIn    过期时间，单位秒
     * @param dataCombType 数据组合类型
     * @param separator    数据分隔符
     * @param fileName     导出文件名，为空时默认使用【 表单名称.txt 】
     */
    public SendResponse getTextExportUrl(String formCode, int expiresIn, int dataCombType, String separator, String fileName) {
        SendResponse sendResponse = new SendResponse();

        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg(getAccessTokenResponse.getMsg());
                return sendResponse;
            }
            String accessToken = getAccessTokenResponse.getAccessToken();
            Map<String, Object> params = new HashMap<>();
            params.put("formCode", formCode);
            params.put("expiresIn", expiresIn);
            params.put("dataCombType", dataCombType);
            params.put("separator", separator);
            if (!StringUtils.isBlank(fileName)) {
                params.put("fileName", fileName);
            }
            String url = String.format(QmFormUrls.FORM_GET_TEXT_EXPORT_URL, accessToken);
            log.info(url);
            log.info(JSON.toJSONString(params));
            String result = HttpUtils.doPostRequestForJson(url, JSONObject.toJSONString(params));
            if (!StringUtils.isBlank(result)) {
                JSONObject resJson = JSON.parseObject(result);
                int code = resJson.getIntValue("code");
                String msg = resJson.getString("msg");
                if (code == 200) {
                    JSONObject dataJson = resJson.getJSONObject("data");
                    String excelUrl = dataJson.getString("url");
                    sendResponse.setStatus(ResponseCode.SUCCESS.code);
                    sendResponse.setMsg(msg);
                    sendResponse.setData(excelUrl);
                    return sendResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    sendResponse.setStatus(ResponseCode.FAILED.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                }
            } else {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg("接口无响应");
                return sendResponse;
            }
        }
        return sendResponse;
    }

    /**
     * 获取excel文件导出路径
     *
     * @param formCode  表单代码
     * @param expiresIn 过期时间，单位秒
     */
    public SendResponse getExcelExportUrl(String formCode, int expiresIn) {
        SendResponse sendResponse = new SendResponse();

        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg(getAccessTokenResponse.getMsg());
                return sendResponse;
            }
            String accessToken = getAccessTokenResponse.getAccessToken();
            Map<String, Object> params = new HashMap<>();
            params.put("formCode", formCode);
            params.put("expiresIn", expiresIn);
            String url = String.format(QmFormUrls.FORM_GET_EXCEL_EXPORT_URL, accessToken);
            log.info(url);
            log.info(JSON.toJSONString(params));
            String result = HttpUtils.doPostRequestForJson(url, JSONObject.toJSONString(params));
            if (!StringUtils.isBlank(result)) {
                JSONObject resJson = JSON.parseObject(result);
                int code = resJson.getIntValue("code");
                String msg = resJson.getString("msg");
                if (code == 200) {
                    JSONObject dataJson = resJson.getJSONObject("data");
                    String excelUrl = dataJson.getString("url");
                    sendResponse.setStatus(ResponseCode.SUCCESS.code);
                    sendResponse.setMsg(msg);
                    sendResponse.setData(excelUrl);
                    return sendResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    sendResponse.setStatus(ResponseCode.FAILED.code);
                    sendResponse.setMsg(msg);
                    return sendResponse;
                }
            } else {
                sendResponse.setStatus(ResponseCode.FAILED.code);
                sendResponse.setMsg("接口无响应");
                return sendResponse;
            }
        }
        return sendResponse;
    }

}
