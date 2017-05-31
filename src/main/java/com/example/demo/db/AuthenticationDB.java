package com.example.demo.db;

import com.example.demo.mode.AuthenticationRecord;
import com.example.demo.util.UtilsConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AuthenticationDB extends DBBase{
	
	
	public static int Authentication(Object[] obj){
		String sql = "insert into tab_authentication(ID,UserID,IdNumber,PhoneNumber, RealName , BankCardNo,OrderId,RespCode,RespDesc) values (?,?,?,?,?,?,?,?,?)";
		return  jdbcTemplate.update(sql, obj);
	}
	
	/**
	 * 获取信用卡开通银行列表
	 * @return
	 */
	public static List<AuthenticationRecord> getAuthenticationRecordList(Object[] obj){
		
		String sql = "select * from tab_authentication where RespCode = ? and UserID = ?";
		List<AuthenticationRecord> list =  new ArrayList<AuthenticationRecord>();
		List<Map<String,Object>> list2 = jdbcTemplate.queryForList(sql, obj);
		
		for (Map<String, Object> map : list2) {
			try {
				list.add(UtilsConstant.mapToBean(map, AuthenticationRecord.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return list;
	}

}
