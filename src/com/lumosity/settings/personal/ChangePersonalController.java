package com.lumosity.settings.personal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;
/**
 * 修改个人信息
 * 1.照片的上传与删除
 * 2.姓名、性别、时区、教育程度、职业的修改。
 * @author Scott
 *
 */
public class ChangePersonalController extends Controller{

	public void index() {
		
		Account account = getSessionAttr("userInfo");
		//定义用户名过滤规则
		String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";  
		String name = getPara("name");
		//对userName进行特殊字符过滤
		Pattern p = Pattern.compile(regEx);     
        Matcher m = p.matcher(name);   
        name = m.replaceAll("").trim();  
		
		Integer gender = getParaToInt("gender");
		Integer education_level = getParaToInt("education_level");
		Integer occupation = getParaToInt("occupation");
		
		account.set("userName", name).set("gender", gender).set("educationId", education_level).set("jobId", occupation).update();
		
		redirect("/settings");
		
		
	}
	
	
	public void edit() {
		render("/settings/personal_info/update_personal.html");
	}
}
