package com.quanminshangxian.tool.code;

public enum ResponseCode {

    SUCCESS(1, "success"),
    FAILED(0, "failed");

    public int code;
    public String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
