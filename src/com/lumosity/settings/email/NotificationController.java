package com.lumosity.settings.email;

import com.jfinal.core.Controller;

public class NotificationController extends Controller{

	public void index() {
		
	}
	
	public void edit() {
		
		render("/settings/email_notifications/email_notifications.html");
	}
}
