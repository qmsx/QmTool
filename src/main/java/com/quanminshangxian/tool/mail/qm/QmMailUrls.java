package com.quanminshangxian.tool.mail.qm;

public class QmMailUrls {

    //获取accessToken
    static final String GET_ACCESS_TOKEN = "http://api.email.quanminshangxian.com/api/getAccessToken";
    //发送普通邮件
    static final String SEND_EMAIL = "http://api.email.quanminshangxian.com/email/send?access_token=%s";
    //发送模板邮件
    static final String SEND_TPL_EMAIL = "http://api.email.quanminshangxian.com/email/tplSend?access_token=%s";

}
