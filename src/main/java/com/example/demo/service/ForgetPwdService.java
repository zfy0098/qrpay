package com.example.demo.service;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.SmsApplyDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

/**
 *     忘记密码
 * @author a
 *
 */
public class ForgetPwdService {

	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void ForgetPwd(TabLoginuser user , RequestData reqdata , ResponseData respdata){
		
		logger.info("用户：" + user.getLoginID() + "忘记密码，开始找回操作");
		
		String newLoginpwd = reqdata.getLoginPwd();
		
		String IDCard = reqdata.getIdNumber();
		
		String name = reqdata.getRealName();
		
		if(UtilsConstant.strIsEmpty(newLoginpwd)){
			logger.info("用户：" + user.getLoginID() + "没有填写登录密码");
			respdata.setRespCode(RespCode.ParamsError[0]);
			respdata.setRespDesc(RespCode.ParamsError[1]);
			return ;
		}
		
		if(UtilsConstant.strIsEmpty(IDCard)){
			logger.info("用户：" + user.getLoginID() + "没有填写身份证号码");
			respdata.setRespCode(RespCode.ParamsError[0]);
			respdata.setRespDesc(RespCode.ParamsError[1]);
			return ;
		}
		
		if(UtilsConstant.strIsEmpty(name)){
			logger.info("用户：" + user.getLoginID() + "没有填写真实名称");
			respdata.setRespCode(RespCode.ParamsError[0]);
			respdata.setRespDesc(RespCode.ParamsError[1]);
			return ;
		}
		
		//  如果身份证号不相等
		if(!IDCard.equals(user.getIDCardNo())){
			logger.info("用户：" + user.getLoginID() + "身份证号不一致,用户上传：" + IDCard + "系统中保存：" + user.getIDCardNo());
			respdata.setRespCode(RespCode.INFOError[0]);
			respdata.setRespDesc(RespCode.INFOError[1]);
			return ;
		}
		
		// 如果名字不相等
		if(!name.equals(user.getName())){
			logger.info("用户：" + user.getLoginID() + "用户真实姓名不一致,用户上传：" + name + "系统中保存：" + user.getName());
			respdata.setRespCode(RespCode.INFOError[0]);
			respdata.setRespDesc(RespCode.INFOError[1]);
			return ;
		}
		
		
		String code = reqdata.getSmsCode();
		
		String smsCode = SmsApplyDB.getSmsCode(new Object[]{user.getLoginID()});
		
		if(smsCode==null||!code.equals(smsCode)){
			logger.info("短息验证码错误:" + reqdata.getLoginID() + "数据库验证码：" + smsCode + "上报验证码：" + code);
			// public static final String[] SMSCodeError = {"E015" , "短息验证码错误，请核对"};
			respdata.setRespCode(RespCode.SMSCodeError[0]);
			respdata.setRespDesc(RespCode.SMSCodeError[1]);
			return ;
		}else{
			SmsApplyDB.delSmsCode(new Object[]{reqdata.getLoginID()});
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
