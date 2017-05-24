package com.lumosity.test;

import java.util.List;

import org.junit.Test;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.AnsiSqlDialect;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.lumosity.model.Account;
import com.lumosity.model.Game;
import com.lumosity.model.PlanGame;

public class TestSql {

	public static void main(String[] args) {
		
		PropKit.use("druid.properties");
		
//		C3p0Plugin C3p0Plugin = new C3p0Plugin(PropKit.get("jdbcUrl"), PropKit.get("user"), 
//				  PropKit.get("password").trim(),PropKit.get("driverClass"));
		
		DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), 
				  PropKit.get("password").trim(),PropKit.get("driverClass"));
		
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		
		arp.setDialect(new AnsiSqlDialect());
		arp.addMapping("[lumosity].[account].[account]", "userId", Account.class);
		//非web环境下，需要手动调用相关插件的start()方法
		druidPlugin.start();
		arp.start();
		
		Account account = Account.dao.findById(1);
		System.out.println("userId = " + account.getLong("userId") + "\n"
						+"userName = " + account.getStr("userName")+ "\n"
						+"email = " + account.getStr("email"));
	}
	
	@Test
	public  void testPlanGmes(){
		
		List<Game> games = Game.dao.findAll();
		for (Game games2 : games) {
			System.out.println(games2);
		}
		
		
	}
}
