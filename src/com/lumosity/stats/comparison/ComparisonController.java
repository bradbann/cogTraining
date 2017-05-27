package com.lumosity.stats.comparison;

import java.util.List;

import com.jfinal.core.Controller;
import com.lumosity.model.Account;
import com.lumosity.model.BrainClassProfile;
import com.lumosity.model.BrainProfile;
import com.lumosity.model.GameClass;

/**
 * LPI的比较(主要使用的是BrainProfile表)
 * @author Scott
 *
 */
public class ComparisonController extends Controller{

	/**
	 *
	 */
	public void index() {
		
//		int begin = getParaToInt(0);
//		int end = getParaToInt(1);
//		List<Account> accounts = Account.dao.findByBirthday(begin, end);
		
		Account account = getSessionAttr("userInfo");
//		Long userId = account.getUserId();
		Long userId = (long) 0;
		if (account != null)
			userId = account.getUserId();
		else
		    userId = getParaToLong("userId");
		
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
		render("/stats/comparison/compare.html");
		
	}
	
	
}
