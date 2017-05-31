package com.example.demo.db;


import java.util.List;
import java.util.Map;

import com.example.demo.mode.PayOrder;
import com.example.demo.util.UtilsConstant;

public class TradeDB extends DBBase{

	
	/**
	 *     查看用户配置信息
	 * @param obj
	 * @return
	 */
	public static Map<String,Object> getUserConfig(Object[] obj){
		String sql = "select * from tab_user_config where UserID=? and PayChannel=?";
		return  jdbcTemplate.queryForMap(sql, obj);
	}
	
	
	/**
	 *    用户查询各种交易类型的费率
	 * @param obj
	 * @return
	 */
	public static List<Map<String,Object>> getUserFeeRate(Object[] obj){
		String sql = "select tpc.ID,PayChannelName , T1SaleRate , T1SettlementRate , T0SaleRate , T0SettlementRate , SaleAmountMax , SaleAmountMaxDay "
				+ " from tab_user_config as tuc , tab_pay_channel as tpc"
				+ " where tuc.PayChannel=tpc.ID and tuc.UserID=?";
		return jdbcTemplate.queryForList(sql, obj);
	}
	
	
	/**
	 *   查询交易类型
	 * @return
	 */
	public static List<Map<String,Object>> getPayChannel(){
		String sql = "select * from  tab_pay_channel";
		return jdbcTemplate.queryForList(sql);
	}
	
	
	
