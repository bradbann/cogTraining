package com.lumosity.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.plugin.ehcache.CacheKit;
import com.lumosity.games.GameDataInterceptor;
import com.lumosity.model.Account;
import com.lumosity.model.GameHistory;
import com.lumosity.model.Game;
import com.lumosity.model.PlanGame;
import com.lumosity.model.QuestionStatus;
import com.lumosity.model.TestPlan;
import com.lumosity.model.TrainPlan;
import com.lumosity.model.UserGame;
import com.lumosity.utils.*;


/**
 * 接口API controllerKey = /api/auth
 * @author Scott
 * @since 2017.05.17
 */
@Clear
public class AuthApiController extends Controller {
	
	public static final String emailReg = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
	public static final String phoneReg = "^1[3|4|5|7|8][0-9]{9}$";

	/**
	 * 注册功能
	 */
//	@ActionKey("/api/auth/doSignUp")
	public void doSignUp() {
		
		String accountInfo = getPara("accountInfo");
		Account account = Account.dao.isAccountExist(accountInfo);
		Account loginInfo = null;
		if (account != null) {
			//注册账号已存在，返回错误信息
			renderJson("msg", "邮箱或手机号已被注册！！");
		} else {
			Date birth = getParaToDate("date");
			String userName = getPara("userName");
			String password = getPara("password");
			
			if (Pattern.matches(emailReg, accountInfo)) {
				//邮箱
				new Account().set("userName", userName).set("email", accountInfo).set("createDate", DateTimeKit.getDate())
					.set("password", password).set("birthday", birth).save();
				loginInfo = Account.dao.findByEmailAndPwd(accountInfo, password);
			} else if (Pattern.matches(phoneReg, accountInfo)) {
				//手机
				new Account().set("userName", userName).set("mobileId", accountInfo).set("createDate", DateTimeKit.getDate())
					.set("password", password).set("birthday", birth).save();
				loginInfo = Account.dao.findByPhoneAndPwd(accountInfo, password);
			} else {
				//非法输入
				renderJson("msg", "你输入的邮箱或者手机号非法！请重新输入");
				return;
			}
			Long userId = loginInfo.getUserId();
			//创建适应性训练计划(固定为三种类型游戏)
			new TestPlan().set("userId", userId).set("gameId", 14).set("sequence", 1).set("isPlay", 0).save();//speed_match
			new TestPlan().set("userId", userId).set("gameId", 2).set("sequence", 2).set("isPlay", 0).save();//lost_in_migration
			new TestPlan().set("userId", userId).set("gameId", 7).set("sequence", 3).set("isPlay", 0).save();//memory_matrix
			//创建用户问卷状态
			new QuestionStatus().set("userId", userId).set("question1Status", 0).set("question2Status", 0).save();
			
			renderJson("msg", "success");
		}
		
	}
	/**
	 * 登录功能
	 */
//	@ActionKey("/api/auth/doLogin")
	public void doLogin() {
		
		String accountInfo = getPara("accountInfo");
		String pwd = getPara("password");
		
		Account account = Account.dao.findAccount(accountInfo, pwd);
		Map<String, Object> loginInfo = new HashMap<>();
		
		if (account != null) {
			Date birth = account.getBirthday();
			String email = account.getEmail();
			Integer gender = account.getGender();
			Integer edu = account.getEducationId();
			Integer job = account.getJobId();
			Date create = account.getCreateDate();
			String phone = account.getMobileId();
			String weixin = account.getWeixinId();
			
			if (birth == null) 
				birth = new Date();
			if (gender == null) 
				gender = 0;
			if (edu == null) 
				edu = 0;
			if (job == null) 
				job = 0;
			if (create == null) 
				create = new Date();
			if (phone == null) 
				phone = "";
			if (weixin == null) 
				weixin = "";
			if (email == null) 
				email = "";
			
			loginInfo.put("userId", account.getUserId());
			loginInfo.put("userName", account.getUserName());
			loginInfo.put("email", email);
			loginInfo.put("password", pwd);
			loginInfo.put("birthday", birth);
			loginInfo.put("gender", gender);
			loginInfo.put("educationId", edu);
			loginInfo.put("jobId", job);
			loginInfo.put("createDate", create);
			loginInfo.put("mobileId", phone);
			loginInfo.put("weixinId", weixin);
			loginInfo.put("isMember", 0);
			setAttr("msg", "success");
			setAttr("account", loginInfo);
			
			renderJson(new String[] {"msg", "account"});
		} else {
			renderJson("msg", "账号或密码错误！");
		}
	}
	/**
	 * 找回密码，校验下2次输入是否相同，一致直update
	 */
//	@ActionKey("/api/auth/doResetPassword")
	public void doResetPassword() {
		
		String accountInfo = getPara("accountInfo");
		Account account = Account.dao.isAccountExist(accountInfo);
		String password = getPara("password");
		if (password == null || "".equals(password.trim())) {
			renderJson("msg", "不能为空");
		} else {
			account.set("password", password).update();
			renderJson("msg", "success");
		}
	}
	
