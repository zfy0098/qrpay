package com.example.demo.service;

import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.AppVersionDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.LoadPro;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.UtilsConstant;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 *   查询App版本号;
 * @author a
 *
 */
public class AppVersionService {

	LoggerTool logger = new LoggerTool(this.getClass());

	public void appVersionInfo(RequestData reqData , ResponseData repData){
	
		/** 平台版本 **/
		if(UtilsConstant.strIsEmpty(reqData.getVersion())){
			repData.setRespCode(RespCode.ParamsError[0]);
			repData.setRespDesc(RespCode.ParamsError[1]); 
			return ;
		}
		Map<String,Object> map = null;
		
		String deviceType = reqData.getDeviceType();
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		
		Object obj = ehcache.get(Constant.cacheName,  deviceType + "appversion");
		if(obj==null){
			map = AppVersionDB.getAppVersionInfo(new Object[]{deviceType});
			ehcache.put(Constant.cacheName,  deviceType +  "appversion", map);
		}else{
			map = (Map<String,Object>) obj;
		}
		repData.setAppInfo(JSONObject.fromObject(map).toString()); 
		repData.setRespCode(RespCode.SUCCESS[0]);
		repData.setRespDesc(RespCode.SUCCESS[1]); 
	}
	
	
	public void MyQRCode(TabLoginuser user , RequestData reqdata, ResponseData repdata){
		
		logger.info("获取我的二维码"); 
		
		String myQRCode = LoadPro.loadProperties("config", "myqrcodeurl");
		repdata.setQrCodeUrl(myQRCode + user.getLoginID());
		//  分享链接
		repdata.setTerminalInfo(LoadPro.loadProperties("config", "myqrcodeurl") + user.getLoginID());
		
		repdata.setRespCode(RespCode.SUCCESS[0]);
		repdata.setRespDesc(RespCode.SUCCESS[1]);
	}
	
	
	
	public void adlist(RequestData reqdata, ResponseData repdata){
		
		repdata.setTranslist("[]"); 
		
		repdata.setRespCode(RespCode.SUCCESS[0]);
		repdata.setRespDesc(RespCode.SUCCESS[1]);
	}
	
}
