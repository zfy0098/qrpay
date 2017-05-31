package com.example.demo.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
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
@WebServlet("/YMFTrade")
public class YMFTradeService2  extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7512355375652322398L;

	LoggerTool logger = new LoggerTool(this.getClass());
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
		req.setCharacterEncoding("utf-8");
		
		/** 用户编号ID **/
		String userID = (String) req.getAttribute("userID");
		String merName = (String) req.getAttribute("merName");
		String amount = (String) req.getAttribute("amount");
		String ymfCode = (String) req.getAttribute("ymfCode");
		
		String tradeDate = (String)req.getAttribute("tradeDate");
		String tradeTime = (String)req.getAttribute("tradeTime");
		String orderNumber = (String)req.getAttribute("orderNumber");
		
		merName = URLDecoder.decode(merName , StringEncoding.UTF_8);
		
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
		Map<String,Object> merchantMap = TradeDB.getMerchantInfo(new Object[]{user.getID() , qrCodeMap.get("PayChannel")});
		if(merchantMap==null||merchantMap.isEmpty()){
			logger.info(user.getLoginID() + "获取商户信息失败");
			return ;
		}
		
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
		
		Map<String,Object> map = new LinkedHashMap<String,Object>(); 
		
		String callbackUrl = LoadPro.loadProperties("http", "callbackUrl");
		
		String serverCallbackUrl = LoadPro.loadProperties("http", "YMFPayNotify");
		
		double amt = AmountUtil.div(amount, "100" , 2);
		
		/** 默认交易交易类型为 微信扫码支付 **/
		String url = LoadPro.loadProperties("http", "WX_JSCodeUrl");
		String trxType = Constant.WX_SCANCODE_JSAPI;
		
		/** 如果支付类型为支付宝扫码支付 **/
		if(qrCodeMap.get("PayChannel").equals(Constant.payChannelAliScancode)){
			trxType =  Constant.Alipay_SCANCODE_JSAPI; 
			url = LoadPro.loadProperties("http", "ALIPAY_JSCodeUrl");
		}
		
		map.put("trxType", trxType);
		map.put("merchantNo", merchantID);
		map.put("orderNum", orderNumber);
		map.put("amount", amt);
		map.put("goodsName", merName);
		map.put("callbackUrl", callbackUrl);
		map.put("serverCallbackUrl", serverCallbackUrl);
		map.put("orderIp", "1.1.1.1");
		/** T0 交易上报结算信息  **/
		if(encrypt.equals(Constant.T0)){
			try {
				Map<String,Object> bankMap = TradeDB.getBankInfo(user.getID());
				
				String toibkn = TradeDB.getBankNoByBankCardNo(bankMap.get("AccountNo").toString());
				
				String cardNo = DESUtil.encode(desKey,bankMap.get("AccountNo").toString());
				String idCardNo = DESUtil.encode(desKey,  user.getIDCardNo());
				String payerName = DESUtil.encode(desKey,bankMap.get("AccountName").toString());
				
				map.put("toibkn", toibkn);
				map.put("cardNo", cardNo);
				map.put("idCardNo", idCardNo);
				map.put("payerName", payerName);
				map.put("phoneNumber", user.getLoginID());
			} catch (Exception e) {
				e.printStackTrace();
				return ;
			}
		}
		
		map.put("encrypt", encrypt);
		StringBuffer str = new StringBuffer("#");
		for (String  key : map.keySet()) {
			str.append(map.get(key)); 
			str.append("#");
		}
		
		String sign = MD5.sign( str + signKey  , StringEncoding.UTF_8);
		map.put("sign", sign);
		
		logger.info("请求报文:" + map.toString());
		
		try {
			String content = HttpClient.post(url, map, "1");
			logger.info("请求获取二维码响应体:" + content);
			JSONObject json = JSONObject.fromObject(content);
			String retCode = json.getString("retCode");
			if(retCode.equals(Constant.payRetCode)){
				String qrurl = json.getString("qrCode");
				resp.sendRedirect(qrurl);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("固定码支付请求异常:"  + e.getMessage()); 
		}
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
