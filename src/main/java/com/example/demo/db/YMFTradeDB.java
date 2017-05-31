package com.example.demo.db;

import java.util.List;
import java.util.Map;

/**
 *    固定码交易
 * @author a
 *
 */
public class YMFTradeDB extends DBBase{

	
	/**
	 *     查询码数据
	 * @param obj
	 * @return
	 */
	public static Map<String,Object> getYMFCode(Object[] obj){
		String sql = "select * from tab_ymf_qrcode where Code=?";
		return jdbcTemplate.queryForMap(sql, obj);
	}
	
	
	/**
	 *   查询固定码列表
	 * @param obj
	 * @return
	 */
	public static List<Map<String,Object>> getUserYMFlist(Object[] obj){
		String sql = "select Code,UserID,Valid,PayChannel,Binded,AgentID,TradeCode,Rate ,PayChannelName"
				+ " from tab_ymf_qrcode as qrcode , tab_pay_channel as tpc"
				+ " where qrcode.PayChannel=tpc.ID and qrcode.Valid=1 and UserID=? and TradeCode=?";
		return jdbcTemplate.queryForList(sql, obj);
	}
	
	
	/**
	 *   更新固定码绑定状态
	 * @param obj
	 * @return
	 */
	public static int updateBindedInfo(Object[] obj){
		String sql = "update tab_ymf_qrcode set UserID=? , Binded=1 , BindedDate=? where Code=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	
}
