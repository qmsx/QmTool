package com.quanminshangxian.tool.oss;

import java.io.Serializable;

public class QmOssGetNetUrlResponse implements Serializable {

    // 0 失败 1 成功
    private int status = 0;
    private String msg = "unknown error";
    private String url;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
