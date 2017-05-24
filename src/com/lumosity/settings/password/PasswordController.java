package com.lumosity.settings.password;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;

public class PasswordController extends Controller{

	public void index() {
		Account account = getSessionAttr("userInfo");
		String password = getPara("password");
		String password_confirmation = getPara("password_confirmation");
		if (password == null || "".equals(password.trim()) 
			|| password_confirmation == null || "".equals(password_confirmation.trim())) {
			setAttr("message", "不能为空");
		} else {
			
			if (password.equals(password_confirmation)) {
				account.set("password", password).update();
				redirect("/settings");
			} else {
				forwardAction("/settings/password/edit");
				setAttr("message", "请输入相同密码");
			}
		}
	}
	
	public void edit() {
		render("/settings/password/update_password.html");
	}
}
