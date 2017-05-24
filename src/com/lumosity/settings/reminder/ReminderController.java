package com.lumosity.settings.reminder;

import com.jfinal.core.Controller;

public class ReminderController extends Controller{

	public void index() {
		
	}
	
	public void edit(){
		
		render("/settings/reminders/reminders.html");
	}
}
