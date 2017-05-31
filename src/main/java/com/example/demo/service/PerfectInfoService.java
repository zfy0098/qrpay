package com.example.demo.service;

import  java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.UtilsConstant;

/**
 *   终端用户完善资料
 * @author a
 *
 */
public class PerfectInfoService {
	
	
	public void PerfectInfo(TabLoginuser user , RequestData reqData , ResponseData respData){
		
		//  商户名称
		String merchantName = reqData.getMerchantName();

		//  真实性名
		String name = reqData.getRealName();
		
		//  身份证号
		String IDcardNumber = reqData.getIdNumber();
		
		//  银行名称
		String bankName = reqData.getBankName();
		
		// 支行名称
		String bankSubbranch = reqData.getBankSubbranch();
		// 银行卡号
		String bankCardNo =  reqData.getBankCardNo();
		
		// 所在省份
		String state = reqData.getState();
		
		// 详细地址
		String address = reqData.getAddress();
		
		//  邮箱
		String email = reqData.getEmail();
		
		int x = LoginUserDB.updateUserInfo(new Object[]{ name, IDcardNumber , bankCardNo ,bankName ,bankSubbranch,merchantName ,state ,address ,email , user.getLoginID()});
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		ehcache.remove(Constant.cacheName, user.getLoginID() + "UserInfo");
		
		
		List<Map<String,Object>> payChannelList = TradeDB.getPayChannel();
		
		List<Object[]> userConfig = new ArrayList<Object[]>();
		for (Map<String, Object> map : payChannelList) {
			Object[] obj = new Object[]{UtilsConstant.getUUID(),user.getID(),map.get("ID"),0,0,5,5,4.9,4.9};
			userConfig.add(obj);
		}
		
		TradeDB.saveUserConfig(userConfig);
		
		
		if(x > 0){
			respData.setRespCode(RespCode.SUCCESS[0]);
			respData.setRespDesc(RespCode.SUCCESS[1]);
		}else{
			respData.setRespCode(RespCode.ServerDBError[0]);
			respData.setRespDesc(RespCode.ServerDBError[1]);
		}
	}
}
