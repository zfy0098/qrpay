package com.example.demo.service;

import java.util.List;
import java.util.Map;

import com.example.demo.constant.RespCode;
import com.example.demo.db.YMFTradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.LoadPro;

import com.example.demo.util.LoggerTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class QueryYMFService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void QueryYMF(TabLoginuser user , RequestData reqdata , ResponseData respdata){
		
		logger.info("用户：" + user.getLoginID() + "查询到账类型" +  reqdata.getTradeCode() + "的固定码"); 
		
		List<Map<String,Object>> list = YMFTradeDB.getUserYMFlist(new Object[]{user.getID(),reqdata.getTradeCode()});
		
		String YMFUrl = LoadPro.loadProperties("config", "YMFUrl");
		
		JSONArray  jsonArray = new JSONArray();
		for (int i = 0; i < list.size(); i++){
			JSONObject json = new JSONObject();
			Map<String,Object> map = list.get(i);
			
			// Code,UserID,Valid,PayChannel,Binded,AgentID,TradeCode,Rate ,PayChannelName"
			json.put("Code", YMFUrl +  map.get("Code"));
			json.put("UserID", map.get("UserID"));
			json.put("Valid", map.get("Valid"));
			json.put("PayChannel", map.get("PayChannel"));
			json.put("Binded", map.get("Binded"));
			json.put("AgentID", map.get("AgentID"));
			json.put("TradeCode", map.get("TradeCode"));
			json.put("Rate", map.get("Rate"));
			json.put("PayChannelName", map.get("PayChannelName"));
			
			jsonArray.add(json);
		}
		respdata.setTranslist(jsonArray.toString());
		respdata.setRespCode(RespCode.SUCCESS[0]);
		respdata.setRespDesc(RespCode.SUCCESS[1]);
		
	}
}
