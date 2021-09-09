package com.quanminshangxian.tool.sms;

import org.junit.Test;

public class SmsClientTests {

    @Test
    public void send() {
        SmsQmClient.build("", "").send("", "", "");
    }

}
