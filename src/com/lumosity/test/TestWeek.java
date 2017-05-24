package com.lumosity.test;

import java.text.ParseException;  
import java.text.SimpleDateFormat;  
import java.util.ArrayList;  
import java.util.Calendar;  
import java.util.Date;  
import java.util.List;

import org.junit.Test;  
  
public class TestWeek {

	@Test
	public void testDate() throws Exception {
		System.out.println(new Date().getTime());
		System.out.println(System.currentTimeMillis());
	}
	
	
	public static void main(String[] args) throws ParseException {  
        //当前日期  
        Date time = new Date();  
        //获取当前日期所在的四个周的周日，其中当前日期的周日是第三个，第四个是下周周日  
        List<String> weekList = get4Weeks(time);  
        for (String week : weekList) {  
            System.out.println(week);  
        }  
    }  
	 private static List<String> get4Weeks(Date time) throws ParseException {  
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//设置时间格式  
	        Calendar cal = Calendar.getInstance();  
	        cal.setTime(time);  
	        //判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了  
	        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);//获得当前日期是一个星期的第几天  
	        if (1 == dayWeek) {  
	            cal.add(Calendar.DAY_OF_MONTH, -1);  
	        }  
	        cal.setFirstDayOfWeek(Calendar.SUNDAY);//设置一个星期的第一天，这是设为星期天，但是按中国的习惯一个星期的第一天是星期一  
	        int day = cal.get(Calendar.DAY_OF_WEEK);//获得当前日期是一个星期的第几天  
	        cal.add(Calendar.DATE, cal.getFirstDayOfWeek()-day);//根据日历的规则，给当前日期减去星期几与一个星期第一天的差值  
	        
	        String secondSunday = sdf.format(cal.getTime());  
	        //System.out.println("原始"+cal.getTime());  
	        //System.out.println("上星期日的日期：" + secondSunday);  
	        cal.add(Calendar.DATE, 7);  
	        String currentSunday = sdf.format(cal.getTime());  
	        //System.out.println("所在周星期日的日期：" + currentSunday);  
	        cal.add(Calendar.DATE, 7);  
	        String lastSunday =sdf.format(cal.getTime());  
	        //System.out.println("下周周日的日期："+ lastSunday);  
	        List<String> weekList = new ArrayList<String>();  
	        cal.add(Calendar.DATE,-21);  
	        String firstSunday =sdf.format(cal.getTime());  
	        weekList.add(firstSunday);  
	        weekList.add(secondSunday);  
	        weekList.add(currentSunday);  
	        weekList.add(lastSunday);  
	        return weekList;  
	    }  
}
