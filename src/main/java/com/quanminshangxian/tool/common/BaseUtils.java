package com.quanminshangxian.tool.common;

import java.util.UUID;

public class BaseUtils {

    /**
     * 生成uuid
     *
     * @return
     */
    public static String genUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成uuid (无分隔符)
     *
     * @return
     */
    public static String genUuidNoSeparator() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
