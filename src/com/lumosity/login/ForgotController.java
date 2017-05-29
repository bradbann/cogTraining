package com.lumosity.login;

import java.util.UUID;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.lumosity.model.Account;

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
	
	public void resetPassword() {

		String accountInfo = getPara("accountInfo");
		String code = getPara("code","");
		
		if (StrKit.isBlank(accountInfo)) {
			setAttr("msg", "邮箱或手机号不能为空！");
			forwardAction("/forgot");
			return;
		}
		
		if (!code.equals(CacheKit.get("valiCode", accountInfo))) {
			setAttr("msg", "无效的验证码！");
			forwardAction("/forgot");
		} else {
			Account account = Account.dao.isAccountExist(accountInfo);
			Long userId = account.getUserId();
			String uuid = UUID.randomUUID().toString();
			CacheKit.put("resetPwdCode", userId, uuid);
			setAttr("token", uuid);
			setAttr("userId", userId);
			setAttr("msg", "doRestPwd");
			render("/reset_password.html");
		}
	}
	
	
	public void doResetPassword(){
		
		String password = getPara("password");
		Long userId = getParaToLong("userId");
		String token = getPara("token","");
		if (StrKit.isBlank(password)) {
			setAttr("msg", "密码不能为空");
		} else {
			if (token.equals(CacheKit.get("resetPwdCode", userId))) {
				//授权成功
				Account.dao.findById(userId).set("password", password).update();
				setAttr("msg", "success");
			} else {
				setAttr("msg", "授权信息已过期，请重新获取！");
				forwardAction("/forgot");
				return;
			}
		}
		
		render("/reset_password.html");
		
	}
}
