package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.demo.constant.RespCode;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;

import com.example.demo.mode.TabLoginuser;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class QueryTradeService {

	
	
	
	
	/**
	 *   交易查询
	 * @param loginUser
	 * @param reqData
	 * @param respData
	 */
	public void QueryTrade(TabLoginuser loginUser , RequestData reqData , ResponseData respData){
		
		List<Map<String,Object>> list = TradeDB.userQueryTradeList(new Object[]{loginUser.getID() , reqData.getPayChannel()});
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
		
		respData.setTranslist(jsonArray.toString()); 
		respData.setRespCode(RespCode.SUCCESS[0]);
		respData.setRespDesc(RespCode.SUCCESS[1]);
	}
}
