package com.lumosity.games;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.GameClass;
import com.lumosity.model.GameHistory;
import com.lumosity.model.UserGame;
import com.lumosity.model.Game;
import com.lumosity.utils.DateTimeKit;
import com.lumosity.utils.LPIKit;



/**
 * 游戏列表的显示控制
 * 游戏界面渲染和结果的显示
 * @author Scott
 *
 */
@Before(FitTestInterceptor.class)
public class GamesController extends Controller{
	
	/**从数据库中查询游戏并列表的显示**/
	public void index() {
		
		String filter = getPara("filter");//获取过滤参数
		List<GameClass> showClasses = new ArrayList<>();//游戏类型显示列表
		List<GameClass> gameClasses = new ArrayList<>();
		/*获取所有的游戏类型和游戏*/
		if (filter == null || "all".equals(filter)) {
			gameClasses = GameClass.dao.findAll();
			setAttr("filter", "all");
		} else {
			gameClasses = GameClass.dao.findByName(filter);
			setAttr("filter", filter);
		}
		List<Game> games = Game.dao.findAll();
		
		for (int i = 0; i < gameClasses.size(); i++) {
			//根据游戏类型查找游戏
			List<Game> hasGames = Game.dao.findByGameClassId(gameClasses.get(i).getGameClassId());
			if (hasGames.size() > 0) {
				//该类型有游戏，则该类型在页面显示
				showClasses.add(gameClasses.get(i));
			}
		}
		
		setAttr("showClasses", showClasses);
		setAttr("games", games);
		
		render("/games/games.html");
	}
	/**单次游戏显示界面**/
	public void play() {
		Account account = getSessionAttr("userInfo");//获取用户
		Long userId = account.getUserId();//获取用户id
		String userName = account.getUserName();
		
		String name = getPara(0);
		Game game = Game.dao.findByName(name);
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
		setAttr("game", game);
		render("/games/play.html");
	}
	
	/** 游戏数据的保存* */
	@Before(GameDataInterceptor.class)
	public void save() {
		Account account = getSessionAttr("userInfo");//获取用户
		Long userId = account.getUserId();//获取用户id
		
		int gameId = getParaToInt("gameId");
		String gameData = getPara("result");//获取异步提交的json字符串
		JSONObject jsonObject = new JSONObject(gameData);//使用json包解析json字符串
		int score = jsonObject.getInt("score");
		
		int LPI = LPIKit.getLPI(score);
		/*单次游戏的保存只需保存在gameHistory表中*/
		new GameHistory().set("gameId", gameId)
						.set("userId", userId)
						.set("score", score)
						.set("playDate", DateTimeKit.getDate())
						.set("gameLPI", LPI)
						.set("gameData", jsonObject.getString("trial_data"))
						.save();
		
//		int week = DateTimeKit.getDateWeekNum();
		
		setAttr("score", score);
		setAttr("LPI", LPI);
		setAttr("game", Game.dao.findById(gameId));
		List<GameHistory> histories = GameHistory.dao.findBestScore(userId, gameId);
		if (histories.size() < 5) {
			setAttr("msg", "游戏已完成");
		} else {
			setAttr("msg", "个人游戏前5名");
		}
		setAttr("histories", histories);
//			renderJson();
		render("/games/result.html");
		
	}
	
	
}
