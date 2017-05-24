package com.lumosity.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;

import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.EmailKit;
import com.lumosity.utils.SmsKit;

public class TestStr {
	
	public static void main(String[] args) {
		
//		String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";  
//		String str = "*adCVs*34_a _09_b5*[/435^*&城池()^$$&*).{}+.|.)%%*(*.中国}34{45[]12.fd'*&999下面是中文的字符￥……{}【】。，；’“‘”？";
//        Pattern   p   =   Pattern.compile(regEx);     
//        Matcher   m   =   p.matcher(str);   
//        str = m.replaceAll("").trim();  
//        System.out.println(str);
//		int gameId = 1;
//		String gameName = "测试";
//		String userName = "管理员";
//		String json = "{\"fit_test\":\"1\",\"game_id\":\""+gameId+"\",\"game_param\":\""+gameName+"\",\"username\":\""+userName+"\",\"token\":\""
//				+ "uqiwejhbaskjdbasdioqw\",\"game_user_setting\":{\"level\":\"1\",\"time\":\"5\",\"score\":\"0\"},\"updated_at\":\""+DateTimeKit.getPlayDate()+"\"}";
//		System.out.println(json);
		String code = RandomStringUtils.random(4, "0123456789");
		String content = "【认知训练平台】你的验证码为:" + code;
		EmailKit.send("295005909@qq.com", "验证码", content);
//		SmsKit.send("15158005964","2323");
	}

}
