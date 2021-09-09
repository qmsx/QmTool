package com.quanminshangxian.tool.oss;

import java.io.Serializable;

public class OssQmResponse implements Serializable {

    // 0 失败 1 成功
    private int status = 0;
    private String msg = "unknown error";
    private OssQmResponseData data;

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

    public OssQmResponseData getData() {
        return data;
    }

    public void setData(OssQmResponseData data) {
        this.data = data;
    }

}
