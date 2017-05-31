package com.example.demo.service;

import java.util.List;
import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.TxDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.EhcacheUtil;

import com.example.demo.util.LoggerTool;
import net.sf.json.JSONArray;

public class TxService {
	
	LoggerTool logger = new LoggerTool(this.getClass());

	public ResponseData send(TabLoginuser loginuser, RequestData reqData , ResponseData repData){
		if(reqData.getAmount()==null ){
			logger.info("提现金额为空");
			repData.setRespCode(RespCode.TXAMOUNTError[0]);
			repData.setRespDesc(RespCode.TXAMOUNTError[1]);
	    	return repData;
		}
		if(Long.parseLong(reqData.getAmount())==0 ){
			logger.info("提现金额无效");
			repData.setRespCode(RespCode.TXAMOUNTError[0]);
			repData.setRespDesc(RespCode.TXAMOUNTError[1]);
	    	return repData;
		}
		
		
		//提现
		int nRet= TxDB.tx(loginuser.getID(), reqData.getAmount(),reqData.getSendSeqId(),reqData.getTxType());


		logger.info("调用提现存储过程返回的nRet：" + nRet);

		if(nRet==2){
			logger.info("余额不足");
			repData.setRespCode(RespCode.TXAMOUNTNOTENOUGH[0]);
			repData.setRespDesc(RespCode.TXAMOUNTNOTENOUGH[1]);
	    	return repData;
		}
		else if(nRet==3 || nRet==4){
			logger.info("系统故障,nRet="+nRet);
			repData.setRespCode(RespCode.ServerDBError[0]);
			repData.setRespDesc(RespCode.ServerDBError[1]);
	    	return repData;
		}
		else if(nRet==1){
			repData.setRespCode(RespCode.SUCCESS[0]);
			repData.setRespDesc(RespCode.SUCCESS[1]);
		}
		else{
			logger.info("系统故障,nRet="+nRet);
			repData.setRespCode(RespCode.ServerDBError[0]);
			repData.setRespDesc(RespCode.ServerDBError[1]);
		}
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		ehcache.remove(Constant.cacheName, loginuser.getLoginID() + "UserInfo");
		return repData;
	}
	
	public ResponseData getTxRecord(TabLoginuser loginuser,RequestData reqData , ResponseData repData){
		List<Map<String, Object>> list = TxDB.getTxRecordList(loginuser.getID(), reqData.getTxType());
		repData.setList(JSONArray.fromObject(list).toString());
		repData.setRespCode(RespCode.SUCCESS[0]);
		repData.setRespDesc(RespCode.SUCCESS[1]);
		return repData;
	}

}
