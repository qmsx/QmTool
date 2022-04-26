package com.quanminshangxian.tool.code;

public enum ResponseCode {

    SUCCESS(200, "success"),
    FAILED(500, "failed");

    public int code;
    public String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
