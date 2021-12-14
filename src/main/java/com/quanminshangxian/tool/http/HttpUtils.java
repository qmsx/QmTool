package com.quanminshangxian.tool.http;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    private static final int CONNECTION_REQUEST_TIMEOUT = 30 * 1000; //从连接池获取连接的超时时间
    private static final int CONNECTION_TIMEOUT = 60 * 1000;  //握手的超时时间
    private static final int SOCKET_TIMEOUT = 60 * 1000; //数据包最大的间隔时间
    private static final int SO_TIMEOUT = 60 * 1000; //等待数据超时时间

    private static PoolingHttpClientConnectionManager poolConnManager;
    private static CloseableHttpClient closeableHttpClient;

    /**
     * 初始化块
     */
    static {
        ConnectionSocketFactory plainConnectionSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainConnectionSocketFactory)
                .register("https", sslConnectionSocketFactory)
                .build();
        poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        // Increase max total connection to 200
        poolConnManager.setMaxTotal(200);
        // Increase default max connection per route to 20
        poolConnManager.setDefaultMaxPerRoute(20);
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(SO_TIMEOUT).build();
        poolConnManager.setDefaultSocketConfig(socketConfig);

        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECTION_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        closeableHttpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager).setDefaultRequestConfig(requestConfig).build();
        if (poolConnManager.getTotalStats() != null) {
            System.out.println("new client pool " + poolConnManager.getTotalStats().toString());
        }
    }

    public static CloseableHttpClient getHttpClient() {
        return closeableHttpClient;
    }

    /**
     * get request
     */
    public static String getRequest(String url) {
        HttpGet httpRequest = new HttpGet(url);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = getHttpClient().execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                httpRequest.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * send post request
     */
    public static String postRequest(String url, Map<String, String> params) {
        HttpPost httpRequest = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        try {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                list.add(new BasicNameValuePair(key, params.get(key)));
            }
            httpRequest.setEntity(new UrlEncodedFormEntity(list, "UTF-8"));
            httpResponse = getHttpClient().execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                httpRequest.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * send post request
     */
    public static String postRequest(String url, String params) {
        HttpPost httpRequest = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        try {
            if (params != null) {
                httpRequest.setEntity(new StringEntity(params, "UTF-8"));
            }
            httpResponse = getHttpClient().execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                httpRequest.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * send post json request
     */
    public static String postRequestForJson(String url, String params) {
        HttpPost httpRequest = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        try {
            httpRequest.setHeader("content-type", "application/json");
            if (params != null) {
                httpRequest.setEntity(new StringEntity(params, "UTF-8"));
            }
            httpResponse = getHttpClient().execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                httpRequest.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * send put request
     *
     * @param url
     * @return
     */
    public static String putRequest(String url, String params) {
        HttpPut httpRequest = new HttpPut(url);
        CloseableHttpResponse httpResponse = null;
        try {
            if (params != null) {
                httpRequest.setEntity(new StringEntity(params, "UTF-8"));
            }
            httpResponse = getHttpClient().execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                httpRequest.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * send delete request
     *
     * @param url
     * @return
     */
    public static String deleteRequest(String url) {
        HttpDelete httpRequest = new HttpDelete(url);
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = getHttpClient().execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                httpRequest.abort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * multipart/form-data 上传
     */
    public static String multiFormDataUpload(String url, String filePath, Map<String, String> params) {
        HttpPost httpRequest = new HttpPost(url);
        CloseableHttpResponse httpResponse = null;
        try {
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            //处理文件 后面的setMode是用来解决文件名称乱码的问题:以浏览器兼容模式运行，防止文件名乱码。
            MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            builder.setCharset(StandardCharsets.UTF_8)
                    .addBinaryBody("file", fileInputStream, ContentType.MULTIPART_FORM_DATA, fileName);
            // 处理其他参数
            if (params != null) {
                for (String param : params.keySet()) {
                    builder.addTextBody(param, params.get(param));
                }
            }
            HttpEntity httpEntity = builder.build();
            httpRequest.setEntity(httpEntity);
            httpResponse = getHttpClient().execute(httpRequest);
            int status = httpResponse.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                return EntityUtils.toString(httpResponse.getEntity());
            } else {
                httpRequest.abort();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpRequest.releaseConnection();
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
