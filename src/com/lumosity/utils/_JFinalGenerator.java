package com.lumosity.utils;

import javax.sql.DataSource;


import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;
import com.lumosity.common.LumosityConfig;

/**
 * 在数据库表有任何变动时，运行一下 main 方法，极速响应变化进行代码重构
 */
public class _JFinalGenerator {
	
	public static DataSource getDataSource() {
		PropKit.use("druid.properties");
		DruidPlugin druidPlugin = LumosityConfig.createDruid();
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}
	
	public static void main(String[] args) {
		// base model 所使用的包名
		String baseModelPackageName = "com.lumosity.model.base";
		// base model 文件保存路径
		String baseModelOutputDir = PathKit.getWebRootPath() + "/../src/com/lumosity/model/base";
		
		// model 所使用的包名 (MappingKit 默认使用的包名)
		String modelPackageName = "com.lumosity.model";
		// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String modelOutputDir = baseModelOutputDir + "/..";
		
		// 创建生成器
		Generator gernerator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
		// 添加不需要生成的表名
		gernerator.addExcludedTable("access_token","agent","app_version","brief","city","dict_data","dict_type","firstorg"
				,"priceauthor","pricediscount","pricetype","province","seconddept","supplier","sys_log","sys_login_record","sys_res"
				,"sys_role","sys_role_res","sys_user","sys_user_role","userauthor","priceauthorpay","priceauthorsub");
		// 设置是否在 Model 中生成 dao 对象
		gernerator.setGenerateDaoInModel(true);
		// 设置是否生成字典文件
		gernerator.setGenerateDataDictionary(false);
		// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
		//gernerator.setRemovedTableNamePrefixes("t_");
		//gernerator.setMetaBuilder(new MyMetaBuilder(getDataSource()));
		//gernerator.setDialect(new SqlServerDialect());
		// 生成
		gernerator.generate();
	}
}