	/**
	 * 获取所有游戏
	 */
	@ActionKey("/api/game/games")
	public void games() {
		
		Long userId = getParaToLong("userId");
		Account account = Account.dao.findById(userId);
		String userName = account.getUserName();
		
		List<Object> games = new ArrayList<>();
		List<Game> gameList = Game.dao.findAll();
		for (Game game : gameList) {
			
			String gameName = game.getGameName();
			int gameId = game.getGameId();
			Integer isLike = 0;
			Integer isLocked = 0;
			String jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
						+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
						+DateTimeKit.getPlayDate()+"'}}";
			UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
			if (userGame != null) {
				if (userGame.getLastPlayDate() != null && userGame.getLastPlayScore() != null) {
					jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
						+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
						+DateTimeKit.getPlayDate()+"'}}";	
				}
				isLike = userGame.getIsLike();
				isLocked = userGame.getIsLocked();
			}
			
			Map<String, Object> gameMap = new HashMap<>();
			List<GameHistory> histories = GameHistory.dao.findBestScore(userId, gameId);
			if (histories.size() > 0) {
				gameMap.put("bestScore", histories.get(0).getScore());
			} else {
				gameMap.put("bestScore", 0);
			}
			String imgPath = "http://120.27.234.232/resources/images/games/" + game.getSmallImg();
			String padImgPath = "http://120.27.234.232/resources/images/games/" + game.getPadGameImg();
			gameMap.put("imgPath", imgPath);
			gameMap.put("padImgPath", padImgPath);
			gameMap.put("gameOpenCode", jsonStr);
			gameMap.put("gameId", gameId);
			gameMap.put("gameName", gameName);
			gameMap.put("brief", game.getBrief());
			gameMap.put("benefit", game.getBenefit());
			gameMap.put("gameFileName", game.getGameFileName());
			gameMap.put("gamePathName", game.getPathName());
			gameMap.put("gameClassId", game.getGameClassId());
			gameMap.put("isLike", isLike);
			gameMap.put("isLocked", isLocked);
			games.add(gameMap);
			
		}
		
		renderJson(games);
		
	}
	
	/**
	 * 用户游戏交互接口
	 */
	@ActionKey("/api/game/userGame")
	public void userGame() {
		
		Long userId = getParaToLong("userId");
		Integer gameId = getParaToInt("gameId");
		
		List<GameHistory> histories = GameHistory.dao.findBestScore(userId, gameId);
		if (histories.size() > 0) {
			renderJson("bestScore", histories.get(0).getScore());
		} else {
			renderJson("bestScore", 0);
		}
		
	}
	/**
	 * 获取用户游戏情况
	 */
	@ActionKey("/api/game/updateGameFavi")
	public void updateGameFavi() {
		
		Long userId = getParaToLong("userId");
		Integer gameId = getParaToInt("gameId");
		Integer isLike = getParaToInt("isLike", 0);
		
		UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
		
		if (userGame != null) {
			userGame.set("isLike", isLike).update();
		} else {
			new UserGame().set("userId", userId).set("gameId", gameId).set("isLike", isLike).save();
		}
		
		setAttr("msg", "更新成功！");
		setAttr("isLike", userGame.getIsLike());
		renderJson(new String[] {"msg","isLike"});
		
		
	}
	
