package com.quanminshangxian.tool.mail.ali;

import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.model.SendResponse;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class AliMailClient {

    /**
     * 发送邮件
     */
    public static SendResponse sendMail(
            String senderEmailAccount, String senderEmailPassword, String senderNickName,
            String receiveMail, String subject, String content, List<String> filePaths) {
        SendResponse emailResponse = new SendResponse();
        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        try {
            Properties props = new Properties();
            // 表示SMTP发送邮件，需要进行身份验证
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", "smtp.mxhichina.com");
            // 如果使用ssl，则去掉使用25端口的配置，进行如下配置,
            String smtpPort = "465";
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", smtpPort);
            props.put("mail.smtp.port", smtpPort);
            // 发件人的账号，填写控制台配置的发信地址,比如xxx@xxx.com
            props.put("mail.user", senderEmailAccount);
            // 访问SMTP服务时需要提供的密码(在控制台选择发信地址进行设置)
            props.put("mail.password", senderEmailPassword);
            // 构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // 用户名、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            };
            // 使用环境属性和授权信息，创建邮件会话
            Session session = Session.getInstance(props, authenticator);
            session.setDebug(false);                                 // 设置为debug模式, 可以查看详细的发送 log
            // 3. 创建一封邮件
            MimeMessage message = createMimeMessage(session, senderEmailAccount, senderNickName, receiveMail, subject, content,
                    null, null, null, filePaths);
            // 发送邮件
            Transport.send(message);

            emailResponse.setStatus(ResponseCode.SUCCESS.code());
            emailResponse.setMsg("发送成功");
            return emailResponse;
        } catch (Exception e) {
            e.printStackTrace();
            emailResponse.setStatus(ResponseCode.FAILURE.code());
            emailResponse.setMsg("发送失败，阿里企业邮箱服务连接失败");
            return emailResponse;
        }
    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session            和服务器交互的会话
     * @param receiveMail        收件人邮箱
     * @param subject            主题
     * @param addReceiveMailList 添加收件人
     * @param msReceiveMailList  密送收件人
     * @return
     * @throws Exception
     */
    private static MimeMessage createMimeMessage(
            Session session, String senderEmailAccount, String senderNickName, String receiveMail,
            String subject, String sendContent, List<String> addReceiveMailList, List<String> csReceiveMailList,
            List<String> msReceiveMailList, List<String> filePath) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
        message.setFrom(new InternetAddress(senderEmailAccount, senderNickName, "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "尊敬的用户", "UTF-8"));
        if (addReceiveMailList != null && addReceiveMailList.size() > 0) {
            for (String addReceiveMail : addReceiveMailList) {
                message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(addReceiveMail, "尊敬的用户", "UTF-8"));
            }
        }
        if (csReceiveMailList != null && csReceiveMailList.size() > 0) {
            for (String csReceiveMail : csReceiveMailList) {
                message.setRecipient(MimeMessage.RecipientType.CC, new InternetAddress(csReceiveMail, "尊敬的用户", "UTF-8"));
            }
        }
        if (msReceiveMailList != null && msReceiveMailList.size() > 0) {
            for (String msReceiveMail : msReceiveMailList) {
                message.setRecipient(MimeMessage.RecipientType.BCC, new InternetAddress(msReceiveMail, "尊敬的用户", "UTF-8"));
            }
        }

        // 4. Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）
        message.setSubject(subject, "UTF-8");

        // 5. Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
        Multipart multipart = new MimeMultipart();
        BodyPart contentPart = new MimeBodyPart();
        contentPart.setContent(sendContent, "text/html;charset=UTF-8");

        multipart.addBodyPart(contentPart);

        // 附件操作
        if (filePath != null && filePath.size() > 0) {
            for (String filename : filePath) {
                BodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(filename);
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                // MimeUtility.encodeWord可以避免文件名乱码
                attachmentBodyPart.setFileName(MimeUtility.encodeText(source.getName()));
//                attachmentBodyPart.setFileName(new String(source.getName().getBytes("UTF-8"),"ISO8859-1"));

                multipart.addBodyPart(attachmentBodyPart);
            }
            // 移走集合中的所有元素
            filePath.clear();
        }
        // 将multipart对象放到message中
        message.setContent(multipart);
        // 6. 设置发件时间
        message.setSentDate(new Date());
        // 7. 保存设置
        message.saveChanges();
        return message;
    }

}
