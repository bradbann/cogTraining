package com.lumosity.question;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.QuestionStatus;

/**
 * 问卷拦截器，不允许跳过
 * @author Scott
 *
 */
public class QuestionInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		
		Controller c = inv.getController();
		Account account = c.getSessionAttr("userInfo");
		Long userId = (long) 0;
		if (account != null)
			userId = account.getUserId();
		else
		    userId = c.getParaToLong("userId");
		QuestionStatus status = QuestionStatus.dao.findById(userId);
//		QuestionStatus status = QuestionStatus.dao.findById(account.getUserId());
		
		int status1 = 0;
		int status2 = 0;
		
		if (status != null) {
			status1 = status.getQuestion1Status();
			status2 = status.getQuestion2Status();
			
		}
		
		if (status1 == 0 && status2 == 0) {
			c.redirect("/question");
		} else if (status1 == 1 && status2 == 0) {
			c.redirect("/question/secondQuestion");
		} else if (status1 == 0 && status2 == 1) {
			c.redirect("/question");
		} else {
			inv.invoke();
		}
		

	}

}