	/**
	 * 获取用户游戏关系
	 */
	@ActionKey("/api/game/getGameFavi")
	public void getGameFavi() {
		
		Long userId = getParaToLong("userId");
		Integer gameId = getParaToInt("gameId");
		
		UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
		if (userGame != null) 
			renderJson(userGame.getIsLike());
		else 
			renderJson(0);
	}
	
	/**
	 * 拼接游戏开始json字符串
	 */
	@ActionKey("/api/game/startGame")
	public void startGame(){
		
		Long userId = getParaToLong("userId");
		Integer gameId = getParaToInt("gameId");
		
		Game game = Game.dao.findById(gameId);
		String gameName = game.getGameName();
		String userName = Account.dao.findById(userId).getUserName();
		String jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
					+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
					+DateTimeKit.getPlayDate()+"'}}";
		UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
		if (userGame != null) {
			if (userGame.getLastPlayDate() != null && userGame.getLastPlayScore() != null) {
				jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
					+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
					+DateTimeKit.getPlayDate()+"'}}";	
			}	
		}
		
		renderJson(jsonStr);
		/*
		 * {"fit_test":"1","game_id":"1","game_param":"速度匹配","username":"test",
		 * "token":"uqiwejhbaskjdbasdioqw","game_user_setting":{"level":"3","time":"5","score":"1230"},
		 * "updated_at":"201701011228"}
		 */
	}

