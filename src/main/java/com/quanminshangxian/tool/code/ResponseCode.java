package com.quanminshangxian.tool.code;

public enum ResponseCode {

    SUCCESS(1, "成功"),
    FAILURE(0, "失败");

    private int code;
    private String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int code() {
        return code;
    }

    public String desc() {
        return desc;
    }
}
