package com.lumosity.settings;

import com.jfinal.core.Controller;

public class SettingsController extends Controller{

	public void index() {
		render("/settings/account_settings.html");
	}

}
