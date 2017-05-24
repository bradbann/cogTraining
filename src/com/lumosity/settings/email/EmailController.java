package com.lumosity.settings.email;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;

public class EmailController extends Controller{

	/**
	 * 修改邮箱逻辑
	 * 1.接收传参
	 * 2.修改数据库
	 * 3.跳转/settings
	 */
	public void index() {
		Account account = getSessionAttr("userInfo");
		
		String email = getPara("email");
		Account valiAccount = Account.dao.findByEmail(email);
		
		if (valiAccount != null) {
			//修改邮箱已经被占用,不允许修改,跳转修改页面
			setAttr("msg", "error");
			forwardAction("/settings/login_info/edit");
			return;
		} 
		if ("".equals(email.trim()) || email == null) {
			redirect("/settings/login_info/edit");
			setAttr("msg", "邮箱不能为空");
		} else {
			account.set("email", email).update();
			redirect("/settings");
			
		}
	}
	
	public void edit() {
		render("/settings/email/change_email.html");
	}
}
