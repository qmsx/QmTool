package com.quanminshangxian.tool.model;

import java.io.Serializable;

public class SendResponse implements Serializable {

    // 0-失败 1-成功
    private int status = 0;
    private String msg = "unknown error";

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
}
