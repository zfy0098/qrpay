package com.example.demo.db;

import java.util.List;
import java.util.Map;

public class OpenKuaiDB extends DBBase{

	
	public static  Map<String,Object> getOpenKuai(Object[] obj){
		String sql = "select * from tab_openkuai where bankCardNo=?";
		return jdbcTemplate.queryForMap(sql, obj);
	}
	
	
	public static int save(Object[] obj){
		String sql = "INSERT INTO tab_openkuai (ID,UserID,bankCardNo,createDate,encrypt,statusCode) VALUES (?,?,?,now(),?,?) "
				+ " ON DUPLICATE KEY UPDATE encrypt=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	
	public static List<Map<String,Object>> kuaiCardlist(Object[] obj){
		String sql = "select * from tab_openkuai where UserID=? and encrypt!=0";
		return jdbcTemplate.queryForList(sql, obj);
	}
}
