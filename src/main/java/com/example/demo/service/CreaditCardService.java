package com.example.demo.service;

import java.text.SimpleDateFormat; 
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.demo.constant.RespCode;
import com.example.demo.db.CreaditCardDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabBankConfig;
import com.example.demo.mode.TabLoginuser;

import com.example.demo.util.LoggerTool;
import net.sf.json.JSONArray;


public class CreaditCardService {
	
	
	LoggerTool logger = new LoggerTool(this.getClass());
	/**
	 * 申请信用卡
	 * @param reqData
	 * @param repData
	 * @return
	 */
	public void applyForCard(TabLoginuser loginuser, RequestData reqData , ResponseData repData){
		try {
			logger.info("进入申请信用卡方法-----------------阿里克里");
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf2=new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance(); 
			calendar.add(Calendar.MONTH, -3);    //得到前3个月 
			String time = sdf2.format(calendar.getTime());
			boolean flag = CreaditCardDB.findCardApplyRecord(reqData.getBankId(), reqData.getIdNumber(), time);
			if(flag){
				repData.setRespCode("A3");
				repData.setRespDesc("此身份证90天内已经申请过");
			}
			String ID = UUID.randomUUID().toString();
			int rsNet = CreaditCardDB.insertCardApplyRecord(ID,reqData.getPhoneNumber(), reqData.getIdNumber(), reqData.getRealName(),"2222", sdf.format(new Date()),reqData.getAgencyNumber(),reqData.getBankId());
			if(rsNet ==0){
				repData.setRespCode("A2");
			}else{
				repData.setRespCode("00");
				TabBankConfig tbc  = CreaditCardDB.getTabBanConfigInfo(reqData.getBankId());
				repData.setRespDesc("交易成功");
				repData.setBankUrl(tbc.getBankUrl());
			}
		} catch (Exception e) {
			repData.setRespCode("A1");
			repData.setRespDesc("系统异常");
			e.printStackTrace();
			logger.error("申请信用卡系统异常 ExceptionMessage:"+e.getMessage());
		}
		
	}	
	
	/**
	 * 获取信用卡开通银行列表
	 * @param reqData
	 * @param repData
	 */
	public void GetBank(TabLoginuser loginuser,RequestData reqData , ResponseData repData){
		try {
			List<TabBankConfig> list = CreaditCardDB.getBankList();
			repData.setBankList(JSONArray.fromObject(list).toString()); 
			repData.setRespCode(RespCode.SUCCESS[0]);
			repData.setRespDesc(RespCode.SUCCESS[1]);
		} catch (Exception e) {
			repData.setRespCode("A3");
			repData.setRespDesc("系统异常");
			e.printStackTrace();
			logger.error("申请信用卡系统异常 ExceptionMessage:"+e.getMessage());
		}
				
	}	
	
	public void myCardShare(TabLoginuser loginuser,RequestData reqData , ResponseData repData){
		
		try {
			List<Map<String, Object>> list = CreaditCardDB.myCardShare(loginuser.getID());
			repData.setList(JSONArray.fromObject(list).toString());
			repData.setRespCode(RespCode.SUCCESS[0]);
			repData.setRespDesc(RespCode.SUCCESS[1]);
		} catch (Exception e) {
			repData.setRespCode("A3");
			repData.setRespDesc("系统异常");
			e.printStackTrace();
			logger.error("获取信用卡分润 ExceptionMessage:"+e.getMessage());
		}
	}

}
