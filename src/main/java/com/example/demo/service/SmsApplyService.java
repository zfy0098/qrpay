package com.example.demo.service;

import java.util.Random;

import com.example.demo.db.SmsApplyDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.util.LoadPro;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.SmsUtil;

public class SmsApplyService {
	LoggerTool logger = new LoggerTool(this.getClass());

	public ResponseData send(RequestData reqData , ResponseData repData){
		
		//生成验证码
		String smsCode = GetSmsCode();
		int nRet = SmsApplyDB.insertSmsCode(reqData.getLoginID(), smsCode);
		if(nRet==-1){
			logger.info("记录手机号校验码失败，手机号=【"+reqData.getLoginID()+"】");
			repData.setRespCode("96");
			repData.setRespDesc("手机号校验失败");
			return repData;
		}
		//发送短信
		nRet= SmsUtil.sendSMS(reqData.getLoginID(), smsCode, LoadPro.loadProperties("config", "ZhuCe"),"5",LoadPro.loadProperties("config", "APPID"));
		if(nRet==0){
			repData.setRespCode("00");
			repData.setRespDesc("短信已发送");
		}else{
			repData.setRespCode("96");
			repData.setRespDesc("短信发送失败");
		}
		return repData;
	}
	
	public ResponseData restPWd(RequestData reqData , ResponseData repData){
		
		//生成验证码
		String smsCode = GetSmsCode();
		int nRet = SmsApplyDB.insertSmsCode(reqData.getLoginID(), smsCode);
		if(nRet==-1){
			logger.info("记录手机号校验码失败，手机号=【"+reqData.getLoginID()+"】");
			repData.setRespCode("96");
			repData.setRespDesc("手机号校验失败");
			return repData;
		}
		//发送短信
		nRet=SmsUtil.sendSMS(reqData.getLoginID(), smsCode,LoadPro.loadProperties("config", "RestPwd"),"5",LoadPro.loadProperties("config", "APPID"));
		if(nRet==0){
			repData.setRespCode("00");
			repData.setRespDesc("短信已发送");
		}else{
			repData.setRespCode("96");
			repData.setRespDesc("短信发送失败");
		}
		return repData;
	}
	
	
	private String  GetSmsCode() {
		Random random = new Random();
		StringBuffer randBuffer = new StringBuffer();
		char[] codeSequence = { '0', '1', '2', '3', '4', '5','6', '7', '8', '9'};
		
		for (int i = 0; i < 4; i++) {
			randBuffer.append(String.valueOf(codeSequence[random.nextInt(10)]));
		}
		return randBuffer.toString();
		
	}	

}
