package com.lumosity.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseBrainProfile<M extends BaseBrainProfile<M>> extends Model<M> implements IBean {

	public void setUserId(java.lang.Long userId) {
		set("userId", userId);
	}

	public java.lang.Long getUserId() {
		return get("userId");
	}

	public void setOverall(java.lang.Integer overall) {
		set("overall", overall);
	}

	public java.lang.Integer getOverall() {
		return get("overall");
	}

	public void setBestLPI(java.lang.Integer bestLPI) {
		set("bestLPI", bestLPI);
	}

	public java.lang.Integer getBestLPI() {
		return get("bestLPI");
	}

	public void setBestDate(java.util.Date bestDate) {
		set("bestDate", bestDate);
	}

	public java.util.Date getBestDate() {
		return get("bestDate");
	}

	public void setLastDate(java.util.Date lastDate) {
		set("lastDate", lastDate);
	}

	public java.util.Date getLastDate() {
		return get("lastDate");
	}

	public void setStartDate(java.util.Date startDate) {
		set("startDate", startDate);
	}

	public java.util.Date getStartDate() {
		return get("startDate");
	}

}