package com.lumosity.model;

import java.util.List;

import com.lumosity.model.base.BaseBrainClassProfile;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class BrainClassProfile extends BaseBrainClassProfile<BrainClassProfile> {
	public static final BrainClassProfile dao = new BrainClassProfile();
	
	
	public BrainClassProfile findByUserAndClass(Long id, int classId){
		return findFirst("select * from brain_class_profile where userId=? and gameClassId=?", id, classId);
	}
	
	public List<BrainClassProfile> findByUser(Long id){
		return find("select LPI from brain_class_profile where userId= ?", id);
	}
	
	public List<BrainClassProfile> findByClassSort(int classId, int LPI){
		return find("select * from brain_class_profile where gameClassId=? and LPI <= ?", classId, LPI);
	}
	public List<BrainClassProfile> findByClass(int classId){
		return find("select * from brain_class_profile where gameClassId=?", classId);
	}
	
	/**
	 * 根据LPI的大小进行排序
	 * @param id
	 * @return
	 */
	public List<BrainClassProfile> findByLPI(Long id){
		return find("select * from brain_class_profile where userId=? order by LPI desc", id);
	}
}