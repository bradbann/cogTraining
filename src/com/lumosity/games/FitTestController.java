package com.lumosity.games;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.GameHistory;
import com.lumosity.model.Game;
import com.lumosity.model.TestPlan;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.LPIKit;

/**
 * 适应性训练:前三个类型各选出一个游戏出来
 * @author Scott
 *
 */
public class FitTestController extends Controller {
	
	
	public void index() {
		
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		
		List<TestPlan> testPlans = TestPlan.dao.findByUser(userId);
		
		if (testPlans.size() <= 0 ) {
			//用户没有适应性训练记录，则创建适应性训练计划(固定为三种类型游戏)
			new TestPlan().set("userId", userId).set("gameId", 14).set("sequence", 1).set("isPlay", 0).save();//speed_match
			new TestPlan().set("userId", userId).set("gameId", 2).set("sequence", 2).set("isPlay", 0).save();//lost_in_migration
			new TestPlan().set("userId", userId).set("gameId", 7).set("sequence", 3).set("isPlay", 0).save();//memory_matrix
		}
		//取出用户剩余的适应性训练计划（可能为新创建也可能为后续取出）
		testPlans = TestPlan.dao.remaining(userId);
		
		if (testPlans.size() > 0) {
			List<Game> games = new ArrayList<>();//适应性训练游戏集合
			for (TestPlan plan : testPlans) {
				games.add(Game.dao.findById(plan.getGameId()));
			}
			int size = 12/(testPlans.size());
			setAttr("games", games);
			setAttr("gameNumber", size);
			setAttr("sequence", testPlans.get(0).getSequence());
		} else {
			setAttr("msg", "complete");
			setAttr("sequence", 0);
		}
		render("/fit/fitTest.html");
	}
	
	
	public void play() {
		
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		
		Integer sequence = getParaToInt(0);
		TestPlan testPlan = TestPlan.dao.findByUserAndSquence(userId, sequence);
		Game testGame = Game.dao.findById(testPlan.getGameId());
		
		String jsonStr = "{\"fit_test\":\"1\",\"game_id\":\""+testPlan.getGameId()+"\",\"game_param\":\""+testGame.getGameName()+"\",\"username\":\""+account.getUserName()+"\",\"token\":\""
				+ "uqiwejhbaskjdbasdioqw\",\"game_user_setting\":{\"level\":\"1\",\"time\":\"5\",\"score\":\"0\"},\"updated_at\":\""
				+DateTimeKit.getPlayDate()+"\"}";
		
		setAttr("jsonStr", jsonStr);
		setAttr("game", testGame);
		setAttr("sequence", sequence);
		
		render("/fit/fitPlay.html");
	}
	
	/**
	 * 适应性训练这保存到特定数据库
	 */
	public void save(){
		
		Account account = getSessionAttr("userInfo");
		Long userId = account.getUserId();
		
		Integer gameId = getParaToInt("gameId");
		Integer sequence = getParaToInt("sequence");
		String gameData = getPara("result");//获取游戏传入数据
		
		TestPlan testPlan = TestPlan.dao.findByUserAndSquence(userId, sequence);
		
		if (gameData.trim() != null && gameData.length() != 0) {
			//游戏数据为空，代表的是用户主动结束游戏
			JSONObject jsonObject = new JSONObject(gameData);//转为java json对象
			
			int score = jsonObject.getInt("score");
			
			int LPI = LPIKit.getLPI(score);
			/*保存本次游戏数据*/
			new GameHistory().set("gameId", gameId)
							.set("userId", userId)
							.set("score", score)
							.set("playDate", DateTimeKit.getDate())
							.set("gameLPI", LPI)
							.set("gameData", jsonObject.getString("trial_data"))
							.save();
			//更新testPlan状态
			testPlan.set("isPlay", 1).update();
			setAttr("sequence", sequence+1);
		}
		List<TestPlan> testPlans = TestPlan.dao.remaining(userId);
		if (testPlans.size() > 0) {
			List<Game> games = new ArrayList<>();//适应性训练游戏集合
			for (TestPlan plan : testPlans) {
				games.add(Game.dao.findById(plan.getGameId()));
			}
			int size = 12/(testPlans.size());
			setAttr("gameNumber", size);
			setAttr("games", games);
			setAttr("sequence", testPlans.get(0).getSequence());
		}
		
		render("/fit/fitComplete.html");
//		redirect("/fitTest");
		
	}

}
