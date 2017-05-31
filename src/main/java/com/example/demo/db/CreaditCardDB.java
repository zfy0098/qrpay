package com.example.demo.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo.mode.TabBankConfig;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.UtilsConstant;


public class CreaditCardDB extends DBBase{
	

	LoggerTool logger = new LoggerTool(this.getClass());

	/**
	 * 校验在一段时间内 是否重复申请信用卡
	 * @param BankID
	 * @param ApplicantIDCardNo
	 * @param CreateTime
	 * @return
	 */
	public static boolean findCardApplyRecord(String BankID,String ApplicantIDCardNo ,String CreateTime ){
		boolean flag = false;
		String sql = "select * from tab_card_apply_record where BankID = ? and ApplicantIDCardNo =? and CreateTime > ?";
		List<Map<String,Object>> list = jdbcTemplate.queryForList(sql, new Object[]{BankID,ApplicantIDCardNo,CreateTime});

		if(list!=null&&list.size()>0){
			flag =true;
		}else{
			flag =false;
		}
		return flag;
	}
	
	public static int insertCardApplyRecord(String ID,String phone_number,String id_number,String real_name,String tjr_user_id,String create_time,String agency_number,String bank_id

			){
		String sql="insert into tab_card_apply_record(ID,ApplicantIDCardNo,ApplicantPhone,ApplicantName,UserID,OrganID,CreateTime,BankID) values (?,?,?,?,?,?,?,?)";
		int nRet= jdbcTemplate.update(sql, new Object[]{ID,id_number,phone_number,real_name,tjr_user_id,agency_number,create_time,bank_id});
		if(nRet==0){
		}
		return nRet;
	}
	
	/**
	 * 获取信用卡开通银行列表
	 * @return
	 */
	public static List<TabBankConfig> getBankList(){
		
		String sql = "select * from tab_bank_config where BankStatus = 1";
		List<TabBankConfig> list =  new ArrayList<TabBankConfig>();
		List<Map<String,Object>> list2 = jdbcTemplate.queryForList(sql);
		
		for (Map<String, Object> map : list2) {
			try {
				list.add(UtilsConstant.mapToBean(map, TabBankConfig.class));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return list;
	}
	
	public static TabBankConfig getTabBanConfigInfo(String id){
		
		String sql = "select * from tab_bank_config where ID=?";
		try {
			
			Map<String, Object> map = jdbcTemplate.queryForMap(sql, new Object[]{id});
			if(map != null && map.size() > 0&&!map.isEmpty()) { 
				TabBankConfig tbc  = UtilsConstant.mapToBean(map, TabBankConfig.class);
				return tbc;
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}
	
	
	public static List<Map<String, Object>> myCardShare(String userId){
		String sql = "select * from tab_splitting_detail where agent_id =?";
		List<Map<String,Object>> list = jdbcTemplate.queryForList(sql, new Object[]{userId});
		return list;
		
	}
	
	public static Map<String,Object> myCardFeeAmount(String userId){
		String sql = "select * from tab_capital where creditmer_id =?";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql ,  new Object[]{userId});
		if(list!=null && list.size()>0){
			return list.get(0);
		}
		return null;
	}

}