	public static int[] saveUserConfig(List<Object[]> list){
		String sql = "insert ignore into tab_user_config (ID,UserID,PayChannel,SaleAmountMax,SaleAmountMaxDay,T1SaleRate,T0SaleRate,T1SettlementRate,T0SettlementRate)"
				+ " values (?,?,?,?,?,?,?,?,?)";
		return jdbcTemplate.batchUpdate(sql,list);
	}
	
	
	/**
	 *   查询商户信息
	 * @param obj
	 * @return
	 */
	public static Map<String,Object> getMerchantInfo(Object[] obj){
		
		String sql = "select * from tab_pay_merchant where UserID=? and PayType=?";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, obj);
		if(map==null||map.isEmpty()){
			return null;
		}
		return map;
	}
	
	/**
	 *    根据商户号查询商户信息
	 * @param merchantID
	 * @return
	 */
	public static Map<String,Object> getMerchantInfo(String merchantID , String payType){
		String sql = "select * from tab_pay_merchant where MerchantID=? and PayType=?";
		return jdbcTemplate.queryForMap(sql, new Object[]{merchantID,payType});
	}
	
	
	/**
	 *   保存商户信息
	 */
	public static int[] saveMerchantInfo(List<Object[]> list){
		String sql = "insert into tab_pay_merchant (MerchantID,MerchantName,SignKey,DESKey,QueryKey,UserID,PayType) values "
				+ "(?,?,?,?,?,?,?)";
		return jdbcTemplate.batchUpdate(sql,list);
//		return executeBatchSql(sql, list);
	}
	
	
	/**
	 *    查询交易配置信息
	 * @param
	 * @return
	 */
	public static Map<String,Object> getTradeConfig(){
		String sql = "select T0StartHour , T0StartMinute , T0EndHour , T0EndMinute , T0AttachFee , T0MinAmount , KuaiT0Time from tab_appconfig";
		return jdbcTemplate.queryForMap(sql);
	}
	
	/**
	 *    记录交易请求
	 * @param obj
	 * @return
	 */
	public static int tradeInit(Object[] obj){
		String sql = "insert into tab_pay_order (ID,Amount,LocalDate,LocalTimes,TradeDate,TradeTime,TermSerno,TradeType,TradeCode,UserID,PayChannel,MerchantID,OrderNumber) values "
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return jdbcTemplate.update(sql, obj);
	}
	
	
	public static int YMFTradeInit(Object[] obj){
		String sql = "insert into tab_pay_order (ID,Amount,LocalDate,LocalTimes,TradeDate,TradeTime,TermSerno,TradeType,TradeCode,UserID,PayChannel,MerchantID,OrderNumber,YMFCode) values "
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		return jdbcTemplate.update(sql, obj);
	}
	
	

	/**
	 *   根据平台订单号查询交易信息
	 * @param orderNumber
	 * @return
	 */
	public static PayOrder getPayOrderInfo(String orderNumber){
		String sql = "select * from tab_pay_order where orderNumber=?";
		
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, new Object[]{orderNumber});
		
		if(map == null || map.isEmpty()){
			return null;
		}
		PayOrder order = null;
		try {
			order = UtilsConstant.mapToBean(map, PayOrder.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return order;
	}
	
	
	/**
	 * 更新订单支付结果
	 * @param obj
	 * @return
	 */
	public static int updatePayOrderPayRetCode(Object[] obj){
		String sql = "update tab_pay_order set PayRetCode=? , PayRetMsg=? , Fee=? , MerchantProfit=?  where ID=?";
		return jdbcTemplate.update(sql, obj);
	}
	
	
	
	/**
	 *    更新订单代付结果
	 * @param obj
	 * @return
	 */
	public static int updateWithdrawStatus(Object[] obj){
		String sql = "update tab_pay_order set T0PayRetCode=? , T0PayRetMsg=? where ID=? ";
		return jdbcTemplate.update(sql, obj);
	}
	
	
	/**
	 *   保存收益信息
	 * @param obj
	 * @return
	 */
	public static int saveProfit(Object[] obj){
		String sql = "insert into tab_platform_profit (ID,UserID,TradeID,Fee,AgentID,AgentProfit,TwoAgentID,TwoAgentProfit,DistributeProfit,PlatformProfit,ChannelProfit)"
				+ " values (?,?,?,?,?,?,?,?,?,?,?)";
		return jdbcTemplate.update(sql, obj);
	}
	
	/** 
	 *   保存三级分销 各个商户的利润
	 * @param obj
	 * @return
	 */
	public static int[] saveDistributeProfit(List<Object[]> obj){
		String sql = "insert into tab_user_profit (ID,UserID,Amount,TradeTime,TradeID) "
				+ "values (?,?,?,?,?)";
		return jdbcTemplate.batchUpdate(sql, obj);
	}
	
	
	/***
	 *    终端用户查询自己的交易记录
	 * @param obj
	 * @return
	 */
	public static List<Map<String,Object>> userQueryTradeList(Object[] obj){
		String sql = "select OrderNumber , CONCAT(TradeDate,TradeTime) as TradeTime, Amount, PayRetCode , PayRetMsg , TermSerno, PayChannelName "
				+ " from tab_pay_order as tpo , tab_pay_channel as tpc where tpo.PayChannel=tpc.ID and tpo.UserID=? and tpc.ID=? and  PayRetCode='0000' ";
		return jdbcTemplate.queryForList(sql, obj);
	}
	
	
	/**
	 *    终端用户查询自己的收益记录
	 * @param obj
	 * @return
	 */
	public static List<Map<String,Object>> userQueryProfitList(Object[] obj){
		String sql = "select Tradetime,Amount from tab_user_profit where UserID=?";
		return jdbcTemplate.queryForList(sql, obj);
	}
	
	
	/**
	 *   根据银行卡号查询银联号
	 * @param BankCardNo
	 * @return
	 */
	public static String getBankNoByBankCardNo(String BankCardNo){
		String sql = "select bankName,unite_bank_no from tab_cardinfo c ,unite_bank u "
				+ " where substring(c.bankid,2,3)=substring(u.unite_bank_no,1,3) and  Substring(?,1,length(CardBin))=CardBin";
		Map<String,Object> map = jdbcTemplate.queryForMap(sql, new Object[]{BankCardNo});
		if(map!=null){
			return UtilsConstant.ObjToStr(map.get("unite_bank_no"));
		}
		return null;
	}
	
	/**
	 *   查询结算信息
	 * @param userID
	 * @return
	 */
	public static Map<String,Object> getBankInfo(String userID){
		String sql = "select * from tab_pay_userbankcard where UserID=?";
		return jdbcTemplate.queryForMap(sql, new Object[]{userID});
	}
}
