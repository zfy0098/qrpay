package com.example.demo.db;

import java.util.Map;

public class AppVersionDB extends DBBase {

	
	
	/**
	 *   查询App版本信息
	 * @return
	 */
	public static Map<String,Object> getAppVersionInfo(Object[] obj){
		String sql = "SELECT * from tab_appversion where DeviceType=? order by UpdateTime desc limit 1";
		return jdbcTemplate.queryForMap(sql, obj);
	}
}
