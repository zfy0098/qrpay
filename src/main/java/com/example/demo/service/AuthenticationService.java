package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.authen.ApiTrans;
import com.example.demo.authen.GetDynKey;
import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.AuthenticationDB;
import com.example.demo.mode.*;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.UtilsConstant;
import net.sf.json.JSONArray;


public class AuthenticationService {
	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	
	public ResponseData send(TabLoginuser loginuser, RequestData reqData , ResponseData repData){
		if(reqData.getBankCardNo()==null || "".equals(reqData.getBankCardNo())){
			repData.setRespCode(RespCode.ParamsError[0]);
			repData.setRespCode(RespCode.ParamsError[1]);
			return repData;
		}
		if(reqData.getPhoneNumber() == null || "".equals(reqData.getPhoneNumber())){
			repData.setRespCode(RespCode.ParamsError[0]);
			repData.setRespCode(RespCode.ParamsError[1]);
			return repData;
		}
		if(reqData.getRealName() == null || "".equals(reqData.getRealName())){
			repData.setRespCode(RespCode.ParamsError[0]);
			repData.setRespCode(RespCode.ParamsError[1]);
			return repData;
		}
		if(reqData.getIdNumber() == null || "".equals(reqData.getIdNumber())){
			repData.setRespCode(RespCode.ParamsError[0]);
			repData.setRespCode(RespCode.ParamsError[1]);
			return repData;
		}
		Constant.TRADE_TYPE ="0411";
		Map<String, String> map = new HashMap<String, String>();
		map.put("accNo", reqData.getBankCardNo());
		map.put("certificateCode", reqData.getIdNumber());
		map.put("name", reqData.getRealName());
		map.put("nbr", reqData.getPhoneNumber());
		try {
			GetDynKey.getDynKey();
			TJJQResponseData tjresp = ApiTrans.doTrans(map);
			int ret = AuthenticationDB.Authentication(new Object[]{UtilsConstant.getUUID(),loginuser.getID(),reqData.getIdNumber(),reqData.getPhoneNumber(),
					reqData.getRealName() , reqData.getBankCardNo(),tjresp.getOrderId(),tjresp.getResultCode(),tjresp.getResultDesc()});
			if(tjresp.getResultCode() =="00"){
				if(ret>0){
					logger.info("用户" + reqData.getLoginID() + "鉴权成功");
					repData.setRespCode(RespCode.SUCCESS[0]);
					repData.setRespDesc(RespCode.SUCCESS[1]);
				}else {
					logger.info("插入数据库收银行书为：" + ret + "用户" + reqData.getLoginID() + "鉴权失败");
					repData.setRespCode(RespCode.ServerDBError[0]);
					repData.setRespDesc(RespCode.ServerDBError[1]);
				}
			}else{
				if(ret>0){
					logger.info("上游鉴权失败：" + tjresp.getOrderId() + "用户" + reqData.getLoginID() + "鉴权失败");
					repData.setRespCode(RespCode.TJJQError[0]);
					repData.setRespDesc(tjresp.getResultDesc());
				}else {
					logger.info("插入数据库收银行书为：" + ret + "用户" + reqData.getLoginID() + "鉴权失败");
					repData.setRespCode(RespCode.ServerDBError[0]);
					repData.setRespDesc(RespCode.ServerDBError[1]);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			repData.setRespCode(RespCode.ServerDBError[0]);
			repData.setRespDesc(RespCode.ServerDBError[1]);
			e.printStackTrace();
		}
		return repData;
	}
	
	public void getAuthenticationList(TabLoginuser loginuser,RequestData reqData , ResponseData repData){
		try {
			List<AuthenticationRecord> list = AuthenticationDB.getAuthenticationRecordList(new Object[]{"00",loginuser.getID()});
			repData.setAuthenList(JSONArray.fromObject(list).toString()); 
			repData.setRespCode(RespCode.SUCCESS[0]);
			repData.setRespDesc(RespCode.SUCCESS[1]);
		} catch (Exception e) {
			repData.setRespCode(RespCode.SystemConfigError[0]);
			repData.setRespDesc(RespCode.SystemConfigError[1]);
			logger.error("申请信用卡系统异常 ExceptionMessage:"+e.getMessage());
			e.printStackTrace();
		}
	}
	

}
