package com.example.demo.service;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.LoggerTool;

public class ExitService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void exit(TabLoginuser user , RequestData reqdata , ResponseData respdata){
		
		logger.info("用户" + user.getLoginID() + "退出程序"); 
		
		String loginID = user.getID();
		
		
		logger.info("清除缓存中所有内容");
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		ehcache.clear(Constant.cacheName);
		
		LoginUserDB.delkToken(new Object[]{loginID});
		
		respdata.setRespCode(RespCode.SUCCESS[0]);
		respdata.setRespDesc(RespCode.SUCCESS[1]);
		
	}
}
