package com.lumosity.stats.detail;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.DayClassRecord;
import com.lumosity.model.DayRecord;
import com.lumosity.model.GameClass;
import com.lumosity.model.WeekClassRecord;
import com.lumosity.model.WeekRecord;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.ListKit;
/**
 * 趋势页面的显示
 * V1
 * V2
 * @author Scott
 *
 */
public class DetailController extends Controller {

	/**
	 * 过去4周的趋势显示，主要遍历dayRecord、dayClassRecord
	 */
	public void index() {
		
		Account account = getSessionAttr("userInfo");
//		Long userId = account.getUserId();
		Long userId = (long) 0;
		if (account != null)
			userId = account.getUserId();
		else
		    userId = getParaToLong("userId");
		
		List<String> fourWeeks = DateTimeKit.getFourWeeksTillToday();
		List<GameClass> gameClasses = GameClass.dao.findAll();
		
		List<Integer> LPIs = new ArrayList<>();//总LPI数值的集合
		Map<String, List<Integer>> classLPIs = new HashMap<>();//各类型的LPI数值的集合
		boolean flag = true;
		for (GameClass gameClass : gameClasses) {
			List<Integer> data = new ArrayList<>();
			for (String day : fourWeeks) {
				if (flag) {
					DayRecord dayRecord = DayRecord.dao.findByIdAndDate(userId, Date.valueOf(day));
					if (dayRecord != null) {
						LPIs.add(dayRecord.getLPI());
					} else {
						LPIs.add(0);
					}
				}
				
				DayClassRecord classRecord = DayClassRecord.dao.findByIdAndClassAndDate(
						userId, gameClass.getGameClassId(), Date.valueOf(day));
				if (classRecord != null) {
					data.add(classRecord.getLPI());
				} else {
					data.add(0);
				}
			}
			flag = false;
			ListKit.fillList(data);
			classLPIs.put(gameClass.getGameClassName(), data);
			
		}
		ListKit.fillList(LPIs);
		setAttr("weeks", fourWeeks);
		setAttr("LPIs", LPIs);
		setAttr("classLPIs", classLPIs);
		
		render("/stats/training_history/detail/history_trends.html");
	}
	
	/**
	 * 所有时间的趋势显示，主要遍历weekRecord、weekClassRecord
	 */
	public void all() {
		
		Account account = getSessionAttr("userInfo");
//		Long userId = account.getUserId();
		Long userId = (long) 0;
		if (account != null) {
			userId = account.getUserId();
		} else {
		    userId = getParaToLong("userId");
			account = Account.dao.findById(userId);
		}
		
		java.util.Date createDate = account.getCreateDate();
		java.util.Date today = DateTimeKit.getDate();
		
		List<GameClass> gameClasses = GameClass.dao.findAll();
		List<String> days = DateTimeKit.getDaysBetweenTwoDates(createDate, today);
		List<String> weekends = new ArrayList<>();
		List<String> showWeekends = new ArrayList<>();
		
		for (String day : days) {
			if (DateTimeKit.isWeekend(day)) {
				weekends.add(day);
				showWeekends.add(day);
			}
		}
		if (!DateTimeKit.isWeekend(DateTimeKit.getStringDate())) {
			weekends.add(DateTimeKit.getWeekEnd());
			showWeekends.add(DateTimeKit.format(today));
		}
		List<Integer> LPIs = new ArrayList<>();
		Map<String, List<Integer>> classLPIs = new HashMap<>();
		
		boolean flag = true;
		for (GameClass gameClass : gameClasses) {
			List<Integer> data = new ArrayList<>();
			for (String weekend : weekends) {
				if (flag) {
					WeekRecord weekRecord = WeekRecord.dao.findByIdAndDate(userId, Date.valueOf(weekend));
					if (weekRecord != null) {
						LPIs.add(weekRecord.getLPI());
					} else {
						LPIs.add(0);
					}
				}
				
				WeekClassRecord classRecord = WeekClassRecord.dao.findByIdAndDateAndClass(
												userId, Date.valueOf(weekend), gameClass.getGameClassId());
				if (classRecord != null) {
					data.add(classRecord.getLPI());
				} else {
					data.add(0);
				}
			}
			
			flag = false;
			ListKit.fillList(data);
			classLPIs.put(gameClass.getGameClassName(), data);
		}
		ListKit.fillList(LPIs);
		setAttr("weeks", showWeekends);
		setAttr("LPIs", LPIs);
		setAttr("classLPIs", classLPIs);
		
		
		render("/stats/training_history/detail/history_trends_all.html");
	}
}
