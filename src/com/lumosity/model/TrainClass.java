package com.lumosity.model;

import java.util.List;

import com.lumosity.model.base.BaseTrainClass;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class TrainClass extends BaseTrainClass<TrainClass> {
	public static final TrainClass dao = new TrainClass();
	
	/**查询所有记录**/
	public List<TrainClass> findAll() {
		return find("select * from train_class");
	}
}