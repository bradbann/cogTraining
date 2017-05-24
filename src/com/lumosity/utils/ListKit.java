package com.lumosity.utils;

import java.util.List;
/**
 * 集合工具
 * @author Scott
 *
 */
public class ListKit {
	
	/**
	 * 对传入list集合进行补全
	 * @param data 需要补全的原list集合
	 * @return
	 */
	public static List<Integer> fillList(List<Integer> data) {
		
		for (int i = 0; i < data.size()-1; i++) {
			if (data.get(i+1) ==0) {
				data.set(i+1, data.get(i));
			}
		}
		return data;
	}

}
