package com.lumosity.test;

import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;

@Clear
public class DeleteController extends Controller {

	public void index() {
		
		Long userId = getParaToLong("id");
		String table = getPara("table");
		
		if (Db.deleteById(table, "userId", userId))
			renderJson("msg","delete success");
		else
			renderJson("msg","delete fail");
			
		
	}
}
