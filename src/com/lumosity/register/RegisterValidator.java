package com.lumosity.register;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class RegisterValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		
		validateRequiredString("userName", "nameMsg", "用户名不能为空");
		validateRequiredString("email", "emailMsg", "邮箱不能为空");
		validateRequiredString("password", "pwdMsg", "密码不能为空");
		validateRequiredString("birthday", "birthMsg", "年份不能为空");
//		validateRequiredString("date_of_birth(2i)", "birthMsg", "年份不能为空");
//		validateRequiredString("date_of_birth(3i)", "birthMsg", "年份不能为空");
//		validateEmail("email", "emailMsg", "请输入正确的邮箱格式");

	}

	@Override
	protected void handleError(Controller c) {
		// TODO Auto-generated method stub
		c.forwardAction("/register");
	}

}
