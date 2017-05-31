package com.example.demo.service;

import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.LoggerTool;


/**
 *     查询商户
 * @author a
 *
 */
public class MyMerchantService {

	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void MyMerchant(TabLoginuser user, RequestData reqdata, ResponseData respdata) {
		
		
		logger.info("用户" + user.getLoginID() + "查询发展商户"); 

		// 查询交易
		String returnString = LoginUserDB.getMyMerchant(user.getID());

		respdata.setLoginID(reqdata.getLoginID());
		respdata.setTranslist(returnString);
		// 组返回给终端的报文
		respdata.setRespCode(RespCode.SUCCESS[0]);
		respdata.setRespDesc(RespCode.SUCCESS[1]);
	}
}