	/**
	 * 单次游戏保存游戏数据
	 */
	@ActionKey("/api/game/postGameData")
	@Before(GameDataInterceptor.class)
	public void postGameData() {
		
		Long userId = getParaToLong("userId");
		int gameId = getParaToInt("gameId");
		
		String gameData = getPara("result");
		JSONObject jsonObject = new JSONObject(gameData);
		int score = jsonObject.getInt("score");
		int LPI = LPIKit.getLPI(score);
		//单次游戏的保存只需保存在gameHistory表中
		new GameHistory().set("gameId", gameId).set("userId", userId)
						.set("score", score).set("playDate", DateTimeKit.getDate())
						.set("gameLPI", LPI).set("gameData", jsonObject.getString("trial_data"))
						.save();
		//查询游戏前5名
		List<GameHistory> histories = GameHistory.dao.findBestScore(userId, gameId);
		
		setAttr("score", score);
		setAttr("histories", histories);
		renderJson(new String[] {"score","histories"});
		
	}
	/**
	 * 单次游戏保存游戏数据
	 */
	@ActionKey("/api/game/postTestGameData")
	public void postTestGameData() {
		
		Long userId = getParaToLong("userId");
		Integer gameId = getParaToInt("gameId");
		String gameData = getPara("result");//获取游戏传入数据
		
		List<TestPlan> testPlans = TestPlan.dao.remaining(userId);
		if (testPlans.size() > 0) {
			TestPlan testPlan = testPlans .get(0);
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
				setAttr("msg", "保存成功");
				setAttr("score", score);
				setAttr("sequence", testPlan.getSequence());
				renderJson(new String[] {"msg","score","sequence"});
			} else {
				renderJson("msg", "保存失败");
			}
		} else {
			renderJson("msg", "你已完成适应性训练计划！请勿再次提交数据！");
		}
		
		
		
	}
	/**
	 * 训练计划游戏保存游戏数据
	 */
	@ActionKey("/api/game/postTrainGameData")
	@Before(GameDataInterceptor.class)
	public void postTrainGameData() {
		
		Long userId = getParaToLong("userId");
		//获取用户今日训练计划
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());
		//获取用户今日训练计划id
		Integer trainPlanId = trainPlan.getTrainPlanId();
		//获取用户今日剩余游戏计划(以sequence升序排列)
		List<PlanGame> planGames = PlanGame.dao.findRemaining(trainPlanId);
		/*获取本次游戏的相关数据*/
		if (planGames.size() > 0) {
			int gameId = planGames.get(0).getGameId();
			
			String gameData = getPara("result");
			JSONObject jsonObject = new JSONObject(gameData);
			int score = jsonObject.getInt("score");
			int LPI = LPIKit.getLPI(score);
			//单次游戏的保存只需保存在gameHistory表中
			new GameHistory().set("gameId", gameId).set("userId", userId)
							.set("score", score).set("playDate", DateTimeKit.getDate())
							.set("gameLPI", LPI).set("gameData", jsonObject.getString("trial_data"))
							.save();
			//设置该游戏状态为已完成
			planGames.get(0).set("isPlay", 1).update();
			//设置今日计划的完成状态
			trainPlan.set("playTimes", trainPlan.getPlayTimes()+1).update();
			//查询游戏前5名
			List<GameHistory> histories = GameHistory.dao.findBestScore(userId, gameId);
			
			setAttr("score", score);
			setAttr("lpi", LPI);
			setAttr("histories", histories);
			setAttr("sequence", planGames.get(0).getSequence());
			
			renderJson(new String[] {"score","histories","sequence","lpi"});
		} else {
			renderJson("msg", "今日训练计划已完成，请勿提交！");
		}
		
		
	}
	/**
	 * 获取训练计划中游戏，需要传入用户id
	 * 返回几个计划游戏的id
	 */
	@ActionKey("/api/game/planGame")
	public void planGame() {
		
		Long userId = getParaToLong("userId");
		if ("".equals(userId) || userId == null) {
			renderJson("msg", "invali userId");
			return;
		}

		List<Object> games = new ArrayList<>();
		List<TestPlan> testPlans = TestPlan.dao.remaining(userId);
		if (testPlans.size() > 0){
			//适应性训练没完成
			setAttr("isTestPlanEnd", 0);
			testPlans = TestPlan.dao.findByUser(userId);
			for (TestPlan testPlan : testPlans) {
				Map<String, Object> gameMap = new HashMap<>();
				
				Game game = Game.dao.findById(testPlan.getGameId());
				int gameId = game.getGameId();
				String imgPath = "http://120.27.234.232/resources/images/games/" + game.getSmallImg();
				
				String gameName = game.getGameName();
				String userName = Account.dao.findById(userId).getUserName();
				String jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
						+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
						+DateTimeKit.getPlayDate()+"'}}";
				
				gameMap.put("imgPath", imgPath);
				gameMap.put("gameOpenCode", jsonStr);
				gameMap.put("isPlay", testPlan.getIsPlay());
				gameMap.put("sequence", testPlan.getSequence());
				gameMap.put("gameId", gameId);
				gameMap.put("gameClassId", game.getGameClassId());
				gameMap.put("gameName", gameName);
				gameMap.put("brief", game.getBrief());
				gameMap.put("benefit", game.getBenefit());
				gameMap.put("gameFileName", game.getGameFileName());
				
				games.add(gameMap);
				
			}
			setAttr("games", games);
			renderJson(new String[] {"isTestPlanEnd","games"});
			return;
		}
		List<PlanGame> planGames = new ArrayList<>();
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());
		if (trainPlan != null) {
			//已经创建训练计划
			Integer id = trainPlan.getTrainPlanId();
			planGames = PlanGame.dao.findByTrainPlanId(id);
			//该训练计划中没有游戏计划，创建游戏计划
			if (planGames == null) 
				CreateKit.createPlanGame(planGames, id);
			if (trainPlan.getPlayTimes() > 0 ) 
				setAttr("isBegin", 1);
			else 
				setAttr("isBegin", 0);
		} else {
			//没有计划,则创建计划
			new TrainPlan().set("userId", userId)
				.set("playTimes", 0)
				.set("trainPlanDate", DateTimeKit.getDate())
				.set("week", DateTimeKit.getDateWeekNum())
				.save();
			trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());
			//训练计划创建完成，则创建游戏计划
			CreateKit.createPlanGame(planGames, trainPlan.getTrainPlanId());
			setAttr("isBegin", 0);
		}
		
		for (PlanGame planGame : planGames) {
			Map<String, Object> gameMap = new HashMap<>();
			
			Game game = Game.dao.findById(planGame.getGameId());
			int gameId = game.getGameId();
			String imgPath = "http://120.27.234.232/resources/images/games/" + game.getSmallImg();
			
			String gameName = game.getGameName();
			String userName = Account.dao.findById(userId).getUserName();
			String jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
					+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
					+DateTimeKit.getPlayDate()+"'}}";
			UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
			if (userGame != null) {
				if (userGame.getLastPlayDate() != null && userGame.getLastPlayScore() != null) {
					jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'0','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
					+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'3','time':'5','score':'0'},'updated_at':'"
					+DateTimeKit.getPlayDate()+"'}}";	
				}	
			}
			gameMap.put("imgPath", imgPath);
			gameMap.put("gameOpenCode", jsonStr);
			gameMap.put("isPlay", planGame.getIsPlay());
			gameMap.put("gameId", gameId);
			gameMap.put("gameClassId", game.getGameClassId());
			gameMap.put("gameName", gameName);
			gameMap.put("brief", game.getBrief());
			gameMap.put("benefit", game.getBenefit());
			gameMap.put("gameFileName", game.getGameFileName());
			gameMap.put("sequence", planGame.getSequence());
			games.add(gameMap);
		}
		
		List<PlanGame> remainingPlans = PlanGame.dao.findRemaining(trainPlan.getTrainPlanId());
		switch (remainingPlans.size()) {
		case 0:
			setAttr("isTrainPlanEnd", 1);
			setAttr("planInfo", "非常棒！今日训练任务已完成！");
			break;
		case 1:
			setAttr("isTrainPlanEnd", 0);
			setAttr("planInfo", "加油加油，只剩一个任务了！");
			break;
		case 2:
			setAttr("isTrainPlanEnd", 0);
			setAttr("planInfo", "很轻松地完成了一个任务，继续努力！");
			break;
		case 3:
			setAttr("isTrainPlanEnd", 0);
			setAttr("planInfo", "很轻松地完成了一个任务，继续努力！");
			break;
		case 4:
			setAttr("isTrainPlanEnd", 0);
			setAttr("planInfo", "很轻松地完成了一个任务，继续努力！");
			break;
		case 5:
			setAttr("isTrainPlanEnd", 0);
			setAttr("planInfo", "今天有五个训练任务，快来热身吧！");
			break;
		default:
			setAttr("isTrainPlanEnd", 0);
			setAttr("planInfo", "训练计划生成出错，请联系管理员！");
			break;
		}
		setAttr("games", games);
		setAttr("isTestPlanEnd", 1);
		renderJson(new String[] {"isBegin","isTrainPlanEnd","isTestPlanEnd","planInfo","games"});
	}
	
	/**
	 * 获取训练计划中的下一个游戏
	 */
	@ActionKey("/api/game/testGame")
	public void testPlay() {
		
		Long userId = getParaToLong("userId");
		String userName = Account.dao.findById(userId).getUserName();
		List<TestPlan> userPlans = TestPlan.dao.remaining(userId);
		if (userPlans.size() > 0) {
			TestPlan testPlan = userPlans.get(0);
			Game game = Game.dao.findById(testPlan.getGameId());
			
			int isLike = 0;
			int gameId = game.getGameId();
			String gameName = game.getGameName();
			String jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
						+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
						+DateTimeKit.getPlayDate()+"'}}";
			UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
			if (userGame != null) {
				if (userGame.getLastPlayDate() != null && userGame.getLastPlayScore() != null) {
					jsonStr = "{'game':"+game.getPathName()+",'datas':{'fit_test':'0','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
						+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'3','time':'5','score':'0'},'updated_at':'"
						+DateTimeKit.getPlayDate()+"'}}";	
				}
				isLike = userGame.getIsLike();
			}
			String imgPath = "http://120.27.234.232/resources/images/games/" + game.getPadGameImg();
			
			Map<String, Object> turboGame = new HashMap<>();
			turboGame.put("sequence", testPlan.getSequence());
			turboGame.put("gameOpenCode", jsonStr);
			turboGame.put("isCollected", isLike);
			turboGame.put("imgPath", imgPath);
			turboGame.put("gameId", gameId);
			turboGame.put("gameClassId", game.getGameClassId());
			turboGame.put("gameName", gameName);
			turboGame.put("brief", game.getBrief());
			turboGame.put("benefit", game.getBenefit());
			turboGame.put("gameFileName", game.getGameFileName());
			
			renderJson("game",turboGame);
		} else {
			renderJson("msg", "适应性训练计划已完成！");
		}
		
	}
	
	/**
	 * 获取训练计划中的下一个游戏
	 */
	@ActionKey("/api/game/truboGame")
	public void turboPlay() {
		
		Long userId = getParaToLong("userId");
		String userName = Account.dao.findById(userId).getUserName();
		
		TrainPlan trainPlan = TrainPlan.dao.findByIdAndDate(userId, DateTimeKit.getDate());
		Integer trainPlanId = trainPlan.getTrainPlanId();
		List<PlanGame> userPlans = PlanGame.dao.findRemaining(trainPlanId);
		if (userPlans.size() > 0) {
			PlanGame planGame = userPlans.get(0);
			Game game = Game.dao.findById(planGame.getGameId());
			
			int isLike = 0;
			int gameId = game.getGameId();
			String gameName = game.getGameName();
			String jsonStr = "{'game':'"+game.getPathName()+"','datas':{'fit_test':'1','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
						+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'1','time':'5','score':'0'},'updated_at':'"
						+DateTimeKit.getPlayDate()+"'}}";
			UserGame userGame = UserGame.dao.findByUserIdAndGameId(userId, gameId);
			if (userGame != null) {
				if (userGame.getLastPlayDate() != null && userGame.getLastPlayScore() != null) {
					jsonStr = "{'game':"+game.getPathName()+",'datas':{'fit_test':'0','game_id':'"+gameId+"','game_param':'"+gameName+"','username':'"+userName+"','token':'"
						+ "uqiwejhbaskjdbasdioqw','game_user_setting':{'level':'3','time':'5','score':'0'},'updated_at':'"
						+DateTimeKit.getPlayDate()+"'}}";	
				}
				isLike = userGame.getIsLike();
			}
			
			Map<String, Object> turboGame = new HashMap<>();
			
			List<GameHistory> histories = GameHistory.dao.findBestScore(userId, gameId);
			if (histories.size() > 0) {
				turboGame.put("bestScore", histories.get(0).getScore());
			} else {
				turboGame.put("bestScore", 0);
			}
			String imgPath = "http://120.27.234.232/resources/images/games/" + game.getPadGameImg();
			
			turboGame.put("sequence", planGame.getSequence());
			turboGame.put("gameOpenCode", jsonStr);
			turboGame.put("isCollected", isLike);
			turboGame.put("imgPath", imgPath);
			turboGame.put("gameId", gameId);
			turboGame.put("gameClassId", game.getGameClassId());
			turboGame.put("gameName", gameName);
			turboGame.put("brief", game.getBrief());
			turboGame.put("benefit", game.getBenefit());
			turboGame.put("gameFileName", game.getGameFileName());
			
			
			renderJson("game",turboGame);
		} else {
			renderJson("msg","今日训练计划已完成，请明日再来~");
		}
		
	}
	/**
	 * 修改个人信息
	 */
