package com.lumosity.login;

import java.util.UUID;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.lumosity.common.UrlFilterInterceptor;
import com.lumosity.model.Account;
import com.lumosity.model.Session;

@Clear({UrlFilterInterceptor.class})
public class LoginController extends Controller{

	/**
	 * 登录功能的初步实现
	 * 1.根据表单传递的数据来数据库查找用户
	 * 2.保存session对象
	 * 3.跳转home页面
	 */
	public void index() {
		Account account = getSessionAttr("userInfo");
		if (account == null) {
			render("/login.html");
		} else {
			redirect("/home");
		}
	}
	
	@Before(LoginValidator.class)
	public void doLogin(){
		//根据用户名密码查找用户
		String accountInfo = getPara("accountInfo");
		String pwd = getPara("password");
		boolean keepLogin = getParaToBoolean("remember", false);
		
		Account account = Account.dao.findAccount(accountInfo, pwd);
		
		if (account != null) {
			//找到用户
			setSessionAttr("userInfo", account);
			
			Long liveSeconds = (long) (keepLogin ? 365 * 24 * 60 * 60 : 120 * 60);
			int maxAgeInSeconds = (int) (keepLogin ? liveSeconds : -1);
			Long expireTime = System.currentTimeMillis() + (liveSeconds * 1000);
			
			if (keepLogin) {
				Session session = new Session();
				String sessionId = UUID.randomUUID().toString();
				session.setSessionId(sessionId);
				session.setUserId(account.getUserId());
				session.setExpireTime(expireTime);
				session.save();
				setCookie("cogTrainingId", sessionId, maxAgeInSeconds, true);
			}
			redirect("/home");
		} else {
			//用户不存在
			setAttr("msg", "账号或密码错误！请重新输入");
			forwardAction("/login");
		}
		
		
	}
	
}
