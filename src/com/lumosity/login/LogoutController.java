package com.lumosity.login;

import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.lumosity.common.UrlFilterInterceptor;
import com.lumosity.model.Session;
/**
 * 登出功能实现
 * 1.清除保存的userInfo的session对象
 * 2.跳转登录页面
 * @author Scott
 *
 */
@Clear({UrlFilterInterceptor.class, LoginSessionIntercepter.class})
public class LogoutController extends Controller{

	
	public void index() {
		//获取session对象并判断是否为空
		if (getSessionAttr("userInfo") != null) {
			//清除session
			String sessionId = getCookie("cogTrainingId");
			if (sessionId != null) {
				//清除对应的session数据库中的数据
				Session.dao.deleteById(sessionId);
			}
			removeSessionAttr("userInfo");
			removeCookie("cogTrainingId");
			redirect("/");
		} else {
			//session为空，直接跳转登录页面
			redirect("/");
		}
	}
}