//	@ActionKey("/api/auth/doUpdatePersonalInfo")
	public void doUpdatePersonalInfo(){
		
		Long userId = getParaToLong("userId");
		Account account = Account.dao.findById(userId);
		
		String name = getPara("name");
		Integer gender = getParaToInt("gender");
		Integer education = getParaToInt("education");
		Integer job = getParaToInt("job");
		
		if (account != null) {
			if (name != null) 
				account.set("userName", name).update();
			if (gender != null) 
				account.set("gender", gender).update();
			if (education != null) 
				account.set("educationId", education).update();
			if (job != null) 
				account.set("jobId", job).update();
			renderJson("msg", "success");
		} else {
			renderJson("msg", "invali userId");
		}
	}
	
	/**
	 * 发送验证码，整合邮箱和手机
	 */
	@ActionKey("/api/sendCode")
	public void sendCode() {
		
		String type = getPara("type");
		String code = RandomStringUtils.random(4, "0123456789");
		boolean flag = true;
		if (Pattern.matches(emailReg, type)) {
			//邮箱
			String content = "【认知训练平台】你的验证码为:" + code + "&nbsp;&nbsp;&nbsp;&nbsp;5分钟内有效！";
			flag = EmailKit.send(type, "认知训练平台验证码", content);
		} else if (Pattern.matches(phoneReg, type)) {
			//手机
			flag = SmsKit.send(type, code);
		} else {
			renderJson("msg","你输入的邮箱或者手机号非法！请重新输入");
			return;
		}
		if (flag) {
			CacheKit.put("valiCode", type, code);//放入缓存，5分钟内有效
			setAttr("msg", "验证码发送成功，请注意查收！");
			setAttr("code", code);
			renderJson(new String[] {"msg","code"});
		} else { 
			renderJson("msg", "验证码发送失败，请稍后重试！");
		}
	}
	
	/**
	 * 验证验证码的有效性，整合邮箱和手机
	 */
	@ActionKey("/api/valiCode")
	public void valiCode() {
		
		String type = getPara("type");
		String code = getPara("code");
		if (code.equals(CacheKit.get("valiCode", type)))
			renderJson("msg", "vali");
		else 
			renderJson("msg", "not vali");
	}
	
	/**
	 * 发送短信验证码
	 *//*
	public void sendSmsCode() {
		
		String phone = getPara("phone");
		String code = RandomStringUtils.random(4, "0123456789");
		boolean flag = SmsKit.send(phone, code);
		
		if (flag) {
			CacheKit.put("phoneCode", phone, code);//放入缓存，3分钟内有效
			setAttr("msg", "success");
		} else { 
			setAttr("msg", "短信发送失败，请稍后重试");
		}
		renderJson();
	}
	
	*//**
	 * 发送邮箱验证码
	 *//*
	public void sendEmailCode() {
		
		String email = getPara("email");
		String code = RandomStringUtils.random(4, "0123456789");
		String content = "【认知训练平台】你的验证码为:" + code;
		boolean flag = EmailKit.send(email, "认知训练平台验证码", content);
		
		if (flag) {
			CacheKit.put("emailCode", email, code);
			setAttr("msg", "success");
		} else { 
			setAttr("msg", "邮件发送失败，请稍后重试");
		}
		renderJson(new String[] {"msg"});
	}
	
	*//**
	 * 验证短信验证码
	 *//*
	public void valiPhoneCode() {
		
		String phone = getPara("phone");
		String code = getPara("code");
		if (code.equals(CacheKit.get("phoneCode", phone)))
			renderJson("msg", "vali");
		else 
			renderJson("msg", "not vali");
		
	}
	
	*//**
	 * 验证邮箱验证码
	 *//*
	public void valiEmailCode() {
		
		String email = getPara("email");
		String code = getPara("code");
		if (code.equals(CacheKit.get("emailCode", email)))
			renderText("vali");
		else 
			renderText("not vali");
		
	}*/
	
	
}




