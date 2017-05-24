package com.lumosity.bottom;

import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.lumosity.common.UrlFilterInterceptor;

@Clear({UrlFilterInterceptor.class})
public class AboutUsController extends Controller {

	public void index() {
		render("/bottom/about.html");
	}
	
	@ActionKey("/legal")
	public void legal(){
		render("/bottom/terms_of_service.html");
	}
	
	@ActionKey("/managerLogin")
	public void managerLogin(){
		render("/bottom/manager_login.html");
	}
	
	@ActionKey("/payment")
	public void payment(){
		render("/bottom/payment.html");
	}
}
