package com.lumosity.games;

import java.util.Date;

import org.json.JSONObject;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.BrainClassProfile;
import com.lumosity.model.BrainProfile;
import com.lumosity.model.DayClassRecord;
import com.lumosity.model.DayGameRecord;
import com.lumosity.model.DayRecord;
import com.lumosity.model.UserGame;
import com.lumosity.model.WeekClassRecord;
import com.lumosity.model.WeekRecord;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.LPIKit;
/**
 * 
 * @author Scott
 *
 */
public class GameDataInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {

		Controller c = inv.getController();
		Account account = c.getSessionAttr("userInfo");
		Long userId = (long) 0;
		
		if (account != null)
			userId = account.getUserId();
		else
		    userId = c.getParaToLong("userId");
		
		Date today = DateTimeKit.getDate();
		
		int gameClassId = c.getParaToInt("gameClassId");
		int gameId = c.getParaToInt("gameId");
		String gameData = c.getPara("result");//获取异步提交的json字符串
		
//		GameClass gameClass = GameClass.dao.findById(gameClassId);
		JSONObject jsonObject = new JSONObject(gameData);
		int score = jsonObject.getInt("score");
		
		/**游戏LPI的计算*/
		int LPI = LPIKit.getLPI(score);//改日的具体某一个游戏的LPI
		
		/**个人日游戏记录表更新*/
		DayGameRecord dayGameRecord = DayGameRecord.dao.findByIdAndGameAndDate(userId, gameId, today);
		if (dayGameRecord != null) {
			//有该日该游戏的记录，则用本次的数据更新覆盖上次的数据
			dayGameRecord.set("score", score).set("LPI", LPI).set("gameTimes", dayGameRecord.getGameTimes()+1).update();
		} else {
			new DayGameRecord().set("userId", userId).set("gameId", gameId).set("gameClassId", gameClassId)
							.set("score", score).set("LPI", LPI).set("gameTimes", 1).set("recordDate", today).save();
		}
		
		/**类型LPI的计算*/
		int classLPI = LPIKit.getClassLPI(userId, gameClassId, today);//该日的某一游戏类型的LPI,必须在个人日记录表的更新之后才能取出
		
		/**个人日类型记录表更新*/
		DayClassRecord classRecord = DayClassRecord.dao.findByIdAndClassAndDate(userId, gameClassId, today);
		if (classRecord != null) {
			//有类型的记录，则覆盖上次数据并记录变化
			classRecord.set("LPI", classLPI).set("change", (classLPI-classRecord.getLPI()))
						.set("gameTimes", classRecord.getGameTimes()+1).update();
		} else {
			new DayClassRecord().set("userId", userId).set("gameClassId", gameClassId).set("LPI", classLPI)
								.set("change", 0).set("recordDate", today).set("gameTimes", 1).save();
		}
		
		/**总LPI(currentLPI)的计算*/
		int currentLPI = LPIKit.getCurrentLPI(userId, today);//该日的总LPI
		
		/**个人日总表*/
		DayRecord dayRecord = DayRecord.dao.findByIdAndDate(userId, today);
		if (dayRecord != null) {
			//有该日的总表记录，则更新覆盖
			dayRecord.set("LPI", currentLPI).set("change", (currentLPI-dayRecord.getLPI()))
					.set("gameTimes", dayRecord.getGameTimes()+1).update();
		} else {
			new DayRecord().set("userId", userId).set("LPI", currentLPI).set("change", 0)
							.set("recordDate", today).set("gameTimes", 1).save();
		}
		
		
		
		/**个人类型档案表的更新,本次的数据覆盖上次的数据*/
		BrainClassProfile classProfile = BrainClassProfile.dao.findByUserAndClass(userId, gameClassId);
		if (classProfile !=null) {
			//更新对应的LPI
			classProfile.set("LPI", classLPI).update();
		} else {
			new BrainClassProfile().set("userId", userId).set("gameClassId", gameClassId).set("LPI", classLPI).save();
		}
		
		/**个人档案表的更新,本次的数据覆盖上次的数据*/
		BrainProfile brainProfile = BrainProfile.dao.findById(userId);
//		List<BrainClassProfile> classProfiles = BrainClassProfile.dao.findByUser(userId);
		if (brainProfile !=null) {
			//更新对应的LPI
			brainProfile.set("lastDate", today).set("overall", currentLPI).update();
			if (currentLPI > brainProfile.getBestLPI()) {
				//玩一次就更新currentLPI，从而更新bestLPI，bestDate可以不设置。
				brainProfile.set("bestLPI", currentLPI).set("bestDate", today).update();
			}
		} else {
			//创建个人总档案表时，用户玩的游戏类型有且只有一个，故不需更新总LPI和最好LPI
			new BrainProfile().set("userId", userId).set("bestDate", today).set("overall", 0).set("bestLPI", 0)
							.set("lastDate", today).set("startDate", today).save();
		}
		
		/**周类型记录表的更新(取每周结束前最后一次的数据)*/
		Date weekEnd = java.sql.Date.valueOf(DateTimeKit.getWeekEnd());//获取本周最后一天日期
		
		WeekClassRecord weekClassRecord = WeekClassRecord.dao.findByIdAndDateAndClass(userId, weekEnd, gameClassId);
		if (weekClassRecord != null) {		
			weekClassRecord.set("LPI", classLPI).set("gameTimes", weekClassRecord.getGameTimes()+1).update();
		} else {
			new WeekClassRecord().set("userId", userId).set("LPI", classLPI).set("gameClassId", gameClassId)
							.set("gameTimes", 1).set("weekEndDate", weekEnd).save();
		}
		
		/**周总表的更新*/
		WeekRecord weekRecord = WeekRecord.dao.findByIdAndDate(userId, weekEnd);
		if (weekRecord != null) {
			if (DateTimeKit.format(today).equals(DateTimeKit.format(brainProfile.getLastDate()))) {
				//日期相同，traindays不增加		
			} else {
				weekRecord.set("trainDays", weekRecord.getTrainDays()+1).update();
			}
			weekRecord.set("LPI", currentLPI).set("gameTimes", weekRecord.getGameTimes()+1).update();
		} else {
			new WeekRecord().set("userId", userId).set("LPI", currentLPI).set("gameTimes", 1)
							.set("weekEndDate", weekEnd).set("trainDays", 1).save();
		}
		
		/**设置用户游戏关系表**/
		UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
		if (userGame != null) {
			userGame.set("lastPlayDate", DateTimeKit.getPlayDate()).set("lastPlayScore", score).update();
		} else {
			new UserGame().set("userId", userId).set("gameId", gameId).set("isLocked", 0).set("isLike", 0)
				.set("lastPlayDate", DateTimeKit.getPlayDate()).set("lastPlayScore", score).save();
		}
		
		inv.invoke();
		
	}

}
