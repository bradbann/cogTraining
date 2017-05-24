package com.lumosity.games;

import java.util.List;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.TestPlan;

/**
 * 适应性计划完成情况拦截器
 * @author Scott
 *
 */
public class FitTestInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {
		
		Controller c = inv.getController();
		Account account = c.getSessionAttr("userInfo");
		Long userId = (long) 0;
		if (account != null)
			userId = account.getUserId();
		else
		    userId = c.getParaToLong("userId");
		//取出剩余适应性计划
		List<TestPlan> testPlans = TestPlan.dao.remaining(userId);
//		List<TestPlan> testPlans = TestPlan.dao.remaining(account.getUserId());
		if (testPlans.size() > 0) {
			//适应性计划还没完成，跳转计划界面
			c.redirect("/fitTest");
		}else {
			//完成适应性计划，放行
			inv.invoke();
		}
		
	}

}
