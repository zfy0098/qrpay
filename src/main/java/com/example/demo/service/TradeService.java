package com.example.demo.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.constant.StringEncoding;
import com.example.demo.db.AgentDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

import net.sf.json.JSONObject;

public class TradeService {

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
		}else{
			log.info("缓存查询交易配置信息");
			tradeConfig = (Map<String,Object>) obj;
		}
		
		
		String encrypt = Constant.T1;
		
		String nowtime = DateUtil.getNowTime(DateUtil.HHmm);
		
		Integer nowHour = Integer.parseInt(nowtime.split("-")[0]);
		Integer nowMinute = Integer.parseInt(nowtime.split("-")[1]);
		
		/** T0交易开始小时和分钟 **/
		Integer startHour = Integer.parseInt(UtilsConstant.ObjToStr(tradeConfig.get("T0StartHour")));
		Integer startMinute = Integer.parseInt(UtilsConstant.ObjToStr(tradeConfig.get("T0StartMinute")));
		/** T0交易结束小时和分钟 **/
		Integer EndHour = Integer.parseInt(UtilsConstant.ObjToStr(tradeConfig.get("T0EndHour")));
		Integer EndMinute = Integer.parseInt(UtilsConstant.ObjToStr(tradeConfig.get("T0EndMinute")));
		
		/**
		 * 判断当前交易时间段是否属于T0 交易时间段
		 * 如果该时间段不能发送T0交易将改为T1交易
		 */
		if(nowHour>startHour&&nowHour<EndHour){
			log.info("属于T0交易时间段");
			encrypt = Constant.T0;
		}else if((nowHour==startHour&&nowMinute>=startMinute)||(nowHour==EndHour&&nowMinute<=EndMinute)){
			log.info("属于T0交易时间段");
			encrypt = Constant.T0;
		}
		
		/** 判断交易金额是否小于T0最低金额 **/
		String t0minAmount = UtilsConstant.ObjToStr(tradeConfig.get("T0MinAmount"));
		if(Integer.parseInt(reqData.getAmount()) < Integer.parseInt(t0minAmount)){
			log.info("交易金额：" + reqData.getAmount() + "T0最小金额" + t0minAmount + "将交易转换为T1交易");
			encrypt = Constant.T1;
		}
		
		
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
		
		
		/** 向数据库插入初始化数据 **/
		int ret = TradeDB.tradeInit(new Object[]{UtilsConstant.getUUID(),reqData.getAmount() ,DateUtil.getNowTime(DateUtil.yyyyMMdd),DateUtil.getNowTime(DateUtil.HHmmss),
				tradeDate,tradeTime , reqData.getSendSeqId(), Constant.TradeType[0] , encrypt, loginUser.getID(),payChannel, merchantID,orderNumber});
		
		if(ret < 1 ){
			log.info("数据库保存信息失败");
			repData.setRespCode(RespCode.ServerDBError[0]);
			repData.setRespDesc(RespCode.ServerDBError[1]);
			return ;
		}
		
		/** 默认交易交易类型为 微信扫码支付 **/
		String url = LoadPro.loadProperties("http" , "WX_ScanCodeUrl");
		String trxType = Constant.WX_ScanCode;
		
		/** 如果支付类型为支付宝扫码支付 **/
		if(reqData.getPayChannel().equals(Constant.payChannelAliScancode)){
			trxType =  Constant.Ali_ScanCode;
			url = LoadPro.loadProperties("http", "Alipay_ScanCodeUrl");
		}
		
		/** 如果支付类型为qq扫码支付 **/
		if(reqData.getPayChannel().equals(Constant.payChannelQQScancode)){
			trxType =  Constant.QQ_ScanCode;
			url = LoadPro.loadProperties("http", "QQ_ScanCodeUrl");
		}
		
		double amount = AmountUtil.div(reqData.getAmount(), "100" , 2);
		Map<String,Object> payrequest = new LinkedHashMap<String,Object>();
		payrequest.put("trxType",trxType);
		payrequest.put("merchantNo",  merchantID);
		payrequest.put("orderNum",orderNumber);
		payrequest.put("amount",amount);
		
		/** 如果交易类型为T0 将交易手续费上传 **/
		if(encrypt.equals(Constant.T0)){
			String T0SaleRate = UtilsConstant.ObjToStr(map.get("T0SaleRate"));
			
			if(UtilsConstant.strIsEmpty(T0SaleRate)){
				log.info("用户"+ loginUser.getLoginID() + "没有配置支付类型(编号)" + repData.getPayChannel() + "的T0费率.");
				repData.setRespCode(RespCode.SystemConfigError[0]);
				repData.setRespDesc(RespCode.SystemConfigError[1]);
				return ;
			}
			// T0 附加费用
			int T0additional;
			try {
				T0additional = Integer.parseInt(UtilsConstant.ObjToStr(tradeConfig.get("T0AttachFee")));
			} catch (NumberFormatException e) {
				log.error("格式化T0代付费用失败:" + e.getMessage() + "将代付费用默认设置为2元");
				T0additional = 200;
			}
			int T0fee = AgentDB.makeFeeFurther(reqData.getAmount(), Double.valueOf(T0SaleRate)  , 0) + T0additional ;
			payrequest.put("t0Fee", AmountUtil.div(T0fee + "" , "100" , 2 ));
		}
		
		payrequest.put("goodsName",loginUser.getMerchantName());
		payrequest.put("serverCallbackUrl", LoadPro.loadProperties("http" , "WX_ScanCodeCallbackUrl"));
		payrequest.put("orderIp","1.1.1.1");
		
		
		Map<String,Object> bankMap = TradeDB.getBankInfo(loginUser.getID());
		
		
		/**  上报结算信息  **/
		if(encrypt.equals(Constant.T0)){
			String toibkn = bankMap.get("BankCode").toString();
			if(toibkn == null){
				 toibkn = TradeDB.getBankNoByBankCardNo(bankMap.get("AccountNo").toString());
			}
			payrequest.put("toibkn", toibkn);
			
			try {
				String cardNo = DESUtil.encode(desKey,bankMap.get("AccountNo").toString());
				String idCardNo = DESUtil.encode(desKey,  loginUser.getIDCardNo());
				String payerName = DESUtil.encode(desKey,bankMap.get("AccountName").toString());
				
				payrequest.put("cardNo", cardNo);
				payrequest.put("idCardNo", idCardNo);
				payrequest.put("payerName", payerName);
				
			} catch (Exception e) {
				e.printStackTrace();
				repData.setRespCode(RespCode.SYSTEMError[0]);
				repData.setRespDesc(RespCode.SYSTEMError[1]);
				return ;
			}
			payrequest.put("phoneNumber", loginUser.getLoginID());
		}
		
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
			log.info("请求获取二维码响应体:" + content);
			
			JSONObject json = JSONObject.fromObject(content);
			String retCode = json.getString("retCode");
			
			/** T0 交易如果成功将返回 10000  **/
			if(encrypt.equals(Constant.T0)){
				if(retCode.equals(Constant.T0RetCode)){
					repData.setQrCodeUrl(json.getString("qrCode")); 
					repData.setRespCode(RespCode.SUCCESS[0]);
					repData.setRespDesc(RespCode.SUCCESS[1]);
				}else{
					repData.setRespCode(retCode);
					if(json.has("msg")){
						repData.setRespDesc(json.getString("msg"));
					}else if(json.has("retMsg")){
						repData.setRespDesc(json.getString("retMsg"));
					}
				}
			}else{
				/** T1返回状态码0000 **/
				if(retCode.equals(Constant.payRetCode)){
					repData.setQrCodeUrl(json.getString("qrCode")); 
					repData.setRespCode(RespCode.SUCCESS[0]);
					repData.setRespDesc(RespCode.SUCCESS[1]);
				}else{
					repData.setRespCode(retCode);
					if(json.has("msg")){
						repData.setRespDesc(json.getString("msg"));
					}else if(json.has("retMsg")){
						repData.setRespDesc(json.getString("retMsg"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("请求获取二维码发生异常:"  + e.getMessage()); 
			repData.setRespCode(RespCode.HttpClientError[0]);
			repData.setRespDesc(RespCode.HttpClientError[1]);
			return ;
		}
	}
}
