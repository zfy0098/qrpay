package com.example.demo.service;

import java.util.List; 
import java.util.Map;
import java.util.Set;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.EhcacheUtil;

import com.example.demo.util.LoggerTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *    用户查询费率
 * @author a
 *
 */
public class QueryUserFeeRateService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	@SuppressWarnings("unchecked")
	public void QueryUserFeeRate(TabLoginuser user, RequestData reqdata, ResponseData respdata){
		
		logger.info("用户" + user.getLoginID() + "查询费率信息");
		
		List<Map<String,Object>> list = null;
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		
		Object obj = ehcache.get(Constant.cacheName, user.getLoginID() + "FeeRate" );
		if(obj == null){
			logger.info(user.getLoginID() + "从数据库查询费率信息");
			list = TradeDB.getUserFeeRate(new Object[]{user.getID()});
			ehcache.put(Constant.cacheName, user.getLoginID() + "FeeRate" , list);
		}else{
			logger.info("================== " + user.getLoginID() +"费率从缓存中查询");
			list = (List<Map<String,Object>>) obj;
		}
		
		
		JSONArray  jsonArray = new JSONArray();
		for (int i = 0; i < list.size(); i++){
			JSONObject json = new JSONObject();
			Map<String,Object> map = list.get(i);
			
			Set<String> keys = map.keySet();
			for (String key : keys) {
				
				json.put(key, map.get(key));
			}
			jsonArray.add(json);
		}
		
		logger.info("用户" + user.getLoginID() + "费率信息：" + jsonArray.toString());
		
		respdata.setTranslist(jsonArray.toString()); 
		respdata.setRespCode(RespCode.SUCCESS[0]);
		respdata.setRespDesc(RespCode.SUCCESS[1]);
		
	}
}
