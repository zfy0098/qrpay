package com.example.demo.service;

import java.util.LinkedHashMap; 
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.constant.Constant;
import com.example.demo.constant.StringEncoding;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.TradeDB;
import com.example.demo.db.YMFTradeDB;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

import net.sf.json.JSONObject;

/***
 *   固定码交易
 * @author a
 *
 */
public class YMFTradeService   {

	/**
	 * 
	 */

	LoggerTool logger = new LoggerTool(this.getClass());
	
	
	public void YMFTrade(Map<String,String> map , HttpServletRequest request , HttpServletResponse response){
		
		/** 用户编号ID **/
		String userID = map.get("userID");
		String merName = map.get("merName");
		String amount = map.get("amount");
		String ymfCode = map.get("ymfCode");
		
		String tradeDate = map.get("tradeDate");
		String tradeTime = map.get("tradeTime");
		String orderNumber = map.get("orderNumber");
		
		/**  查询固定码信息 **/
		Map<String,Object> qrCodeMap = YMFTradeDB.getYMFCode(new Object[]{ymfCode});
		
		TabLoginuser user = null;
		try {
			user = LoginUserDB.getLoginuserInfo(userID);
		} catch (Exception e1) {
			logger.error("固定交易异常" +  e1.getMessage());
			return ;
		}
		
		/**  获取交易商户  **/
		Map<String,Object> merchantMap = TradeDB.getMerchantInfo(new Object[]{user.getID()});
		if(merchantMap==null||merchantMap.isEmpty()){
			logger.info(user.getLoginID() + "获取商户信息失败");
			return ;
		}
		
		/** 到账类型  **/
		String encrypt =  UtilsConstant.ObjToStr(qrCodeMap.get("TradeCode")); 
		
		/** 交易商户编号 **/
		String merchantID = merchantMap.get("MerchantID").toString();
		/** 交易签名秘钥 **/
		String signKey = merchantMap.get("SignKey").toString();
		/** 卡信息加密秘钥 **/
		String desKey = merchantMap.get("DESKey").toString();
		
		/** 向数据库插入初始化数据 **/
		int ret = TradeDB.YMFTradeInit(new Object[]{UtilsConstant.getUUID(),amount , DateUtil.getNowTime(DateUtil.yyyyMMdd),DateUtil.getNowTime(DateUtil.HHmmss),
				tradeDate,tradeTime , DateUtil.getNowTime(DateUtil.yyyyMMddHHmmssSSS), Constant.TradeType[1] ,encrypt,
				user.getID(),qrCodeMap.get("PayChannel"), merchantID,orderNumber , ymfCode});
		if(ret < 1 ){
			logger.info("数据库保存信息失败");
			return ;
		}
		
		Map<String,Object> paymap = new LinkedHashMap<String,Object>(); 
		
		/** 固定码请求地址 **/
		String url = LoadPro.loadProperties("http", "WX_JSCodeUrl");
		/** 前台通知地址 **/
		String callbackUrl = LoadPro.loadProperties("http", "callbackUrl");
		/** 服务端通知地址  **/
		String serverCallbackUrl = LoadPro.loadProperties("http", "WX_ScanCodeCallbackUrl");
		
		double amt = AmountUtil.div(amount, "100" , 2);
		paymap.put("trxType", "WX_SCANCODE_JSAPI");
		paymap.put("merchantNo", merchantID);
		paymap.put("orderNum", orderNumber);
		paymap.put("amount", amt);
		paymap.put("goodsName", merName);
		paymap.put("callbackUrl", callbackUrl);
		paymap.put("serverCallbackUrl", serverCallbackUrl);
		paymap.put("orderIp", "1.1.1.1");
		
		/** T0 交易上报结算信息  **/
		if(encrypt.equals(Constant.T0)){
			try {
				Map<String,Object> bankMap = TradeDB.getBankInfo(user.getID());
				
				String toibkn = TradeDB.getBankNoByBankCardNo(user.getBankCardNo());
//				String cardNo = DESUtil.encode(desKey,user.getBankCardNo());
//				String idCardNo = DESUtil.encode(desKey,user.getIDCardNo());
//				String payerName = DESUtil.encode(desKey,user.getName());
				
				
				String cardNo = DESUtil.encode(desKey,bankMap.get("AccountNo").toString());
				String idCardNo = DESUtil.encode(desKey,  user.getIDCardNo());
				String payerName = DESUtil.encode(desKey,bankMap.get("AccountName").toString());
				
				paymap.put("toibkn", toibkn);
				paymap.put("cardNo", cardNo);
				paymap.put("idCardNo", idCardNo);
				paymap.put("payerName", payerName);
				paymap.put("phoneNumber", user.getLoginID());
			} catch (Exception e) {
				e.printStackTrace();
				return ;
			}
		}
		paymap.put("encrypt", encrypt);
		StringBuffer str = new StringBuffer("#");
		for (String  key : paymap.keySet()) {
			str.append(paymap.get(key)); 
			str.append("#");
		}
		
		String sign = MD5.sign( str + signKey  , StringEncoding.UTF_8);
		paymap.put("sign", sign);
		
		logger.info("请求报文:" + paymap.toString());
		
		try {
			String content = HttpClient.post(url, paymap, "1");
			logger.info("请求获取二维码响应体:" + content);
			JSONObject json = JSONObject.fromObject(content);
			String retCode = json.getString("retCode");
			if(retCode.equals(Constant.payRetCode)){
				String qrurl = json.getString("qrCode");
				response.sendRedirect(qrurl);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("固定码支付请求异常:"  + e.getMessage()); 
		}
	}
		
}
