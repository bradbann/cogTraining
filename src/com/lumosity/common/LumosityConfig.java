package com.lumosity.common;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.lumosity.login.LoginSessionIntercepter;
import com.lumosity.model._MappingKit;

/**
 * 核心配置
 * @author Scott
 *
 */
public class LumosityConfig extends JFinalConfig {

	@Override
	public void configConstant(Constants me) {
		PropKit.use("druid.properties");
		me.setDevMode(PropKit.getBoolean("devMode", false));
		
		me.setError404View("/error/404.html");
		me.setError500View("/error/500.html");
		
	}

	@Override
	public void configRoute(Routes me) {
		
		me.add(new LumosityRoutes());
			
	}
	
	public static DruidPlugin createDruid(){
		return new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), 
				  PropKit.get("password").trim(),PropKit.get("driverClass"));
		
	}
	@Override
	public void configPlugin(Plugins me) {
		
		DruidPlugin druidPlugin = createDruid();
		me.add(druidPlugin);
		//数据库操作插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		me.add(arp);
		//设置方言
		arp.setDialect(new MysqlDialect());
//		arp.setShowSql(true);
		//数据库映射
		_MappingKit.mapping(arp);
		
		me.add(new EhCachePlugin());
	}

	@Override
	public void configInterceptor(Interceptors me) {
		me.add(new SessionInViewInterceptor());
		me.add(new LoginSessionIntercepter());
		me.add(new UrlFilterInterceptor());
		
	}

	@Override
	public void configHandler(Handlers me) {
		//全局处理器，设置上下文路径
		me.add(new ContextPathHandler("basePath"));
		
	}

	public static void main(String[] args){
		
		JFinal.start("WebRoot", 8080, "/", 5);
	}


}
