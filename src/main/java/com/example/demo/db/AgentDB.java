package com.example.demo.db;

import com.example.demo.util.UtilsConstant;

import java.text.DecimalFormat;
import java.util.Map;


public class AgentDB extends DBBase{

	
	/**
	 * 
	 *   查询代理商信息
	 * @param obj
	 * @return
	 */
	public static Map<String,Object> agentInfo(Object[] obj){
		String sql = "select * from tab_agent where ID=?";
		return jdbcTemplate.queryForMap(sql, obj);
	}
	
	
	/**
	 *   代理商配置信息
	 * @param obj
	 * @return
	 */
	public static Map<String,Object> agentConfig(Object[] obj){
		String sql = "select * from tab_agent_config where AgentID=? and PayChannel=?";
		return jdbcTemplate.queryForMap(sql, obj);
	}
	
	
	/**
	 *   全部舍掉
	 * @param amount
	 * @param fee
	 * @param top
	 * @return
	 */
	public static int makeFeeAbandon(String amount, Double fee, int top) {
		DecimalFormat format = new DecimalFormat("0.00");
		fee = Double.parseDouble(format.format(fee));
		Double feeTemp = (Long.parseLong(amount) * fee) / 1000;
		int poundage = (new Double(feeTemp)).intValue();
		if (poundage > top && top != 0) {
			return top;
		} else {
			return poundage;
		}
	}

	/**
	 *   舍弃小数部分并进一位
	 * @param amount
	 * @param fee
	 * @param top
	 * @return
	 */
	public static int makeFeeFurther(String amount, Double fee, int top) {
		Double feeTemp = Math.ceil((Long.parseLong(amount) * fee) / 1000);
		int poundage = (new Double(feeTemp)).intValue();
		if (poundage > top && top != 0) {
			return top;
		} else {
			return poundage;
		}
	}
	
	
	/**
	 *   获取T0附加手续费
	 * @return
	 */
	public static int T0additional(){
		String sql = "select * from tab_appconfig";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql);
		return Integer.parseInt(UtilsConstant.ObjToStr(map.get("T0AttachFee")));
	}
	
	
	
	
	/**
	 * 
	 *    查询用户deviceToken
	 */
	public static String getDeviceToken(String userID){
		String sql = "select UserID , DeviceToken from tab_devicetoken where UserID=?";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, new Object[]{userID});
		if(map!=null){
			String deviceToken = UtilsConstant.ObjToStr(map.get("DeviceToken"));
			return deviceToken;
		}
		return null;
	}
}
