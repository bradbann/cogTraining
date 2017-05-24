package com.lumosity.api;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.lumosity.model.BrainClassProfile;
import com.lumosity.model.BrainProfile;
import com.lumosity.model.DayClassRecord;
import com.lumosity.model.DayRecord;
import com.lumosity.model.GameClass;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.ListKit;

/**
 * 移动端WebView H5
 * @author Scott
 *
 */
@Clear
public class StatsController extends Controller{

	@ActionKey("/api/page/overview")
	public void brainProfile() {

		Long userId = getParaToLong("userId");
	
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
		
		render("/apiPages/overview.html");
	}
	
	@ActionKey("/api/page/compare")
	public void compare() {
		
	    Long userId = getParaToLong("userId");
		
		List<GameClass> gameClasses = GameClass.dao.findAll();
		for (GameClass gameClass : gameClasses) {
			int classId = gameClass.getGameClassId();
			BrainClassProfile accountClassProfile = BrainClassProfile.dao.findByUserAndClass(userId, classId);
			int accountLPI = 0;
			if (accountClassProfile != null) {
				accountLPI = accountClassProfile.getLPI();
			}	
			//该类型游戏的所有用户记录
			List<BrainClassProfile> classProfiles = BrainClassProfile.dao.findByClass(classId);
			//查找该类型小于用户LPI的分的记录
			List<BrainClassProfile> lessProfiles = BrainClassProfile.dao.findByClassSort(classId, accountLPI);
			if (classProfiles.size() > 0 ) {
				setAttr(gameClass.getDis(), lessProfiles.size()/classProfiles.size());	
			} else {
				setAttr(gameClass.getDis(), 0);
			}
		}
		int overall = 0;
		BrainProfile brainProfile = BrainProfile.dao.findById(userId);
		if (brainProfile != null) {
			overall = brainProfile.getOverall();
			//查找所有的用户总记录
			List<BrainProfile> profiles = BrainProfile.dao.findAll();
			//查询总LPI分数小于用户的记录
			List<BrainProfile> lessProfiles = BrainProfile.dao.findBySort(overall);
			if (profiles.size() > 0) {
				setAttr("overall", lessProfiles.size()/profiles.size());
			} else {
				setAttr("overall", 0);
			}
		} else {
			setAttr("overall", 0);
		}
		
		render("/apiPages/compare.html");
	}
	
	@ActionKey("/api/page/history")
	public void history() {
		Long userId = getParaToLong("userId");
		
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
		
		render("/apiPages/history_summary.html");
	}
}
