package com.example.demo.service;


import com.example.demo.constant.RespCode;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.LoadPro;
import com.example.demo.util.LoggerTool;
import com.rom.util.md5.KeyBean;

public class MerchantReportService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void merchantReportURL(TabLoginuser user , RequestData reqData , ResponseData repdata){
		
		logger.info("用户" + user.getLoginID() +  "请求访问入网资料填写页面"); 
		
		
		if(user.getBankInfoStatus()==2||user.getBankInfoStatus()==1){
			repdata.setRespCode(RespCode.MerchantInfoError[0]);
			repdata.setRespDesc(RespCode.MerchantInfoError[1]);
			return;
		}
		
		
		String url = LoadPro.loadProperties("config", "reportURL");
		
		KeyBean keyBean = new KeyBean();
		
		
		
		String key = keyBean.getkeyBeanofStr(user.getLoginID());
		
		//  分享链接
		repdata.setTerminalInfo(url + user.getLoginID() + "&sign=" + key);
			
		repdata.setRespCode(RespCode.SUCCESS[0]);
		repdata.setRespDesc(RespCode.SUCCESS[1]);
	}
	
	
	public void getMerchantReportInfo(TabLoginuser user ,  RequestData reqData , ResponseData repdata){
		logger.info("用户" + user.getLoginID() +  "查看入网资料"); 
		
		String url = LoadPro.loadProperties("config", "reportInfoURL");
		//  分享链接
		repdata.setTerminalInfo(url + user.getLoginID());
			
		repdata.setRespCode(RespCode.SUCCESS[0]);
		repdata.setRespDesc(RespCode.SUCCESS[1]);
	}
}
