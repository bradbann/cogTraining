package com.lumosity.home;

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
import com.lumosity.model.PlanGame;
import com.lumosity.model.TrainPlan;
import com.lumosity.question.QuestionInterceptor;
import com.lumosity.utils.CreateKit;
import com.lumosity.utils.DateTimeKit;

/**
 * 主页界面控制器
 * 1.游戏推荐算法
 * 2.训练日历
 * 3.历史LPI
 * @author Scott
 *
 */
@Before({QuestionInterceptor.class,FitTestInterceptor.class})
public class HomeController extends Controller{
	/**
	 * 游戏推荐首先从数据库取出当天的训练计划，根据trainpalnid取出计划游戏表，传递到视图层
	 */
	public void index() {
		
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		
		List<PlanGame> planGames = new ArrayList<>();//游戏计划
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());
		
		if (trainPlan != null) {
			/*已经创建训练计划*/
			Integer id = trainPlan.getTrainPlanId();
			planGames = PlanGame.dao.findByTrainPlanId(id);
			
			if (planGames.size() <= 0) {
				/*该训练计划中没有游戏计划*/
				CreateKit.createPlanGame(planGames, id);//创建游戏计划
			} 
			setAttr("trainPlan", trainPlan);
		} else {
			//没有计划,则创建计划
			TrainPlan plan = new TrainPlan();
			plan.set("userId", userId)
				.set("playTimes", 0)
				.set("trainPlanDate", DateTimeKit.getDate())
				.set("week", DateTimeKit.getDateWeekNum())
				.save();
			//训练计划创建完成，则创建游戏计划
			planGames = new ArrayList<>();
			CreateKit.createPlanGame(planGames, plan.getTrainPlanId());
			setAttr("trainPlan", plan);
		}
		
		/**日历显示*/
		java.util.Date today = DateTimeKit.getDate();
		List<String> fourWeeks = DateTimeKit.getFourWeeks();//4周时间的集合
		List<Integer> dateStats = new ArrayList<>();//4周内日期的状态值集合
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
				} else {
					//该日没有记录，状态为2
					dateStats.add(2);
				} 
			}
		}
		int totalPlayDay = 0;
		for (Integer stat : dateStats) {
			if (stat == 1) {
				totalPlayDay += 1;
			}
		}
		setAttr("dateStats", dateStats);
		setAttr("weeks", fourWeeks);
		setAttr("totalPlayDay", totalPlayDay);
		
		/**柱状图显示*/
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
		
		render("/home.html");
	}
	
	
}
