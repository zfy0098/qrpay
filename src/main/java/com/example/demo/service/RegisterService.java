package com.example.demo.service;

import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.SmsApplyDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

/**
 *   注册接口
 * @author a
 *
 */
public class RegisterService {
	
	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void Register(RequestData reqData , ResponseData respData){
		
		if(UtilsConstant.strIsEmpty(reqData.getLoginID())){
			logger.info("登录账号为空");
			respData.setRespCode(RespCode.ParamsError[0]);
			respData.setRespDesc(RespCode.ParamsError[1]);
			return;
		}
		
		if(UtilsConstant.strIsEmpty(reqData.getLoginPwd())){
			logger.info("注册密码为空");
			respData.setRespCode(RespCode.ParamsError[0]);
			respData.setRespDesc(RespCode.ParamsError[1]);
			return;
		}
		
		TabLoginuser loginUser = new TabLoginuser();
		loginUser.setLoginID(reqData.getLoginID()); 
		
		/*** 判断注册用户是否存在 ***/
		boolean flag = LoginUserDB.getLoginUserInfo(loginUser);
		if(flag){
			logger.info(reqData.getLoginID() +  "已经存在");
			respData.setRespCode(RespCode.RegisterError[0]);
			respData.setRespDesc(RespCode.RegisterError[1]);
			return ;
		}
		
		String code = reqData.getSmsCode();
		
		String smsCode = SmsApplyDB.getSmsCode(new Object[]{reqData.getLoginID()});
		
		if(!code.equals(smsCode)){
			
			logger.info("短息验证码错误:" + reqData.getLoginID() + "数据库验证码：" + smsCode + "上报验证码：" + code);
			// public static final String[] SMSCodeError = {"E015" , "短息验证码错误，请核对"};
			respData.setRespCode(RespCode.SMSCodeError[0]);
			respData.setRespDesc(RespCode.SMSCodeError[1]);
			return ;
		}else{
			SmsApplyDB.delSmsCode(new Object[]{reqData.getLoginID()});
		}
		
		
		String initKey= LoadPro.loadProperties("config","protectINDEX");
		String initKey2= LoadPro.loadProperties("config","TMKINDEX");
		
		String password = MakeCipherText.MakeLoginPwd(initKey2,reqData.getLoginPwd(),initKey);
		
		String tgr = reqData.getTgr();
		
		String threeLevel = "" , twoLevel = "" , oneLevel = "" ,  agentID = "5F2C0084-3455-4251-8ECD-03CB7E52EA2D";
		
		if(!UtilsConstant.strIsEmpty(tgr)){
			logger.info("推广人信息不为空，填写三级用户ID");
			TabLoginuser user = LoginUserDB.loginuser(tgr); 
			if(user!=null){
				threeLevel  = user.getID();
				twoLevel = user.getThreeLevel();
				oneLevel = user.getTwoLevel();
				agentID = user.getAgentID();
			}
		}else{
			logger.info("推广人信息为空，不填写三级用户");
		}
		
		String nowTime = DateUtil.getNowTime(DateUtil.yyyyMMddHHmmss);
		String userID = UtilsConstant.getUUID();
		int ret = LoginUserDB.registerUser(new Object[]{userID,reqData.getLoginID(),password,threeLevel, twoLevel , oneLevel, agentID ,nowTime}); 
		
		LoginUserDB.allocationTermk(userID);

		if(ret > 0){
			logger.info("用户" + reqData.getLoginID() + "注册成功");
			respData.setRespCode(RespCode.SUCCESS[0]);
			respData.setRespDesc(RespCode.SUCCESS[1]);
		}else{
			logger.info("插入数据库受影响行数：" + ret + "用户" + reqData.getLoginID() + "注册失败");
			respData.setRespCode(RespCode.ServerDBError[0]);
			respData.setRespDesc(RespCode.ServerDBError[1]);
		}
	}
}
