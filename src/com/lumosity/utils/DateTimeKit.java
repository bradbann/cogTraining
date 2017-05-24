package com.lumosity.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期工具类：日期格式化，获取当前日期
 * @author Scott
 *
 */
public class DateTimeKit {
	
	public static final String pattern_ymd = "yyyy-MM-dd"; 
	/**
	 * 格式化日期
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date) {
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);
	}
	/**
	 * 获取当前时间的string形式
	 * @return
	 */
	public static String getStringDate() {
		DateFormat sdf = new SimpleDateFormat(pattern_ymd);
		return sdf.format(new Date());
	}
	/**
	 * 获取今日(yyyyMMddHHmm)格式的string型数据
	 * @return
	 */
	public static String getPlayDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		return format.format(new Date());
		
	}
	/**
	 * 获取今日(yyyy-MM-dd)格式的sql Date型数据
	 * @return
	 */
	public static Date getDate() {
		SimpleDateFormat format = new SimpleDateFormat(pattern_ymd);
		String dateString = format.format(new Date());
		Date date = java.sql.Date.valueOf(dateString);
		return date;
	}
	/**
	 * 解析日期
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static Date parse(String date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return format.parse(date);
		} catch (ParseException e) {
			
			return null;
		}
	}
	/**
	 * 获取星期int型
	 * @return
	 */
	public static int getDateWeekNum() {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int week = calendar.get(Calendar.DAY_OF_WEEK);
		return week;
		
	}
	/**
	 * 获取中文表示的星期数
	 * @return
	 */
	public static String getDateWeek() {
		
		String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int dayIndex = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
		if (dayIndex < 0) {
			dayIndex = 0;
		}
		return weekDays[dayIndex];
	}
	/**
	 * 获取最近4周
	 * @return
	 */
	public static List<String> getFourWeeks() {
		
		List<String> weeks = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        // 今天是一周中的第几天
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK );
        //获取最近四周
    	c.add(Calendar.DATE, -dayOfWeek-21);
    	for (int i=1;i<=28;i++) {
            c.add(Calendar.DATE, 1);
            weeks.add(sdf.format(c.getTime()));
        }
    	return weeks;
	}
	/**
	 * 获取今天之前的最近4周
	 * @return
	 */
	public static List<String> getFourWeeksTillToday() {
		List<String> weeks = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        // 今天是一周中的第几天
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK );
        //获取最近四周
    	c.add(Calendar.DATE, -dayOfWeek-21);
    	for (int i=1;i<=28;i++) {
            c.add(Calendar.DATE, 1);
            if (getDate().after(c.getTime())) {
            	weeks.add(sdf.format(c.getTime()));
			}
        }
    	weeks.add(getStringDate());
    	return weeks;
	}
	/**
	 * 获取2个日期之间的一周最后一天(周六)
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static List<String> getDaysBetweenTwoDates(Date beginDate, Date endDate) {  
        List<String> Dates = new ArrayList<String>();  
        Dates.add(format(beginDate));// 把开始时间加入集合  
        Calendar cal = Calendar.getInstance();  
        // 使用给定的 Date 设置此 Calendar 的时间  
        cal.setTime(beginDate);  
        boolean bContinue = true;  
        while (bContinue) {  
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量  
            cal.add(Calendar.DAY_OF_MONTH, 1);  
            // 测试此日期是否在指定日期之后  
            if (endDate.after(cal.getTime())) {  
            	/*if (cal.get(Calendar.DAY_OF_WEEK) == 7) {
					Dates.add(format(cal.getTime()));
				}*/
                Dates.add(format(cal.getTime()));  
            } else {  
                break;  
            }  
        }  
        Dates.add(format(endDate));// 把结束时间加入集合  
        return Dates;  
    }  
	
	public static boolean isWeekend(String date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(java.sql.Date.valueOf(date));
		if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
			return true;
		}
		return false;
	}
	
	public static String getWeekEnd(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		
		return format(calendar.getTime());
	}

	public static void main(String[] args) {

//		List<String> weeks = getDaysBetweenTwoDates(parse("2017-2-2"), parse("2017-3-14"));
//		
//		for (int i = 0; i < weeks.size(); i++) {
//			System.out.println(weeks.get(i));
//		}
//		System.out.println(1%7);
//		for (String day : weeks) {
//			if (isWeekend(day)) {
//				System.out.println(day);
//			}
//		}
//        List<String> a = getFourWeeksTillToday();
		Boolean flag = parse("2017-02-25").after(parse(getWeekEnd()));
		System.err.println(flag);
		
	}
}
