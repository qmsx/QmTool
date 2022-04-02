package com.quanminshangxian.tool.form;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.common.StringUtils;
import com.quanminshangxian.tool.http.HttpUtils;
import com.quanminshangxian.tool.model.AccessTokenCache;
import com.quanminshangxian.tool.model.CommonResponse;
import com.quanminshangxian.tool.model.CreateOrderResponse;
import com.quanminshangxian.tool.model.GetAccessTokenResponse;
import com.quanminshangxian.tool.sms.QmSmsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public CommonResponse addData(String formCode, String dataCombType, List<Object> fieldDataList) {
        CommonResponse commonResponse = new CommonResponse();

        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                commonResponse.setStatus(ResponseCode.FAILED.code);
                commonResponse.setMsg(getAccessTokenResponse.getMsg());
                return commonResponse;
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
                    commonResponse.setStatus(ResponseCode.SUCCESS.code);
                    commonResponse.setMsg(msg);
                    return commonResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    commonResponse.setStatus(ResponseCode.FAILED.code);
                    commonResponse.setMsg(msg);
                    return commonResponse;
                }
            } else {
                commonResponse.setStatus(ResponseCode.FAILED.code);
                commonResponse.setMsg("接口无响应");
                return commonResponse;
            }
        }
        return commonResponse;
    }

    /**
     * 导入文本数据文件
     */
    public CommonResponse impText() {

        return null;
    }

    /**
     * 导入excel数据文件
     */
    public CommonResponse impExcel() {

        return null;
    }

    /**
     * 获取excel文件导出路径
     *
     * @param formCode  表单代码
     * @param expiresIn 过期时间，单位秒
     */
    public CommonResponse getExcelExportUrl(String formCode, int expiresIn) {
        CommonResponse commonResponse = new CommonResponse();

        int EXEC_COUNT = 0;
        int MAX_COUNT = 2;
        while (EXEC_COUNT < MAX_COUNT) {
            EXEC_COUNT++;

            GetAccessTokenResponse getAccessTokenResponse = getAccessToken(appid, appsecret, false);
            int getAccessTokenResponseStatus = getAccessTokenResponse.getStatus();
            if (getAccessTokenResponseStatus == ResponseCode.FAILED.code) {
                commonResponse.setStatus(ResponseCode.FAILED.code);
                commonResponse.setMsg(getAccessTokenResponse.getMsg());
                return commonResponse;
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
                    commonResponse.setStatus(ResponseCode.SUCCESS.code);
                    commonResponse.setMsg(msg);
                    commonResponse.setData(excelUrl);
                    return commonResponse;
                } else if (code == 301) {
                    //如果服务端返回失效,则强制重新获取
                    getAccessToken(appid, appsecret, true);
                } else {
                    commonResponse.setStatus(ResponseCode.FAILED.code);
                    commonResponse.setMsg(msg);
                    return commonResponse;
                }
            } else {
                commonResponse.setStatus(ResponseCode.FAILED.code);
                commonResponse.setMsg("接口无响应");
                return commonResponse;
            }
        }
        return commonResponse;
    }

}
