package com.lumosity.register;

import java.util.Date;
import java.util.regex.Pattern;

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
	
	public static final String emailReg = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
	public static final String phoneReg = "^1[3|4|5|7|8][0-9]{9}$";

	public void index() {
		render("/register.html");
	}
	/**
	 * 注册功能的简单实现（数据库主键id已设置自增，增幅为1）
	 */
//	@Before(RegisterValidator.class)
	public void save() {
		
		String accountInfo = getPara("accountInfo");
		
		Account account = Account.dao.isAccountExist(accountInfo);
		if (account != null) {
			//email或手机号重复，不允许注册
			setAttr("msg", "邮箱或手机号已被注册！请重新输入");
			forwardAction("/register");
			return;
		} 
		//email或手机号不重复，允许注册跳转
		String code = getPara("code");
		//验证验证码的有效性
		if (!code.equals(CacheKit.get("valiCode", accountInfo))) {
			setAttr("msg", "无效的验证码！");
			forwardAction("/register");
			return;
		}
		String userName = getPara("userName");
		String password = getPara("password");
		Date birth = getParaToDate("date");
	
		Account loginInfo = null;
		
		if (Pattern.matches(emailReg, accountInfo)) {
			//邮箱
			new Account().set("userName", userName).set("email", accountInfo).set("createDate", DateTimeKit.getDate())
				.set("password", password).set("birthday", birth).save();
			loginInfo = Account.dao.findByEmailAndPwd(accountInfo, password);
		} else if (Pattern.matches(phoneReg, accountInfo)) {
			//手机
			new Account().set("userName", userName).set("mobileId", accountInfo).set("createDate", DateTimeKit.getDate())
				.set("password", password).set("birthday", birth).save();
			loginInfo = Account.dao.findByPhoneAndPwd(accountInfo, password);
		} else {
			//非法输入
			setAttr("msg", "你输入的邮箱或者手机号非法！请重新输入");
			return;
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
