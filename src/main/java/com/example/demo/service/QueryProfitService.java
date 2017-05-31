package com.example.demo.service;

import java.util.List; 
import java.util.Map;
import java.util.Set;

import com.example.demo.constant.RespCode;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;

import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.LoggerTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *    个人收益记录查询
 * @author a
 *
 */
public class QueryProfitService {
	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void QueryProfit(TabLoginuser user , RequestData reqData , ResponseData respData){
		
		logger.info("用户：" +  user.getLoginID() + "查询个人收益记录");
		
		List<Map<String,Object>> list = TradeDB.userQueryProfitList(new Object[]{user.getID()});
		
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
		
		respData.setSendTime(reqData.getSendTime());
		respData.setSendSeqID(reqData.getSendSeqId()); 
		respData.setTxndir(reqData.getTxndir());
		respData.setTranslist(jsonArray.toString()); 
		respData.setRespCode(RespCode.SUCCESS[0]);
		respData.setRespDesc(RespCode.SUCCESS[1]);
	}

}
