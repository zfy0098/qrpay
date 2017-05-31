package com.example.demo.controller;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.constant.StringEncoding;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.TradeDB;
import com.example.demo.db.YMFTradeDB;
import com.example.demo.mode.Fee;
import com.example.demo.mode.PayOrder;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.service.NotifyService;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.MD5;
import com.example.demo.util.UtilsConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 *    固定码支付通知接口
 * @author a
 *
 */
@RestController
@RequestMapping("/YMFPayNotify")
public class YMFPayNotifyController {

	@Autowired
	private NotifyService notifyService;
	
	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	public Object YMFPayNotify(HttpServletRequest request , HttpServletResponse response) throws Exception{
		Map<String,String> map2 = new HashMap<String,String>();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = paramValues[0];
				if (paramValue.length() != 0) {
					map2.put(paramName, paramValue);
				}
			}
		}
		if(map2==null||map2.isEmpty()){
			logger.info("回调报文为空");
			return  RespCode.notifyfail;
		}
		logger.info("接收上游回调, 回调内容:" + map2.toString()); 
		
		StringBuffer text = new StringBuffer("#");
		/**  拼接加密字符串  **/
		for (String key : Constant.notifyParams) {
			if(map2.containsKey(key)){
				String value = map2.get(key);
				if(UtilsConstant.strIsEmpty(value)||"sign".equals(key)){
					continue;
				}
				text.append(value);
				text.append("#");
			}
		}
		/** 查询商户信息  **/
		String merchantID = map2.get("r1_merchantNo");
		Map<String,Object> map =  TradeDB.getMerchantInfo(merchantID , null);
		/**  计算签名 **/
		String serverSign = MD5.sign(text.append(map.get("SignKey")).toString(), StringEncoding.UTF_8);
		String reqSign = map2.get("sign");
		
		if(!serverSign.equals(reqSign)){
			logger.info("平台计算签名：" + serverSign + ", 通知上传签名：" + reqSign);
			return RespCode.notifyfail;
		}

		/** 获取订单号  **/
		String orderNumber = map2.get("r2_orderNumber");

		/**  查询订单信息 **/
		PayOrder order = TradeDB.getPayOrderInfo(orderNumber);
		
		if(order==null){
			logger.info("订单号：" + orderNumber + "未查到订单信息");
			return RespCode.notifyfail;
		}
		
		/** 查询固定码信息  **/
		Map<String,Object> qrcode = YMFTradeDB.getYMFCode(new Object[]{order.getYMFCode()});
		if(qrcode==null||map.isEmpty()){
			logger.info("订单号：" + orderNumber + "固定码信息为空");
			return RespCode.notifyfail;
		}
		String retCode = map2.get("retCode");
		String orderStatus = map2.get("r8_orderStatus");
		
		logger.info("订单：" + orderNumber + "状态为 : retCode= " + retCode + " , orderStatus=" + orderStatus);
		
		if((Constant.payRetCode.equals(retCode)&&Constant.orderStatus.equals(orderStatus))||
				(Constant.T0RetCode.equals(retCode)&&Constant.orderStatus.equals(orderStatus))){
			
			String retMsg = "支付成功";
			
			if(map2.containsKey("retMsg")){
				retMsg = map2.get("retMsg");
			}
			TabLoginuser loginUser = LoginUserDB.getLoginuserInfo(order.getUserID());
			
			Fee fee = notifyService.YMFcalProfit(order, loginUser, qrcode);
			
			int updateRet = TradeDB.updatePayOrderPayRetCode(new Object[]{retCode ,retMsg ,fee.getMerchantFee() , null , order.getID()});
			if(updateRet < 1){
				logger.info("订单号：" + orderNumber + "更新数据库失败"); 
				return RespCode.notifyfail;
			}
			
			TradeDB.saveProfit(new Object[]{
					UtilsConstant.getUUID(),loginUser.getID(), order.getID() ,fee.getMerchantFee(),fee.getAgentID(),fee.getAgentProfit(),
					fee.getTwoAgentID(),fee.getTwoAgentProfit(),
					null , fee.getPlatformProfit(),fee.getPlatCostFee()
			});
		}
		return RespCode.notifySuccess;
	}
}
