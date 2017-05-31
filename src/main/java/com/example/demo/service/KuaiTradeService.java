package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.constant.StringEncoding;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

import net.sf.json.JSONObject;

public class KuaiTradeService {
LoggerTool log  = new LoggerTool(this.getClass());
	
	@SuppressWarnings("unchecked")
	public void send(TabLoginuser loginUser, RequestData reqData , ResponseData repData){
		log.info("用户"+  loginUser.getLoginID() + "发起支付请求") ;
		
		/**  获取支付类型 **/
		String payChannel = reqData.getPayChannel();
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		
		/** 查询用户交易配置信息  **/
		Map<String,Object> map = null;
		Object obj = ehcache.get(Constant.cacheName, loginUser.getID() + payChannel + "userConfig");
		if(obj == null){
			log.info("缓存读取用户支付配置信息失败，从数据中读取， 用户：" + loginUser.getID() + " , 支付类型:" + payChannel);
			map = TradeDB.getUserConfig(new Object[]{ loginUser.getID() , payChannel});
			if(map==null||map.isEmpty()){
				log.info("用户：" + loginUser.getID() + "支付类型：" + payChannel + "系统为查到该类型配置信息" );
				repData.setRespCode(RespCode.TradeTypeConfigError[0]);
				repData.setRespDesc(RespCode.TradeTypeConfigError[1]); 
				return ;
			}
			ehcache.put(Constant.cacheName, loginUser.getID() + payChannel + "userConfig" , map);
		}else{
			log.info("用户：" + loginUser.getID() + " , 支付类型:" + payChannel + "缓存读取信息成功 继续操作");
			map = (Map<String,Object>) obj;
			obj = null;
		}
		
		
		/**  获取交易商户  **/
		Map<String,Object> merchantMap = TradeDB.getMerchantInfo(new Object[]{loginUser.getID() ,  payChannel}); 
		if(merchantMap==null||merchantMap.isEmpty()){
			log.info(loginUser.getLoginID() + "获取商户信息失败");
			repData.setRespCode(RespCode.MerchantNoConfig[0]);
			repData.setRespDesc(RespCode.MerchantNoConfig[1]); 
			return ;
		}
		
		//  查询交易配置信息
		Map<String,Object> tradeConfig = null;
		obj = ehcache.get(Constant.cacheName, "tradeConfig");
		if(obj == null){
			log.info("缓存中获取交易配置信息失败,从数据库中查询");
			tradeConfig = TradeDB.getTradeConfig(); 
			ehcache.put(Constant.cacheName, "tradeConfig", tradeConfig); 
		}else{
			log.info("缓存查询交易配置信息");
			tradeConfig = (Map<String,Object>) obj;
		}
		
		String encrypt = Constant.T1;
		
		String nowtime = DateUtil.getNowTime(DateUtil.HHmm);
		
		Integer nowHour = Integer.parseInt(nowtime.split("-")[0]);
		
		/** T0交易开始小时和分钟 **/
		String KuaiT0Time = UtilsConstant.ObjToStr(tradeConfig.get("KuaiT0Time"));
		
		Integer startHour = Integer.parseInt(KuaiT0Time.split("-")[0]);
		/** T0交易结束小时和分钟 **/
		Integer EndHour = Integer.parseInt(KuaiT0Time.split("-")[1]);
		
		
		/**
		 * 判断当前交易时间段是否属于T0 交易时间段
		 * 如果该时间段不能发送T0交易将改为T1交易
		 */
		if(nowHour>=startHour&&nowHour<EndHour){
			log.info("属于T0交易时间段");
			encrypt = Constant.T0;
		}
		
		/** 判断交易金额是否小于T0最低金额 **/
		/*String t0minAmount = UtilsConstant.ObjToStr(tradeConfig.get("T0MinAmount"));
		if(Integer.parseInt(reqData.getAmount()) < Integer.parseInt(t0minAmount)){
			log.info("交易金额：" + reqData.getAmount() + "T0最小金额" + t0minAmount + "将交易转换为T1交易");
			encrypt = Constant.T1;
		}*/
		
		
		/** 终端发起的交易日期 **/
		String tradeDate = reqData.getSendTime().substring(0, 8);
		/** 终端发起的交易时间 **/
		String tradeTime = reqData.getSendTime().substring(8);
		
		/** 订单号 **/
		String orderNumber = UtilsConstant.getOrderNumber();
		
		/** 交易商户编号 **/
		String merchantID = merchantMap.get("MerchantID").toString();
		/** 交易签名秘钥 **/
		String signKey = merchantMap.get("SignKey").toString();
		/** 卡信息加密秘钥 **/
		String desKey = merchantMap.get("DESKey").toString();
		
		log.info("获取SignKey:"+ signKey);
		
		/** 向数据库插入初始化数据 **/
		int ret = TradeDB.tradeInit(new Object[]{UtilsConstant.getUUID(),reqData.getAmount() ,DateUtil.getNowTime(DateUtil.yyyyMMdd),DateUtil.getNowTime(DateUtil.HHmmss),
				tradeDate,tradeTime , reqData.getSendSeqId(), Constant.TradeType[0] , encrypt, loginUser.getID(),payChannel, merchantID,orderNumber});
		
		if(ret < 1 ){
			log.info("数据库保存信息失败");
			repData.setRespCode(RespCode.ServerDBError[0]);
			repData.setRespDesc(RespCode.ServerDBError[1]);
			return ;
		}
		
		String url = LoadPro.loadProperties("http", "KUAI_ScanCodeUrl");
		String trxType = Constant.KUAI_ScanCode;
		
		double amount = AmountUtil.div(reqData.getAmount(), "100" , 2);
		Map<String,Object> payrequest = new LinkedHashMap<String,Object>();
		payrequest.put("trxType",trxType);
		payrequest.put("merchantNo",  merchantID);
		payrequest.put("orderNum",orderNumber);
		payrequest.put("amount",amount);
		
		
		Map<String, Object> termKey = LoginUserDB.selectTermKey(loginUser.getID());
		String initKey = LoadPro.loadProperties("config", "DBINDEX");
		
		String bankCardno = "";
		String cardNo = "";
		try {
			log.info("用户：" + loginUser.getLoginID() + "请求无卡快捷支付请求 , 银行卡卡号：(密文)" + reqData.getBankCardNo());
			String desckey = DESUtil.deskey(UtilsConstant.ObjToStr(termKey.get("MacKey")), initKey);
			bankCardno = DES3.decode(reqData.getBankCardNo(), desckey);
			log.info("用户：" + loginUser.getLoginID() + "请求无卡快捷支付请求 , 银行卡卡号：(原文)" + bankCardno);

			cardNo = DESUtil.encode(desKey,bankCardno);
		} catch (Exception e) {
			e.printStackTrace();
			repData.setRespCode(RespCode.SYSTEMError[0]);
			repData.setRespDesc(RespCode.SYSTEMError[1]);
			return ;
		}
		
	    payrequest.put("goodsName",loginUser.getMerchantName());
	    payrequest.put("callbackUrl", LoadPro.loadProperties("http" , "WX_ScanCodeCallbackUrl"));
	    payrequest.put("serverCallbackUrl", LoadPro.loadProperties("http" , "WX_ScanCodeCallbackUrl"));
		payrequest.put("orderIp", "10.10.20.187");
		payrequest.put("cardNo", cardNo);
		payrequest.put("payerPhone", reqData.getPayerPhone());
		payrequest.put("kuaiType","PAY");
		payrequest.put("encrypt",encrypt);
		
		StringBuffer str = new StringBuffer("#");
		
		for (String  key : payrequest.keySet()) {
			str.append(payrequest.get(key)); 
			str.append("#");
		}
		
		String sign = MD5.sign( str + signKey , StringEncoding.UTF_8);
		payrequest.put("sign", sign);
		
		log.info("请求报文:" + payrequest.toString());
		
		try {
			String content = HttpClient.post(url, payrequest, "1");
			JSONObject json = JSONObject.fromObject(content);
			log.info("响应报文："+json.toString());
			String retCode=json.getString("respCode");
			String msg=json.getString("respMsg");
			//无卡快捷支付
			if (encrypt.equals(Constant.T0)) {
				if (retCode.equals(Constant.payRetCode)) {
					repData.setRespCode(RespCode.SUCCESS[0]);
					repData.setRespDesc(RespCode.SUCCESS[1]);
					repData.setOrderNumber(orderNumber);
					repData.setMerchantNo(merchantID);
				} else {
					repData.setRespCode(retCode);
					repData.setRespDesc(msg);
				}
			} else {
				/** T1返回状态码0000 **/
				if (retCode.equals(Constant.payRetCode)) {
					repData.setRespCode(RespCode.SUCCESS[0]);
					repData.setRespDesc(RespCode.SUCCESS[1]);
					repData.setOrderNumber(orderNumber);
					repData.setMerchantNo(merchantID);
				} else {
					repData.setRespCode(retCode);
					repData.setRespDesc(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("请求获取发生异常:"  + e.getMessage()); 
			repData.setRespCode(RespCode.HttpClientError[0]);
			repData.setRespDesc(RespCode.HttpClientError[1]);
		}
	}
	
	

	public void confirm(TabLoginuser loginUser, RequestData reqData , ResponseData repData){
		Map<String,Object> payrequest = new LinkedHashMap<String,Object>();
		/**  获取交易商户  **/
		Map<String,Object> merchantMap = TradeDB.getMerchantInfo(new Object[]{loginUser.getID() ,  "4"}); 
		if(merchantMap==null||merchantMap.isEmpty()){
			log.info(loginUser.getLoginID() + "获取商户信息失败");
			repData.setRespCode(RespCode.MerchantNoConfig[0]);
			repData.setRespDesc(RespCode.MerchantNoConfig[1]); 
			return ;
		}
		
		String signKey = merchantMap.get("SignKey").toString();
		String url = LoadPro.loadProperties("http", "KUAI_Confirm");
       
		payrequest.put("trxType","OnlineKuaiPayConfirm");
		payrequest.put("merchantNo", reqData.getMerchantNo());
		payrequest.put("orderNum",reqData.getOrderNumber());
		payrequest.put("smsVerifyCode", reqData.getSmsCode());
		StringBuffer str = new StringBuffer("#");
		for (String key : payrequest.keySet()) {
			str.append(payrequest.get(key));
			str.append("#");
		}
		String sign = MD5.sign(str + signKey, StringEncoding.UTF_8);
		payrequest.put("sign", sign);
		log.info("请求报文:" + payrequest.toString());
		
		try {
			String content = HttpClient.post(url, payrequest, "1");
			log.info("响应报文："+ content);
			JSONObject json = JSONObject.fromObject(content);
			String retCode=json.getString("respCode");
			String msg=json.getString("respMsg");
			if(retCode.equals(Constant.payRetCode)){
				repData.setRespCode(RespCode.SUCCESS[0]);
				repData.setRespDesc(RespCode.SUCCESS[1]);
			}else{
				repData.setRespCode(retCode);
				repData.setRespDesc(msg);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("请求获取发生异常:"  + e.getMessage()); 
			repData.setRespCode(RespCode.HttpClientError[0]);
			repData.setRespDesc(RespCode.HttpClientError[1]);
		}
	}
}
