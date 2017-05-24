package com.lumosity.utils;

import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

public class EmailKit {
	
	/**
	 * FIXME smtp地址需改成配置项，邮件正文代码略微复杂应可简化
	 * @param toEmail
	 * @param title
	 * @param content
	 */
	public static boolean send(String toEmail, String title, String content) {
		
		
		boolean flag = true;
		try {
			Prop emailProp = PropKit.use("email.properties");
			String fromEmail = emailProp.get("fromEmail");

			Properties props = new Properties();
			props.put("mail.smtp.host", emailProp.get("smtpHost"));
			props.put("mail.smtp.auth", "true"); // 这样才能通过验证
			
			//ssl 验证  部分非企业邮箱
			props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.setProperty("mail.smtp.port", "465");
			props.setProperty("mail.smtp.socketFactory.port", "465");
			
			Session s = Session.getInstance(props);
			s.setDebug(true);

			MimeMessage message = new MimeMessage(s);

			// 给消息对象设置发件人/收件人/主题/发信时间
			InternetAddress from = new InternetAddress(fromEmail); // 发邮件的出发地（发件人的信箱）
			message.setFrom(from);
			InternetAddress to = new InternetAddress(toEmail);// 发邮件的目的地（收件人信箱）
			message.setRecipient(Message.RecipientType.TO, to);
			message.setSubject(title);
			message.setSentDate(new Date());
			
			

			// 给消息对象设置内容
			BodyPart mdp = new MimeBodyPart();// 新建一个存放信件内容的BodyPart对象
			mdp.setContent(content, "text/html;charset=gb2312");// 给BodyPart对象设置内容和格式/编码方式
			Multipart mm = new MimeMultipart();// 新建一个MimeMultipart对象用来存放BodyPart对象(事实上可以存放多个)
			mm.addBodyPart(mdp);// 将BodyPart加入到MimeMultipart对象中(可以加入多个BodyPart)
			message.setContent(mm);// 把mm作为消息对象的内容

			message.saveChanges();
			Transport transport = s.getTransport("smtp");
			transport.connect(emailProp.get("smtpHost"), fromEmail, emailProp.get("fromPassword"));
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

		} catch (MessagingException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
		
	}
	
}
