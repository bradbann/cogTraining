package com.lumosity.test;

import java.util.ArrayList;
import java.util.List;

import com.lumosity.utils.ListKit;

public class TestList {

	public static void main(String[] args) {
		
		List<Integer> fourWeeksLPI = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			fourWeeksLPI.add(0);
		}
		fourWeeksLPI.add(120);
		fourWeeksLPI.add(0);
		fourWeeksLPI.add(0);
		fourWeeksLPI.add(140);
		fourWeeksLPI.add(220);
		fourWeeksLPI.add(0);
		fourWeeksLPI.add(0);
		fourWeeksLPI.add(80);
		
		/*for (int i = 0; i < fourWeeksLPI.size()-1; i++) {
			if (fourWeeksLPI.get(i+1) == 0) {
				fourWeeksLPI.set(i+1, fourWeeksLPI.get(i));
			}
		}*/
		ListKit.fillList(fourWeeksLPI);
		System.out.println(fourWeeksLPI);
		
	}
}
