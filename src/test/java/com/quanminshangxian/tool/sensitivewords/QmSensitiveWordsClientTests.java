package com.quanminshangxian.tool.sensitivewords;

import com.alibaba.fastjson.JSON;
import com.quanminshangxian.tool.model.SendResponse;
import org.junit.Test;


public class QmSensitiveWordsClientTests {

    private QmSensitiveWordsClient qmSensitiveWordsClient = QmSensitiveWordsClient.build("", "");

    /**
     * 敏感文字检测
     */
    @Test
    public void wordCheck() {
        String content = "hello";
        SendResponse commonResponse = qmSensitiveWordsClient.wordCheck(content);
        System.out.println(JSON.toJSONString(commonResponse));
    }


}
