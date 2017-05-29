package com.lumosity.login;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

/**
 * 登陆功能简单的后台效验功能
 * @author Scott
 *
 */

public class LoginValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		
		validateRequiredString("accountInfo", "msg", "请输入用户名!");
		validateRequiredString("password", "msg", "请输入密码!");
		
	}

	@Override
	protected void handleError(Controller c) {
		// TODO Auto-generated method stub
		c.forwardAction("/login");
	}

	
}
