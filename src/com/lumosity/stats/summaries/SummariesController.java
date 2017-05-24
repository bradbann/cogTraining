package com.lumosity.stats.summaries;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.BrainProfile;
import com.lumosity.model.DayClassRecord;
import com.lumosity.model.DayRecord;
import com.lumosity.model.GameClass;
import com.lumosity.model.WeekClassRecord;
import com.lumosity.model.WeekRecord;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.ListKit;

/**
 * 总览页面的显示
 * V1
 * V2 2017-02-23
 * @author Scott
 *
 */
public class SummariesController extends Controller{

	public void index() {
		Account account = getSessionAttr("userInfo");
//		Long userId = account.getUserId();
		Long userId = (long) 0;
		if (account != null)
			userId = account.getUserId();
		else
		    userId = getParaToLong("userId");
		
		List<String> fourWeeks = DateTimeKit.getFourWeeks();//4周时间的集合
		java.util.Date today = DateTimeKit.getDate();
		java.util.Date startDay = Date.valueOf(fourWeeks.get(0));//4周的起始日期
		BrainProfile userProdile = BrainProfile.dao.findById(userId);
		
		java.util.Date endDate = DateTimeKit.getDate();
		if (userProdile != null) {
			endDate = userProdile.getLastDate();
		}
		
		List<Integer> dateStats = new ArrayList<>();//4周内日期的状态值集合
		List<Integer> weekGameNum = new ArrayList<>();//每一周的游戏数
		List<Integer> fourWeeksLPI = new ArrayList<>();//4周时间内每天的总LPI的集合
		
		int gameNum = 0;
		for (int i = 0; i < fourWeeks.size(); i++) {
			
			if (today.before(DateTimeKit.parse(fourWeeks.get(i)))) {
				//在今日之后的时间里，一定没有dayRecord的记录，只需进行状态值的设定
				dateStats.add(0);
			} else {
				//对在今日和今日之前的dayRecord进行遍历
				DayRecord record = DayRecord.dao.findByIdAndDate(userId, Date.valueOf(fourWeeks.get(i)));
				if (record != null) {
					//该日有记录，状态为1
					dateStats.add(1);
					fourWeeksLPI.add(record.getLPI());
					gameNum += record.getGameTimes();
				} else {
					//该日没有记录，状态为2
					dateStats.add(2);
					fourWeeksLPI.add(0);
				} 
			}
	
			if ((i+1)%7==0) {
				weekGameNum.add(gameNum);
				gameNum = 0;
			}
		}
		//对四周内的LPI进行补全
		ListKit.fillList(fourWeeksLPI);
		int totalGameNum = 0;
		for (Integer num : weekGameNum) {
			totalGameNum += num;
		}
		//计算状态值为1的天数（四周内玩过游戏的天数）
		int totalPlayDay = 0;
		for (Integer stat : dateStats) {
			if (stat == 1) {
				totalPlayDay += 1;
			}
		}
		setAttr("startDay", startDay);
		setAttr("today", today);
		setAttr("dateStats", dateStats);
		setAttr("totalPlayDay", totalPlayDay);
		setAttr("weeks", fourWeeks);
		setAttr("weekGameNum", weekGameNum);
		setAttr("totalGameNum", totalGameNum);
		setAttr("fourWeeksLPI", fourWeeksLPI);
		
		
		List<GameClass> gameClasses = GameClass.dao.findAll();
		Map<String, Integer> pie = new HashMap<>();
		List<Integer> classPie = new ArrayList<>();
		
		for (GameClass gameClass : gameClasses) {
			int num = 0;
			for (String day : fourWeeks) {
				DayClassRecord record = DayClassRecord.dao.findByIdAndClassAndDate(
											userId, gameClass.getGameClassId(), Date.valueOf(day));
				if (record !=null) {
					//有该日该类型的记录
					num += record.getGameTimes();
				}
			}
			pie.put(gameClass.getGameClassName(), num);
			classPie.add(num);
		}
		setAttr("pie", pie);
		setAttr("classPie", classPie);
		
		int startLPI = 0;;
		DayRecord startRecord = DayRecord.dao.findByIdAndDate(userId, startDay);
		if (startRecord != null) {
			startLPI = startRecord.getLPI();
		}
		int endLPI = 0;
		DayRecord endRecord = DayRecord.dao.findByIdAndDate(userId, endDate);
		if (endRecord != null) {
			endLPI = endRecord.getLPI();
		}
		setAttr("startLPI", startLPI);
		setAttr("endLPI", endLPI);
		setAttr("change", endLPI-startLPI);
		if (startLPI == 0) {
			setAttr("changePercent", 100);

		} else {
			setAttr("changePercent", ((endLPI-startLPI)/startLPI)*100);

		}
		
		Map<String, Integer> changePie = new HashMap<>();
		for (GameClass gameClass : gameClasses) {
			int classId = gameClass.getGameClassId();
			int classStartLPI = 0;
			DayClassRecord classStartRecord = DayClassRecord.dao.findByIdAndClassAndDate(userId, classId, startDay);
			if (classStartRecord != null) {
				classStartLPI = classStartRecord.getLPI();
			}
			int classEndLPI = 0;
			DayClassRecord classEndRecord = DayClassRecord.dao.findByIdAndClassAndDate(userId, classId, endDate);
			if (classEndRecord != null) {
				classEndLPI = classEndRecord.getLPI();
			}
			changePie.put(gameClass.getGameClassName(), classEndLPI-classStartLPI);
		}
		setAttr("changePie", changePie);
		
		render("/stats/training_history/summaries/history_summary.html");
	}
	
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
		java.util.Date createDate = account.getCreateDate();//用户创建日期
		java.util.Date today = DateTimeKit.getDate();
		List<String> days = DateTimeKit.getDaysBetweenTwoDates(createDate, today);//从用户创建到现在的天数
		List<Integer> weekLPI = new ArrayList<>();//每周LPI的分数集合
		int trainDays = 0;//所有时间内的有玩游戏的日期
		int totalPlayTimes = 0;//所有时间内一共玩的游戏数
		
