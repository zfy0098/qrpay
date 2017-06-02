package com.example.demo.db;

import com.example.demo.util.UtilsConstant;

import java.util.Map;


public class SmsApplyDB extends DBBase{
	
	public static int insertSmsCode(String phone,String smsCode){
		String sql="insert into tab_smscode(ID,phoneNum,smsCode) values( '" + UtilsConstant.getUUID() + "' ,'"+phone+"','"+smsCode+"')";
		return jdbcTemplate.update(sql);
	}

	
	public static String getSmsCode(Object[] object){
		String sql = "select * from tab_smscode where phoneNum=? order by insertTime desc ";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, object);
		String code = null;
		if(map!=null&&!map.isEmpty()){
			code = UtilsConstant.ObjToStr(map.get("smsCode"));
		}
		
		return code;
	}
	
	
	public static int delSmsCode(Object[] obj){
		String sql = "delete from tab_smscode where phoneNum=?";
		return jdbcTemplate.update(sql, obj);
	}
}
