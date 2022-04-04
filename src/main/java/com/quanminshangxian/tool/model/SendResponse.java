package com.quanminshangxian.tool.model;

public class SendResponse {

    // 1：成功 0：失败
    private int status = 0;
    private String data;
    private String msg = "unknown error";

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
