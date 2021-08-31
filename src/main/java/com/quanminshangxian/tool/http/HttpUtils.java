package com.quanminshangxian.tool.http;

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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    public static final int CONNECTION_REQUEST_TIMEOUT = 30 * 1000; //从连接池获取连接的超时时间
    public static final int CONNECTION_TIMEOUT = 60 * 1000;  //握手的超时时间
    public static final int SOCKET_TIMEOUT = 60 * 1000; //数据包最大的间隔时间
    public static final int SO_TIMEOUT = 60 * 1000; //等待数据超时时间

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
     * 发送get请求
     */
    public static String sendGetRequest(String url) {
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
     * 发送post请求
     */
    public static String sendPostRequest(String url, Map<String, String> params) {
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
     * 发送post请求
     */
    public static String sendPostRequest(String url, String params) {
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
     * 发送post请求
     */
    public static String sendPostRequestForJson(String url, String params) {
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

    public static String sendPutRequest(String url, String params) {
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

    public static String sendDeleteRequest(String url) {
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

}
