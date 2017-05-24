package com.lumosity.common;

import com.jfinal.core.Controller;
/**
 * 判断本地有没有存储用户登录cookie
 * 1.有       跳转home
 * 2.没有   跳转index
 * @author Scott
 *
 */
public class IndexController extends Controller{
	
	public void index() {
		
		render("/index.html");
	}

}
