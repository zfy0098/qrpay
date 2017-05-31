package com.example.demo.service;

import java.util.List;
import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.OpenKuaiDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class KuaiCardListService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	
	@SuppressWarnings("unchecked")
	public void kuaiCardList(TabLoginuser user, RequestData reqData , ResponseData repData){
		
		
		logger.info("-----用户：" + user.getLoginID() + "请求开通快捷银行卡列表");
		
		
		Object obj = null;
		//  查询交易配置信息
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		
		Map<String,Object> tradeConfig = null;
		obj = ehcache.get(Constant.cacheName, "tradeConfig");
		if(obj == null){
			logger.info("缓存中获取交易配置信息失败,从数据库中查询");
			tradeConfig = TradeDB.getTradeConfig();
		}else{
			logger.info("缓存查询交易配置信息");
			tradeConfig = (Map<String,Object>) obj;
		}
		
		String nowtime = DateUtil.getNowTime(DateUtil.HHmm);
		
		Integer nowHour = Integer.parseInt(nowtime.split("-")[0]);
		
		
		String KuaiT0Time = UtilsConstant.ObjToStr(tradeConfig.get("KuaiT0Time"));
		
		/** T0交易开始小时 **/
		Integer startHour = Integer.parseInt(KuaiT0Time.split("-")[0]);
		/** T0交易结束小时 **/
		Integer EndHour = Integer.parseInt(KuaiT0Time.split("-")[1]);
		
		
		Integer t0Result = 1;
		
		if(nowHour>=startHour&&nowHour<EndHour){
			t0Result = 0;
		}
		
		List<Map<String,Object>> list = OpenKuaiDB.kuaiCardlist(new Object[]{user.getID()});
		
		Map<String, Object> termKey = LoginUserDB.selectTermKey(user.getID());
		String initKey = LoadPro.loadProperties("config", "DBINDEX");
		
		logger.info("卡号列表：" + JSONArray.fromObject(list));
		
		try {
			String desckey = DESUtil.deskey(UtilsConstant.ObjToStr(termKey.get("MacKey")), initKey);
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				JSONObject json = new JSONObject();
				Map<String,Object> map = list.get(i);
				json.put("cardno", DES3.encode(UtilsConstant.ObjToStr(map.get("bankCardNo")), desckey));
				/**
				 * 	代表是否可做交易 0 可做交易， 1 不可做交易
				 * 
				 * 	0 未开通 
				 * 	1 开通T0 
				 * 	2 开通T1 
				 * 	3 T1 与T0 都开通
				 * 
				 */
				String encrypt = UtilsConstant.ObjToStr(map.get("encrypt"));
				String status = "1";
				if(t0Result==1){
					// t1 时间段
					if("2".equals(encrypt)||"3".equals(encrypt)){
						status = "0";
					}
				}else if(t0Result==0){
					// t0 时间段
					if("1".equals(encrypt)||"3".equals(encrypt)){
						status = "0";
					}
				}
				logger.info("当前到账时间段： " +t0Result+ " , 卡号："+UtilsConstant.ObjToStr(map.get("bankCardNo")) + ",当前卡号交易状态：" + status);
				
				json.put("status", status);		
 				array.add(json); 
			}
			repData.setTranslist(array.toString()); 
			repData.setRespCode(RespCode.SUCCESS[0]);
			repData.setRespDesc(RespCode.SUCCESS[1]);
		} catch (Exception e) {
			e.printStackTrace();
			repData.setRespCode(RespCode.SYSTEMError[0]);
			repData.setRespDesc(RespCode.SYSTEMError[1]);
			return ;
		}
	}
}
