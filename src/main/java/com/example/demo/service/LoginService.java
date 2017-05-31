package com.example.demo.service;

import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

public class LoginService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	/**
	 *   用户登录
	 * @param reqData
	 * @param repData
	 */
	public void Login(RequestData reqData , ResponseData repData){
		
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		
		Object obj = ehcache.get(Constant.cacheName,reqData.getLoginID() + "UserInfo" );

		System.out.println(reqData.getLoginID());

		TabLoginuser user ;
		if(obj == null){
			logger.info("查询数据库");
			user = LoginUserDB.loginuser(reqData.getLoginID());
			if(user == null){
				logger.info("未查到用户 " + reqData.getLoginID() +"信息");
				repData.setRespCode(RespCode.userDoesNotExist[0]);
				repData.setRespDesc(RespCode.userDoesNotExist[1]);
				return ;
			}
			ehcache.put(Constant.cacheName, user.getLoginID() + "UserInfo" , user);
		}else{
			logger.info("查询缓存");
			user = (TabLoginuser) obj;
		}
		
		logger.info("用户" +  user.getLoginID() + "登录");
		
		String passwd = MakeCipherText.calLoginPwd(reqData.getLoginID(),user.getLoginPwd(), reqData.getSendTime());
		
		if(!passwd.equals(reqData.getLoginPwd())){
			logger.info("用户" + user.getLoginID() + "密码错误, 上送密码：" + reqData.getLoginPwd() + ", 平台计算密码:" + passwd);
			repData.setRespCode(RespCode.PasswordError[0]);
			repData.setRespDesc(RespCode.PasswordError[1]);
			return;
		}
		
		int nRef = LoginUserDB.updateUserLoginInfo(new Object[]{DateUtil.getNowTime(DateUtil.yyyyMMddHHmmss) ,reqData.getTerminalInfo() , user.getLoginID()});
		
		if(nRef == 0){
	    	logger.info("终端号：" + reqData.getTerminalInfo() + " ,用户名："+ user.getLoginID()+"登录失败");
	    	repData.setRespCode(RespCode.ServerDBError[0]);
	    	repData.setRespDesc(RespCode.ServerDBError[1]);
	    	return ;
	    }
		
		//获取终端主密钥
	    Map<String,Object> map = LoginUserDB.selectTermKey(user.getID());
	    
	    String initKey = LoadPro.loadProperties("jmj", "3");
	    
	    String tmk = "";
		try {
			tmk = DESUtil.bcd2Str(DESUtil.decrypt3(UtilsConstant.ObjToStr(map.get("TermTmkKey")), initKey));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	    
		//初始化返回包的信息
	    repData.setTerminalInfo(tmk);
	    repData.setBankCardNo(user.getBankCardNo());
	    repData.setBankName(user.getBankName());
	    
	    logger.info("用户"+ user.getLoginID()+"获取终端主秘钥:" +UtilsConstant.ObjToStr(map.get("TermTmkKey"))); 
	    
	    ehcache.clear(Constant.cacheName); 
	    
		//组返回给终端的报文
	    repData.setRespCode(RespCode.SUCCESS[0]);
	    repData.setRespDesc(RespCode.SUCCESS[1]);
	    
	    logger.info("用户"+ user.getLoginID()+"登录成功");
	}
}
