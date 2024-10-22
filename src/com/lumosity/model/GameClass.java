package com.lumosity.model;

import java.util.List;

import com.lumosity.model.base.BaseGameClass;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class GameClass extends BaseGameClass<GameClass> {
	public static final GameClass dao = new GameClass();
	
	public List<GameClass> findAll() {
		return find("select * from game_class where gameClassId < 6");
	}
	/**根据名称查询记录**/
	public List<GameClass> findByName(String name) {
		return find("select * from game_class where dis =?", name);
	}
}
