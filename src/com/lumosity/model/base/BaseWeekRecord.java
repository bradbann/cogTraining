package com.lumosity.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseWeekRecord<M extends BaseWeekRecord<M>> extends Model<M> implements IBean {

	public void setRecordId(java.lang.Integer recordId) {
		set("recordId", recordId);
	}

	public java.lang.Integer getRecordId() {
		return get("recordId");
	}

	public void setUserId(java.lang.Long userId) {
		set("userId", userId);
	}

	public java.lang.Long getUserId() {
		return get("userId");
	}

	public void setLPI(java.lang.Integer LPI) {
		set("LPI", LPI);
	}

	public java.lang.Integer getLPI() {
		return get("LPI");
	}

	public void setGameTimes(java.lang.Integer gameTimes) {
		set("gameTimes", gameTimes);
	}

	public java.lang.Integer getGameTimes() {
		return get("gameTimes");
	}

	public void setWeekEndDate(java.util.Date weekEndDate) {
		set("weekEndDate", weekEndDate);
	}

	public java.util.Date getWeekEndDate() {
		return get("weekEndDate");
	}

	public void setTrainDays(java.lang.Integer trainDays) {
		set("trainDays", trainDays);
	}

	public java.lang.Integer getTrainDays() {
		return get("trainDays");
	}

}