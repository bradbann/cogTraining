package com.lumosity.model;

import java.util.Date;
import java.util.List;

import com.lumosity.model.base.BaseDayRecord;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class DayRecord extends BaseDayRecord<DayRecord> {
	public static final DayRecord dao = new DayRecord();
	
	public DayRecord findByIdAndDate(Long id, Date date) {
		return findFirst("select * from day_record where userId =? and recordDate =?", id, date);
	}
	public List<DayRecord> findByUserSortByLPI(Long id) {
		return find("select * from day_record where userId =? order by LPI desc", id);
	}
}