package com.lumosity.settings.preferences;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.TrainPrefer;

public class PreferencesController extends Controller{

	public void index() {
		
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		
		List<Integer> classPref = new ArrayList<>();
		int rank_0 = getParaToInt("rank-0");
		int rank_1 = getParaToInt("rank-1");
		int rank_2 = getParaToInt("rank-2");
		int rank_3 = getParaToInt("rank-3");
		int rank_4 = getParaToInt("rank-4");
		int level = getParaToInt("optionsRadios");
		int english = 0;
		int color = 0;
		if (getParaToInt("english") != null) {
			english = getParaToInt("english");
		}
		 
		if (getParaToInt("colorBlind") != null) {
			color = getParaToInt("colorBlind");
		}
		classPref.add(rank_0);
		classPref.add(rank_1);
		classPref.add(rank_2);
		classPref.add(rank_3);
		classPref.add(rank_4);

		TrainPrefer prefer = TrainPrefer.dao.findById(userId);
		if (prefer != null) {
			prefer.set("level", level).set("isEnglish", english).set("isColorBlind", color)
					.set("classPref", classPref.toString()).update();
		} else {
			new TrainPrefer().set("userId", userId).set("level", level)
				.set("isEnglish", english).set("isColorBlind", color)
				.set("classPref", classPref.toString()).save();
		}
		
		
		redirect("/settings");
	}
	
	public void edit() {
		
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		
		TrainPrefer prefer = TrainPrefer.dao.findById(userId);
		if (prefer != null) {
			setAttr("classPref", prefer.getClassPref());
			setAttr("level", prefer.getLevel());
			setAttr("english", prefer.getIsEnglish());
			setAttr("color", prefer.getIsColorBlind());
		} 
		
		render("/settings/training_preferences/update_preferences.html");
	}
}
