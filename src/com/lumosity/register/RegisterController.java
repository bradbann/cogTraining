package com.lumosity.register;

import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;

import com.jfinal.core.Controller;
import com.jfinal.plugin.ehcache.CacheKit;
import com.lumosity.model.Account;
import com.lumosity.model.QuestionStatus;
import com.lumosity.model.TestPlan;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.EmailKit;
import com.lumosity.utils.SmsKit;

//@Clear(UrlFilterInterceptor.class)
public class RegisterController extends Controller{

	public void index() {
		render("/register.html");
	}
	/**
	 * 注册功能的简单实现（数据库主键id已设置自增，增幅为1）
	 */
//	@Before(RegisterValidator.class)
	public void save() {
		
		String email = getPara("email");
		String phone = getPara("phone");
		String password = getPara("password");
		Account account = null;
		if (email != null) 
			account = Account.dao.findByEmail(email);
		else 
			account = Account.dao.findByEmail(phone);
		
		if (account != null) {
			//email或手机号重复，不允许注册
			setAttr("msg", "error");
			forwardAction("/register");

		} else {
			//email或手机号不重复，允许注册跳转
			String birthday = getPara("date");
			Date birth = DateTimeKit.parse(birthday);
		
			Account loginInfo = null;
			
			if (email != null) {
				//邮箱注册
				new Account().set("userName",getPara("userName")).set("email", email).set("createDate", DateTimeKit.getDate())
					.set("password", password).set("birthday", birth).save();
				loginInfo = Account.dao.findByEmailAndPwd(email, password);
			} else {
				//手机号注册
				new Account().set("userName",getPara("userName")).set("mobileId", phone).set("createDate", DateTimeKit.getDate())
				.set("password", password).set("birthday", birth).save();
				loginInfo = Account.dao.findByPhoneAndPwd(phone, password);
			}
			
			//保存session数据
			setSessionAttr("userInfo", loginInfo);
			Long userId = loginInfo.getUserId();
			//创建适应性训练计划(固定为三种类型游戏)
			new TestPlan().set("userId", userId).set("gameId", 14).set("sequence", 1).set("isPlay", 0).save();//speed_match
			new TestPlan().set("userId", userId).set("gameId", 2).set("sequence", 2).set("isPlay", 0).save();//lost_in_migration
			new TestPlan().set("userId", userId).set("gameId", 7).set("sequence", 3).set("isPlay", 0).save();//memory_matrix
			//创建用户问卷状态
			new QuestionStatus().set("userId", userId).set("question1Status", 0).set("question2Status", 0).save();
			//跳转问卷页面
			redirect("/question");
		}
	}
	
	/**
	 * 发送短信验证码
	 */
	public void sendSmsCode() {
		
		String phone = getPara(0);
		String code = RandomStringUtils.random(4, "0123456789");
		boolean flag = SmsKit.send(phone, code);
		if (flag)
			CacheKit.put("phoneCode", phone, code);//放入缓存，3分钟内有效
		else 
			setAttr("msg", "短信发送失败，请稍后重试");
		
	}
	
	public void sendEmailCode() {
		
		String email = getPara("email");
		String code = RandomStringUtils.random(4, "0123456789");
		
		String content = "【认知训练平台】你的验证码为:" + code;
		
		boolean flag = EmailKit.send(email, "验证码", content);
		if (flag)
			CacheKit.put("emailCode", email, code);
		else
			setAttr("msg", "邮件发送失败，请稍后重试");
		
	}
	
}
