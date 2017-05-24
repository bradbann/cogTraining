package com.lumosity.common;

import java.util.List;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.QuestionStatus;
import com.lumosity.model.TestPlan;

/**
 * 简单的url拦截器
 * @author Scott
 *
 */
public class UrlFilterInterceptor implements Interceptor{

	@Override
	public void intercept(Invocation inv) {
		
		Controller c = inv.getController();
		Account account = c.getSessionAttr("userInfo");
		String controllerKey = inv.getControllerKey();
		
		Long userId = (long) 0;
		if (account != null) {
			userId = account.getUserId();
		} else {
		    userId = c.getParaToLong("userId");
			account = Account.dao.findById(userId);
		}
		
		if (account == null) {
			//用户没有登录，跳转登录页面，一些特殊页面需要放行
			if ("/index".equals(controllerKey) || "/".equals(controllerKey) || "/register".equals(controllerKey)
					|| "/forgot".equals(controllerKey)) {
				inv.invoke();
			} else {
				c.redirect("/login");
				c.setAttr("msg", "请登录");
			}
		} else {
			//用户已经登录，则过滤特殊url：/index /register 
			if ("/index".equals(controllerKey) || "/".equals(controllerKey) || "/register".equals(controllerKey)
					|| "/forgot".equals(controllerKey)) {
				c.redirect("/home");
			} else if ("/question".equals(controllerKey)) {
				QuestionStatus status = QuestionStatus.dao.findById(account.getUserId());
				if (status == null) {
					new QuestionStatus().set("userId", account.getUserId()).set("question1Status", 0).set("question2Status", 0).save();
					status = QuestionStatus.dao.findById(account.getUserId());
				}
				if (status.getQuestion1Status() == 1 && status.getQuestion2Status() == 1) {
					//完成问卷，跳转主页
					c.redirect("/home");
				} else {
					inv.invoke();
				}
			} else if ("/fitTest".equals(controllerKey)) {
				List<TestPlan> testPlans = TestPlan.dao.remaining(account.getUserId());
				if (testPlans.size() == 0) {
					//完成适应性训练，跳转主页
					c.redirect("/home");
				} else {
					inv.invoke();
				}
			} else {
				inv.invoke();
			}
			
		}
		
		
		
	}

}
