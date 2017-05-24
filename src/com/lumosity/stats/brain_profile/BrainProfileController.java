package com.lumosity.stats.brain_profile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.lumosity.games.FitTestInterceptor;
import com.lumosity.model.Account;
import com.lumosity.model.BrainClassProfile;
import com.lumosity.model.BrainProfile;
import com.lumosity.model.DayRecord;
import com.lumosity.model.GameClass;
import com.lumosity.question.QuestionInterceptor;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.ListKit;

/**
 * 个人数据中心总览页面的显示控制
 * V1
 * V2
 * @author Scott
 *
 */
@Before({QuestionInterceptor.class,FitTestInterceptor.class})
public class BrainProfileController extends Controller{
	
	public void index(){
		
		Account account = getSessionAttr("userInfo");
		Long userId = (long) 0;
		
		if (account != null)
			userId = account.getUserId();
		else
		    userId = getParaToLong("userId");
	
		List<GameClass> gameClasses = GameClass.dao.findAll();
		
		for (GameClass gameClass : gameClasses) {
			BrainClassProfile classProfile = BrainClassProfile.dao.findByUserAndClass(userId, gameClass.getGameClassId());
			if (classProfile != null) {
				setAttr(gameClass.getDis(), classProfile.getLPI());
			} else {
				setAttr(gameClass.getDis(), 0);
			}
		}
		
		BrainProfile profile = BrainProfile.dao.findById(userId);
		if (profile != null) {
			setAttr("overall", profile.getOverall());
			setAttr("bestLPI", profile.getBestLPI());
		} else {
			setAttr("overall", 0);
			setAttr("bestLPI", 0);
		}
		
		/*根据LPI的倒序查找前2个*/
		List<BrainClassProfile> classProfiles = BrainClassProfile.dao.findByLPI(userId);
		if (classProfiles.size() > 1) {
			GameClass class1 = GameClass.dao.findById(classProfiles.get(0).getGameClassId());
			setAttr("first", class1.getGameClassName());
			GameClass class2 = GameClass.dao.findById(classProfiles.get(1).getGameClassId());
			setAttr("second", class2.getGameClassName());
		} else if (classProfiles.size() == 1) {
			GameClass class1 = GameClass.dao.findById(classProfiles.get(0).getGameClassId());
			setAttr("first", class1.getGameClassName());
		} 
		
		
		/**折线图的显示 dayRecord表*/
		List<String> fourWeeks = DateTimeKit.getFourWeeks();
		List<Integer> fourWeeksLPI = new ArrayList<>();//四周时间内总LPI的集合
		
		for (String day : fourWeeks) {
			DayRecord record = DayRecord.dao.findByIdAndDate(userId, Date.valueOf(day));
			if (record != null) {
				fourWeeksLPI.add(record.getLPI());
			} else {
				fourWeeksLPI.add(0);
			}
		}
		//进行补全
		ListKit.fillList(fourWeeksLPI);
		
		setAttr("fourWeeksLPI", fourWeeksLPI);
		
		render("/stats/brain_profile/overview.html");
	}
}
