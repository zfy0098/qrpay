package com.example.demo.service;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.LoadPro;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.MakeCipherText;


/**
 *   修改密码
 * @author a
 *
 */
public class UpdatePasswordService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void UpdatePassword(TabLoginuser user , RequestData reqdata , ResponseData respdata){
		
		String loginpwd = reqdata.getLoginPwd();
		
		String newLoginpwd = reqdata.getNewLoginPwd();
		
		String passwd = MakeCipherText.calLoginPwd(reqdata.getLoginID(),user.getLoginPwd(), reqdata.getSendTime());
		
		if(!passwd.equals(reqdata.getLoginPwd())){
			logger.info("用户" + user.getLoginID() + "密码错误, 上送密码：" + loginpwd + ", 平台计算密码:" + passwd);
			respdata.setRespCode(RespCode.PasswordError[0]);
			respdata.setRespDesc(RespCode.PasswordError[1]);
			return;
		}
		
		String initKey= LoadPro.loadProperties("config","protectINDEX");
		String initKey2= LoadPro.loadProperties("config","TMKINDEX");
		
		String password = MakeCipherText.MakeLoginPwd(initKey2,newLoginpwd,initKey);
		
		int ret = LoginUserDB.updatePassword(new Object[]{password , user.getLoginID()});
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		ehcache.remove(Constant.cacheName, user.getLoginID() + "UserInfo");
		
		if(ret > 0){
			logger.info("用户:" + user.getLoginID() + "修改密码成功");
			respdata.setRespCode(RespCode.SUCCESS[0]);
			respdata.setRespDesc(RespCode.SUCCESS[1]);
		}else{
			logger.info("用户:" + user.getLoginID() + "修改密码失败");
			respdata.setRespCode(RespCode.ServerDBError[0]);
			respdata.setRespDesc(RespCode.ServerDBError[1]);
		}
	}
}
