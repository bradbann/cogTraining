package com.lumosity.utils;

import java.util.Date;
import java.util.List;

import com.lumosity.model.DayClassRecord;
import com.lumosity.model.DayGameRecord;

/**
 * LPI算法
 * @author Scott
 *
 */
public class LPIKit {
	/**
	 * 游戏LPI初步算法
	 * @param score 游戏分数
	 * @return
	 */
	public static int getLPI(int score){
		
		int lpi = score/10;
		if (lpi > 2000) 
			lpi = 2000;
		return lpi;
	}
	/**
	 * 获取游戏类型 的LPI,初步算法即取出该游戏类型下所有游戏的日LPI，然后求平均数
	 * @param id 用户id
	 * @param classId 游戏类型id
	 * @return
	 */
	public static int getClassLPI(Long id, int classId, Date date){
		List<DayGameRecord> records = DayGameRecord.dao.findByIdAndClassAndDate(id, classId, date);
		
		//事实上，records必定大于0，因为它实在个人游戏档案表更新后执行的
		if (records.size() > 0) {
			int sum = 0;
			for (DayGameRecord dayRecord : records) {
				sum += dayRecord.getLPI();
			}
			return sum/records.size();
		} else {
			return 0;
		}
	}
	/**
	 * 获取用户当天的总LPI（currentLPI），初步算法：取出当天的个类型的LPI，然后求平均数
	 * @param id 用户id
	 * @param date 时间
	 * @return
	 */
	public static int getCurrentLPI(Long id, Date date){
		List<DayClassRecord> records = DayClassRecord.dao.findByIdAndDate(id, date);
		if (records.size() > 4) {
			//用户5种类型都有记录，才计算总LPI，否则为0
			int sum = 0;
			for (DayClassRecord record : records) {
				sum += record.getLPI();
			}
			return sum/records.size();
		} else {
			return 0;
		}
	}
}
