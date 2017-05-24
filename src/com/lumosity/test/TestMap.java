package com.lumosity.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lumosity.utils.MapKit;

public class TestMap {
	public static void main(String[] args) {
		
		Map<String, Integer> lpi = new HashMap<>();
		lpi.put("speed", 723);
		lpi.put("memory", 234);
		lpi.put("attention", 345);
		lpi.put("flexibility", 456);
		lpi.put("problem", 567);
		Map<String, Integer> sort = MapKit.sortByValue(lpi);
		System.out.println(lpi);
		/*List<Integer> lpi = new ArrayList<>();
		lpi.add(21);
		lpi.add(12);
		lpi.add(32);
		lpi.add(44);
		lpi.add(1);
		Collections.sort(lpi);
		System.out.println(lpi.get(lpi.size()-1));*/
	}

}
