package com.lumosity.train;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.lumosity.games.GameDataInterceptor;
import com.lumosity.model.Account;
import com.lumosity.model.GameClass;
import com.lumosity.model.GameHistory;
import com.lumosity.model.Game;
import com.lumosity.model.PlanGame;
import com.lumosity.model.TrainPlan;
import com.lumosity.model.UserGame;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.LPIKit;
/**
 * 训练计划游戏控制
 * @author Scott
 *
 */

public class TurboController extends Controller{
	
	/**从plangame取出剩余游戏的序列号，并将其中最小的发送给start()方法**/
	public void index() {
		Account account = getSessionAttr("userInfo");//获取用户
		Long userId = account.getUserId();//获取用户id
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());//获取用户今日训练计划
		Integer trainPlanId = trainPlan.getTrainPlanId();//获取用户今日训练计划id
		List<PlanGame> planGames = PlanGame.dao.findRemaining(trainPlanId);//获取用户今日剩余游戏计划(以sequence升序排列)
		
		/*对剩余游戏进行判断（可以不要判断，当剩余游戏为0时就不会出现start training按钮了）*/
		if (planGames.size() > 0) {
			redirect("/train/turbo/start/" + planGames.get(0).getSequence());
		} else {
			redirect("/home");
		}
		
		
	}
	public void start() {
		Account account = getSessionAttr("userInfo");//获取用户
		Long userId = account.getUserId();//获取用户id
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());//获取用户今日训练计划
		Integer trainPlanId = trainPlan.getTrainPlanId();//获取用户今日训练计划id
		
		Integer sequence = getParaToInt(0);//从参数中取出序列号
		PlanGame planGame = PlanGame.dao.findByTrainPlanIdAndSequence(trainPlanId, sequence);
		Game game = Game.dao.findById(planGame.getGameId());
		GameClass gameClass = GameClass.dao.findById(game.getGameClassId());
		/*查找所有该游戏的记录*/
		List<GameHistory> histories = GameHistory.dao.findBestScore(userId, game.getGameId());
		if (histories.size() > 0) {
			setAttr("best_score", histories.get(0).getScore());
		} else {
			setAttr("bset_score", 0);
		}
		setAttr("sequence", sequence);
		setAttr("planGame", planGame);
		setAttr("game", game);
		setAttr("gameClass", gameClass);
		render("/train/turbo_start.html");
	}
	
	public void play() {
		Account account = getSessionAttr("userInfo");//获取用户
		Long userId = account.getUserId();//获取用户id
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());//获取用户今日训练计划
		Integer trainPlanId = trainPlan.getTrainPlanId();//获取用户今日训练计划id
		
		Integer sequence = getParaToInt(0);//从参数中取出序列号
		PlanGame planGame = PlanGame.dao.findByTrainPlanIdAndSequence(trainPlanId, sequence);
		Game game = Game.dao.findById(planGame.getGameId());
		GameClass gameClass = GameClass.dao.findById(game.getGameClassId());
		
		String userName = account.getUserName();
		int gameId = game.getGameId();
		String gameName = game.getGameName();
		//默认用户没有玩过游戏
		String jsonStr = "{\"fit_test\":\"1\",\"game_id\":\""+gameId+"\",\"game_param\":\""+gameName+"\",\"username\":\""+userName+"\",\"token\":\""
				+ "uqiwejhbaskjdbasdioqw\",\"game_user_setting\":{\"level\":\"1\",\"time\":\"5\",\"score\":\"0\"},\"updated_at\":\""
				+DateTimeKit.getPlayDate()+"\"}";
		//查找用户游戏关系表
		UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
		if (userGame != null) {
			if (userGame.getLastPlayDate() != null && userGame.getLastPlayScore() != null) {
				//用户玩过游戏,fit_test:0
				jsonStr = "{\"fit_test\":\"0\",\"game_id\":\""+gameId+"\",\"game_param\":\""+gameName+"\",\"username\":\""+userName+"\",\"token\":\""
						+ "uqiwejhbaskjdbasdioqw\",\"game_user_setting\":{\"level\":\"3\",\"time\":\"5\",\"score\":\""+userGame.getLastPlayScore()
						+"\"},\"updated_at\":\""+userGame.getLastPlayDate()+"\"}";	
			}	
		}
		
		setAttr("jsonStr", jsonStr);
		setAttr("gameClass", gameClass);
		setAttr("sequence", sequence);
		setAttr("planGame", planGame);
		setAttr("game", game);
		render("/train/turbo_play.html");
		
	}
	@Before(GameDataInterceptor.class)
	public void complete() {
		Account account = getSessionAttr("userInfo");//获取用户
		Long userId = account.getUserId();//获取用户id
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());//获取用户今日训练计划
		Integer trainPlanId = trainPlan.getTrainPlanId();//获取用户今日训练计划id
		List<PlanGame> planGames = PlanGame.dao.findRemaining(trainPlanId);//获取用户今日剩余游戏计划(以sequence升序排列)
		
		/*获取本次游戏的相关数据*/
		int sequence = planGames.get(0).getSequence();//本次游戏所在训练计划中的序列号
		int gameId = planGames.get(0).getGameId();
		
		String gameData = getPara("result");//获取游戏传入数据
	
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
		
		planGames.get(0).set("isPlay", 1).update();//设置该游戏状态为已完成
		trainPlan.set("playTimes", trainPlan.getPlayTimes()+1).update();//设置今日计划的完成状态
		
		List<GameHistory> histories = GameHistory.dao.findBestScore(userId,gameId);
		if (histories.size() < 5) {
			setAttr("msg", "游戏已完成");
		} else {
			setAttr("msg", "您的前5名成绩");
		}
		
		setAttr("score", score);
		setAttr("game", Game.dao.findById(gameId));
		setAttr("histories", histories);
		setAttr("sequence", sequence);
		render("/train/complete.html");
		
	}
	/**
	 * 当日训练计划的总结
	 */
	public void summary() {
		Account account = getSessionAttr("userInfo");//获取用户
		Long userId = account.getUserId();//获取用户id
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());//获取用户今日训练计划
		Integer trainPlanId = trainPlan.getTrainPlanId();//获取用户今日训练计划id
		List<PlanGame> planGames = PlanGame.dao.findByTrainPlanId(trainPlanId);//获取用户今日游戏计划
		List<Game> games = new ArrayList<>();
		for (PlanGame planGame : planGames) {
			games.add(Game.dao.findById(planGame.getGameId()));
			Game.dao.findById(planGame.getGameId()).getGameClassId();
		}
		setAttr("games", games);
		
		render("/train/turbo_summary.html");
	}
	/**
	 * 训练计划中的剩余游戏
	 */
	public void rest() {
		
		
	}
}

	