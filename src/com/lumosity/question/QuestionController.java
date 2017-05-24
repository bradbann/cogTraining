package com.lumosity.question;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.QuestionStatus;

public class QuestionController extends Controller {

	public void index(){
		render("/question/question.html");
	}
	
	/**
	 * 表单的保存
	 */
	public void save(){
		
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		account.set("gender", getParaToInt("gender")).set("educationId", getParaToInt("education_level"))
				.set("jobId", getParaToInt("occupation")).update();
		
		QuestionStatus status = QuestionStatus.dao.findById(userId);
		if (status != null) {
			status.set("question1Status", 1).update();
		} else {
			new QuestionStatus().set("userId", userId).set("question1Status", 1).save();
		}
		
		redirect("/question/secondQuestion");
	}
	
	public void secondQuestion() {
		render("/question/questionnaire.html");
	}
	
	public void secondSave(){
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		
		QuestionStatus status = QuestionStatus.dao.findById(userId);
		if (status != null) {
			status.set("question2Status", 1).update();
		} else {
			new QuestionStatus().set("userId", userId).set("question2Status", 1).save();
		}
		redirect("/fitTest");
	}
}
