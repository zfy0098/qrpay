package com.example.demo.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.constant.StringEncoding;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.Fee;
import com.example.demo.mode.PayOrder;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.service.NotifyService;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.MD5;
import com.example.demo.util.UtilsConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 *   支付通知入口
 * @author a
 *
 */

@Controller
@RequestMapping("/paynotify")
@ResponseBody
public class PayNotifyController {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	@Autowired
	private NotifyService notifyService;
	
	@RequestMapping("")
	public Object notify(HttpServletRequest request) { 
		
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
		
		String paytype = "1";
		if("Alipay".equals(map2.get("r5_business").toString())){
			paytype = "2";
		}else if("B2C".equals(map2.get("r5_business").toString())){
			paytype = "4";
		}
		
		
		
		Map<String,Object> map =  TradeDB.getMerchantInfo(merchantID,paytype);
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
		
		if(Constant.payRetCode.equals(order.getPayRetCode())){
			logger.info("订单号：" + orderNumber + "已经成功支付");
			return RespCode.notifySuccess;
		}
		
		String retCode = map2.get("retCode");
		String orderStatus = map2.get("r8_orderStatus");
		
		logger.info("订单：" + orderNumber + "状态为 : retCode= " + retCode + " , orderStatus=" + orderStatus);
		
		if((Constant.payRetCode.equals(retCode)&&Constant.orderStatus.equals(orderStatus))||
				(Constant.T0RetCode.equals(retCode)&&Constant.orderStatus.equals(orderStatus))){
			logger.info("订单 ：" + orderNumber  + "支付成功");
			String retMsg = "支付成功";
			
			retCode = Constant.payRetCode;
			
			if(map2.containsKey("retMsg")){
				retMsg = map2.get("retMsg");
			}
			
			TabLoginuser loginUser = null;
			try {
				loginUser = LoginUserDB.getLoginuserInfo(order.getUserID());
			} catch (Exception e) {
				logger.info(e.getMessage()); 
				return RespCode.notifyfail;
			}
			
			Fee fee = notifyService.calProfit(order, loginUser);
			
			if(fee==null){
				logger.info("订单：" +  orderNumber + "计算手续费失败");
				return RespCode.notifyfail;
			}
			
			int updateRet = TradeDB.updatePayOrderPayRetCode(new Object[]{retCode ,retMsg ,fee.getMerchantFee() , fee.getMerchantprofit() , order.getID()});
			if(updateRet < 1){
				logger.info("订单号：" + orderNumber + "更新数据库失败"); 
				return RespCode.notifyfail;
			}
			
			// ID,UserID,TradeID,Fee,AgentID,AgentProfit,TwoAgentID,TwoAgentProfit,DistributeProfit,PlatformProfit
			TradeDB.saveProfit(new Object[]{
					UtilsConstant.getUUID(),loginUser.getID(), order.getID() ,fee.getMerchantFee(),fee.getAgentID(),fee.getAgentProfit(),
					fee.getTwoAgentID(),fee.getTwoAgentProfit(),
					fee.getDistributeProfit(),fee.getPlatformProfit(),fee.getPlatCostFee()
			});
			
			/**  计算三积分销各个商户的利润   **/
			List<Object[]> objs = notifyService.calDistributeProfit(fee, order, loginUser);
			
			logger.info("订单号：" + orderNumber + "三级分销的list长度:" + objs.size());
			
			if(objs.size()>0){
				/**  保存三级分销各个商户的利润  **/
				TradeDB.saveDistributeProfit(objs);
				/** 更新用户信息表中的 分润总额 **/
				List<Object[]> profitlist = new ArrayList<Object[]>();
				for (Object[] objects : objs) {
					logger.info("交易单号：" + order.getOrderNumber() + "  ====用户分润list中的值：" + Arrays.toString(objects));
					profitlist.add(new Object[]{ objects[2] , objects[2] , objects[1]});
				}
				LoginUserDB.merchantProfit(profitlist);
			} else {
				logger.info("订单号：" + orderNumber + ",没有产生分润");
			}
			
			EhcacheUtil ehcache = EhcacheUtil.getInstance();
			ehcache.clear(Constant.cacheName);
			 
		}
		return RespCode.notifySuccess;
	}
}


