package com.quanminshangxian.tool.mail.wy;

import com.quanminshangxian.tool.code.ResponseCode;
import com.quanminshangxian.tool.model.SendResponse;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 网易 163 邮箱发送
 */
public class Mail163Client {

    /**
     * send email
     */
    public static SendResponse sendMail(
            String senderEmailAccount, String senderEmailPassword, String senderNickName,
            String receiveMail, String subject, String content, List<String> filePaths) {
        SendResponse emailResponse = new SendResponse();
        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        try {
            Properties props = new Properties();
            props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
            props.setProperty("mail.smtp.host", "smtp.163.com");   // 发件人的邮箱的 SMTP 服务器地址
            props.setProperty("mail.smtp.auth", "true");            // 需要请求认证

            // PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
            //     如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
            //     打开下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。
            // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
            //                  需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助,
            //                  QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)
            final String smtpPort = "465";
            props.setProperty("mail.smtp.port", smtpPort);
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.socketFactory.port", smtpPort);

            System.setProperty("mail.mime.splitlongparameters", "false");
            // 2. 根据配置创建会话对象, 用于和邮件服务器交互
            Session session = Session.getInstance(props);
            session.setDebug(false);                                 // 设置为debug模式, 可以查看详细的发送 log

            // 3. 创建一封邮件
            MimeMessage message = createMimeMessage(
                    senderEmailAccount, senderEmailPassword, senderNickName,
                    session, receiveMail, subject, content,
                    null, null, null, filePaths);

            // 4. 根据 Session 获取邮件传输对象
            Transport transport = session.getTransport();

            // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
            //
            //    PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
            //           仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
            //           类型到对应邮件服务器的帮助网站上查看具体失败原因。
            //
            //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
            //           (1) 邮箱没有开启 SMTP 服务;
            //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
            //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
            //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
            //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
            //
            //    PS_03: 仔细看log, 认真看log, 看懂log, 错误原因都在log已说明。
            transport.connect(senderEmailAccount, senderEmailPassword);
            // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(message, message.getAllRecipients());
            // 7. 关闭连接
            transport.close();

            emailResponse.setStatus(ResponseCode.SUCCESS.code());
            emailResponse.setMsg("发送成功");
            return emailResponse;
        } catch (Exception e) {
            e.printStackTrace();
            emailResponse.setStatus(ResponseCode.FAILURE.code());
            emailResponse.setMsg("发送失败，网易163邮箱服务连接失败");
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
            String senderEmailAccount, String senderEmailPassword, String senderNickName,
            Session session, String receiveMail, String subject, String sendContent,
            List<String> addReceiveMailList, List<String> csReceiveMailList, List<String> msReceiveMailList,
            List<String> filePath) throws Exception {
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
