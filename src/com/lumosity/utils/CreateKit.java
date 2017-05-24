package com.lumosity.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.lumosity.model.Game;
import com.lumosity.model.PlanGame;
/**
 * 首页游戏推荐算法（需要后期完善）
 * @author Scott
 *
 */
public class CreateKit {

	/**
	 * 创建游戏计划(需要后期算法完善！现阶段只是随机从游戏库中抽取5个游戏！)
	 * @param games 游戏计划
	 * @param id	训练计划
	 * @return
	 */
	public static List<PlanGame> createPlanGame(List<PlanGame> games, Integer id) {
		List<Integer> gameIdList = createGameId();
		for (int i = 0; i < 5; i++) {
			Integer gameId = gameIdList.get(i);//取出随机游戏id
			PlanGame game = new PlanGame();
			game.set("trainPlanId", id)
				.set("gameId", gameId)
				.set("isPlay", 0)
				.set("sequence", i+1)
				.save();
			games.add(game);
		}
		return games;
	}
	
	/**
	 * 产生随机不重复的游戏id集合
	 * @return
	 */
	public static List<Integer> createGameId() {
		
		List<Integer> list = new ArrayList<>();//随机id集合
		List<Game> games = Game.dao.findAll();//游戏库所有游戏
		List<Integer> nums = new ArrayList<>();//所有游戏id集合
		for (Game game : games) {
			nums.add(game.getGameId());
		}
		
		for (int i = 0; i < nums.size(); i++) {
			Random random = new Random();
			Integer number = nums.get(random.nextInt(nums.size()));//随机id
			if (!list.contains(number)) {
				list.add(number);//放入集合
			}
		}
		
		return list;
		
	}
}
