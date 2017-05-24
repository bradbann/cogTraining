package com.lumosity.login;

import java.util.UUID;

import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.lumosity.model.Account;
import com.lumosity.utils.EmailKit;

/**
 * 忘记密码控制器
 * @author Scott
 *
 */
//@Clear(UrlFilterInterceptor.class)
public class ForgotController extends Controller {

	public void index() {
		render("/forgot.html");
	}
	
	/**
	 * 发送重置密码申请
	 */
	public void doSendEmail() {
		String email = getPara("email");

		Account account = Account.dao.findByEmail(email);
		if (account == null) {
			setAttr("msg","error");
			forwardAction("/forgot");
			return;
		}

		String mailUUID = UUID.randomUUID().toString();
		
//		 配置缓存，15分钟有效
//		CacheKit.put("FindPasswordByEmail", String.valueOf(account.getUserId()), mailUUID);
		
		String content = "<a href=\"http://"
				+ PropKit.use("email.properties").get("resetHost") 
				+"/forgot/resetPassword?randCode="+mailUUID+"&userId="+account.getUserId()+"\">"
				+"访问下面链接即可重置密码</a>";
		EmailKit.send(email, "cogTraining邮箱找回密码", content);
		setAttr("message","邮件已发送，请登录邮箱进行验证!");
		render("/forgot.html");
	}
	
	public void resetPassword() {
		keepPara("randCode","userId");
		render("/reset_password.html");
	
	}
	public void doResetPassword(){
		
//		CacheKit.get("FindPasswordByEmail", getPara("userId")) .equals(getPara("randCode"));
//		StrKit.notBlank("userId","randCode");
		String code = getPara("randCode");
		String password = getPara("password");
		Integer userId = getParaToInt("userId");
		
		if (StrKit.notBlank(code,password)) {
			Account.dao.findById(userId).set("password", password).update();
		} else {
			setAttr("msg", "重置密码失败！请联系管理员！");
		}
		setAttr("msg", "success");
		render("/reset_password.html");
		
	}
}
