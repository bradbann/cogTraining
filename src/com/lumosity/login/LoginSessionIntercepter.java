package com.lumosity.login;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.Session;

/**
 * 
 * @author Scott
 *
 */
public class LoginSessionIntercepter implements Interceptor {

	
	@Override
	public void intercept(Invocation inv) {
		Account account = null;
		Controller c = inv.getController();
		//特殊url如/index需要跳转至/home
//		String key = inv.getControllerKey();
		
		String sessionId = c.getCookie("cogTrainingId");
		if (sessionId != null) {
			Session session = Session.dao.findById(sessionId);
			
			account = Account.dao.findById(session.getUserId());
			if (account != null) {
				c.setSessionAttr("userInfo", account);
			} else {
				c.removeCookie("cogTrainingId");
			}
		}
		inv.invoke();
	}

}