		/*List<WeekRecord> records = WeekRecord.dao.findByUser(userId);
		if (records.size() > 0) {
				for (WeekRecord record : records) {
					trainDays += record.getTrainDays();
					totalPlayTimes += record.getGameTimes();
				}
		} */
		/**WeekRecord主要设计折线图和天数的显示（持续训练3天的还未实现）*/
		int maxTrainDay = 0;
		for (String day : days) {
			if (DateTimeKit.isWeekend(day)) {
				//该日是一周的最后一天
				WeekRecord weekRecord = WeekRecord.dao.findByIdAndDate(userId, Date.valueOf(day));
				if (weekRecord != null) {
					trainDays += weekRecord.getTrainDays();
					weekLPI.add(weekRecord.getLPI());
					if (weekRecord.getTrainDays() > maxTrainDay) {
						maxTrainDay = weekRecord.getTrainDays();
					}
				} else {
					weekLPI.add(0);
				}
			}
			//如果不是最后一天不需作任何处理
		}
		String weekend = DateTimeKit.getWeekEnd();
		//集合最后一天不是本周最后一天，trainDays、totalPlayTimes、weekLPI都少算一次
		if (!(days.get(days.size()-1).equals(weekend))) {
			WeekRecord weekRecord = WeekRecord.dao.findByIdAndDate(userId, Date.valueOf(weekend));
			if (weekRecord != null) {
				trainDays += weekRecord.getTrainDays();
				weekLPI.add(weekRecord.getLPI());
				if (weekRecord.getTrainDays() > maxTrainDay) {
					maxTrainDay = weekRecord.getTrainDays();
				}
			} else {
				weekLPI.add(0);
			}
		} 
	
		setAttr("daysPerWeek", (trainDays/days.size())*10);
		setAttr("createDate", createDate);
		setAttr("trainDays", trainDays);
		setAttr("weekLPI", weekLPI);
		setAttr("maxTrainDay", maxTrainDay);
		
		/**WeekClassRecord主要涉及饼状图的显示*/
		Map<String, Integer> pie = new HashMap<>();
		List<Integer> classPie = new ArrayList<>();
		
		List<GameClass> gameClasses = GameClass.dao.findAll();
		for (GameClass gameClass : gameClasses) {
			int classPlayTimes = 0;
			List<WeekClassRecord> records2 = WeekClassRecord.dao.findByUserAndClass(userId, gameClass.getGameClassId());
			if (records2.size() > 0) {
				for (WeekClassRecord record : records2) {
					classPlayTimes += record.getGameTimes();
				}
			}
			totalPlayTimes += classPlayTimes;
			pie.put(gameClass.getGameClassName(), classPlayTimes);
			classPie.add(classPlayTimes);
		}
		setAttr("pie", pie);
		setAttr("totalPlayTimes", totalPlayTimes);
		setAttr("classPie", classPie);
		
		
		int startLPI = 0;
		int lastLPI = 0;
		BrainProfile brainProfile = BrainProfile.dao.findById(userId);
		if (brainProfile != null) {
			//个人档案一旦创建就说明至少玩过一次游戏，即开始时间和最后一次的时间必定不为空
			java.util.Date startDate = brainProfile.getStartDate();
			java.util.Date lastDate = brainProfile.getLastDate();
			startLPI = DayRecord.dao.findByIdAndDate(userId, startDate).getLPI();
			lastLPI = DayRecord.dao.findByIdAndDate(userId, lastDate).getLPI();
			setAttr("startLPI", startLPI);
			setAttr("lastLPI", lastLPI);
			setAttr("change", lastLPI-startLPI);
		} else {
			setAttr("startLPI", 0);
			setAttr("lastLPI", 0);
			setAttr("change", 0);
		}
		
		/**bestLPI是从日记录中取出*/
		int bestLPI = 0;
		List<DayRecord> records3 = DayRecord.dao.findByUserSortByLPI(userId);
		if (records3.size() > 0) {
			bestLPI = records3.get(0).getLPI();
			setAttr("bestDate", records3.get(0).getRecordDate());
		}
		setAttr("bestLPI", bestLPI);
		
		
		render("/stats/training_history/summaries/history_summary_all.html");
	}
	
}
