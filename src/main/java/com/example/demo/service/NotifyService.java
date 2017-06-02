package com.example.demo.service;

import com.example.demo.constant.Constant;
import com.example.demo.db.AgentDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.Fee;
import com.example.demo.mode.PayOrder;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.AmountUtil;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.PushUtils;
import com.example.demo.util.UtilsConstant;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@Service
public class NotifyService {
	
	LoggerTool logger = new LoggerTool(this.getClass());
	
	/**
	 *   计算手续费
	 * @param order
	 * @param loginUser
	 * @return
	 */
	public Fee calProfit(PayOrder order , TabLoginuser loginUser ){
		
		logger.info("计算订单：" + order.getOrderNumber() + "手续费");
		
		Fee fee = new Fee();
		String tradeCode = order.getTradeCode();
		// 交易费率
		String feeRate = "0";
		// 结算费率
		String SettlementRate = "0";
		// t0 附加费用
		int T0additional = 0;
		
		/** 用户费率配置信息 **/
		Map<String,Object> userConfig = TradeDB.getUserConfig(new Object[]{order.getUserID() ,  order.getPayChannel()});
		
		/** 代理商信息  **/
		Map<String,Object> agentInfo = AgentDB.agentInfo(new Object[]{loginUser.getAgentID()});
		/**  代理商配置信息  **/
		Map<String,Object> agentConfig = AgentDB.agentConfig(new Object[]{agentInfo.get("ID") , order.getPayChannel()}); 
		
		if(agentConfig==null||agentConfig.isEmpty()){
			logger.info("代理商：" + agentInfo.get("ID") + "没有配置支付类型：" +  order.getPayChannel()); 
			return null;
		}
		
		
		/**  商户下放费率   ,   代理商签约成本 ,  渠道成本  **/
		String merchantRate = "0", agentRate = "0" ,  channelRate ="0";
		
		/** 交易类型为T0 **/
		if(tradeCode.equals(Constant.T0)){
			
			logger.info("订单:" + order.getOrderNumber() + "为T0交易"); 
			
			//  交易费率  T0SaleRate,T0SettlementRate 
			feeRate =  UtilsConstant.ObjToStr(userConfig.get("T0SaleRate"));
			//  结算费率                                                                                                    
			SettlementRate = UtilsConstant.ObjToStr(userConfig.get("T0SettlementRate"));
			//  T0商户下放
			merchantRate = UtilsConstant.ObjToStr(agentConfig.get("T0MerchantRate"));
			//  T0代理商成本
			agentRate = UtilsConstant.ObjToStr(agentConfig.get("T0AgentRate"));
			//  T0渠道成本
			channelRate = UtilsConstant.ObjToStr(agentConfig.get("T0ChannelRate"));
			
			//  T0附加手续费
			T0additional = AgentDB.T0additional();
			
			logger.info(order.getOrderNumber() + "交易费率为：" + feeRate + ",结算费率为：" + SettlementRate + ",T0商户下放：" + merchantRate  + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
			
		}else{
			
			logger.info("订单:" + order.getOrderNumber() + "为T1交易"); 
			
			/** 交易类型为T1 **/
			// 交易费率
			feeRate = UtilsConstant.ObjToStr(userConfig.get("T1SaleRate"));
			// 结算费率
			SettlementRate = UtilsConstant.ObjToStr(userConfig.get("T1SettlementRate"));
			//  商户下放费率
			merchantRate = UtilsConstant.ObjToStr(agentConfig.get("MerchantRate"));
			// 代理商成本费率
			agentRate = UtilsConstant.ObjToStr(agentConfig.get("AgentRate"));
			//  渠道成本
			channelRate = UtilsConstant.ObjToStr(agentConfig.get("ChannelRate"));
			
			logger.info(order.getOrderNumber()  + "交易费率为：" + feeRate + ",结算费率为：" + SettlementRate + ",商户下放：" + merchantRate  + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
			
		}
		
		/** 商户手续费 **/
		fee.setMerchantFee(AgentDB.makeFeeFurther(order.getAmount(), Double.valueOf(feeRate)  , 0) + T0additional);
		/** 商户自己的分润  **/
		fee.setMerchantprofit(AgentDB.makeFeeAbandon(order.getAmount(), AmountUtil.sub(feeRate, SettlementRate), 0));
		
		/** 三级分销总金额  **/
		int distributeProfit = AgentDB.makeFeeAbandon(order.getAmount(),AmountUtil.sub(SettlementRate, merchantRate), 0);
		fee.setDistributeProfit(distributeProfit);
		
		/** 二级代理商商户交易 **/
		if(agentInfo.get("ParentAgentID")!=null&&!agentInfo.get("ParentAgentID").equals(agentInfo.get("ID"))){
			
			logger.info(order.getOrderNumber() + "订单的商户的代理商为二级代理商");
			
			//  获得父级代理商ID
			Map<String,Object> agentParent = AgentDB.agentInfo(new Object[]{agentInfo.get("ParentAgentID")});
			//  获取父类代理商配置信息
			Map<String,Object> agentParentConfig = AgentDB.agentConfig(new Object[]{agentParent.get("ID") , order.getPayChannel()});
			//  代理商ID
			fee.setAgentID(agentParent.get("ID").toString());
			//  二级代理商ID
			fee.setTwoAgentID(agentInfo.get("ID").toString());
			
			String parentAgentRate = "0";
			
			if(tradeCode.equals(Constant.T0)){
				parentAgentRate = agentParentConfig.get("T0AgentRate").toString();
			}else{
				parentAgentRate = agentParentConfig.get("AgentRate").toString();
			}
			//  代理商分润
			fee.setAgentProfit(AgentDB.makeFeeAbandon(order.getAmount(), AmountUtil.sub(agentRate ,parentAgentRate ) ,0));
			//  设置二级代理商分润
			fee.setTwoAgentProfit(AgentDB.makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,agentRate), 0));
			agentRate = parentAgentRate;
		}else{
			logger.info(order.getOrderNumber() + "订单商户的代理商为一级代理商");
			// 代理商ID
			fee.setAgentID(agentInfo.get("ID").toString());
			//  代理商分润
			fee.setAgentProfit(AgentDB.makeFeeAbandon(order.getAmount(), AmountUtil.sub(merchantRate ,agentRate) ,0));
		}
		//计算平台手续费
		fee.setPlatCostFee(AgentDB.makeFeeFurther(order.getAmount(), Double.valueOf(channelRate),0));
		// 平台收益
		fee.setPlatformProfit(AgentDB.makeFeeFurther(order.getAmount(),  AmountUtil.sub(agentRate, channelRate),0));
		
		return fee;
	}
	
	
	
	/**
	 *     计算三级分销
	 * @param fee
	 * @param loginUser
	 * @return
	 */
	public List<Object[]> calDistributeProfit( Fee fee , PayOrder order ,TabLoginuser loginUser){
		logger.info("开始计算订单编号为：" + order.getOrderNumber() + "的三级分销分润");
		
		List<Object[]> list = new ArrayList<Object[]>();
		
		String one = loginUser.getOneLevel();
		
		String two = loginUser.getTwoLevel();
		
		String three = loginUser.getThreeLevel();
		
		Map<String,Object> agentInfo = AgentDB.agentInfo(new Object[]{loginUser.getAgentID()});
 		
		int total = fee.getDistributeProfit();

		logger.info("计算订单编号为：" + order.getOrderNumber() + "的三级分销分润总金额：" + total);
		Object[] obj = null;
		if(total > 0){
			/** 上级用户 **/
			if(!UtilsConstant.strIsEmpty(three)){
				int threeProfit = total*Integer.parseInt(UtilsConstant.ObjToStr(agentInfo.get("ThreeLeveFee")))/10;
				if(threeProfit > 0){
					String tonken = AgentDB.getDeviceToken(three);
					logger.info("============================订单编号:" + order.getOrderNumber() + "上级用户Token:" + tonken + "开始发送push");
					if(!UtilsConstant.strIsEmpty(tonken)){
						String content = "您的下级商户为您贡献" + new BigDecimal(threeProfit).divide(new BigDecimal(100),2,RoundingMode.DOWN) + "元分润，请查看";
						PushUtils.IOSPush(content, tonken);
						PushUtils.AndroidPush("分润通知", content, tonken); 
					}
					
					obj = new Object[]{UtilsConstant.getUUID(),three,threeProfit ,order.getTradeDate() + order.getTradeTime(),order.getID()};
					list.add(obj);
				}
				logger.info("订单编号:" + order.getOrderNumber() + " , 上级用户" + three + "获得分润:" + threeProfit);
			}
			/** 第二级用户  **/
			if(!UtilsConstant.strIsEmpty(two)){
				int twoProfit = total*Integer.parseInt(UtilsConstant.ObjToStr(agentInfo.get("TwoLeveFee")))/10;
				if(twoProfit > 0){
					String tonken = AgentDB.getDeviceToken(two);
					logger.info("============================订单编号:" + order.getOrderNumber() + "第二级用户Token:" + tonken + "开始发送push");
					if(!UtilsConstant.strIsEmpty(tonken)){
						String content = "您的下级商户为您贡献" + new BigDecimal(twoProfit).divide(new BigDecimal(100),2,RoundingMode.DOWN) + "元分润，请查看";
						PushUtils.IOSPush(content, tonken);
						PushUtils.AndroidPush("分润通知", content, tonken); 
					}
					
					obj = new Object[]{UtilsConstant.getUUID(),two ,twoProfit ,order.getTradeDate() + order.getTradeTime(),order.getID()};
					list.add(obj);
				}
				logger.info("订单编号:" + order.getOrderNumber() + " , 第二级用户" + two + "获得分润:" + twoProfit);
			}
			
			/** 第三级用户  **/
			if(!UtilsConstant.strIsEmpty(one)){
				int oneProfit = total*Integer.parseInt(UtilsConstant.ObjToStr(agentInfo.get("OneLeveFee")))/10;
				if(oneProfit > 0){
					String tonken = AgentDB.getDeviceToken(one);
					logger.info("============================订单编号:" + order.getOrderNumber() + "第三级用户Token:" + tonken + "开始发送push");
					if(!UtilsConstant.strIsEmpty(tonken)){
						String content = "您的下级商户为您贡献" + new BigDecimal(oneProfit).divide(new BigDecimal(100),2,RoundingMode.DOWN) + "元分润，请查看";
						PushUtils.IOSPush(content, tonken);
						PushUtils.AndroidPush("分润通知", content, tonken); 
					}
					
					obj = new Object[]{UtilsConstant.getUUID(),one ,oneProfit , order.getTradeDate() + order.getTradeTime(),order.getID()};
					list.add(obj);
				}
				logger.info("订单编号:" + order.getOrderNumber() + " , 第三级用户" + one + "获得分润:" + oneProfit);
			}
		}
		
		if(fee.getMerchantprofit() > 0){
			/** 保存商户自己的分润 **/
			String tonken = AgentDB.getDeviceToken(loginUser.getID());
			logger.info("============================订单编号:" + order.getOrderNumber() + "商户自己的token" + tonken + "开始发送push"); 
			if(!UtilsConstant.strIsEmpty(tonken)){
				String content = "本次交易您自己获取的分润" + new BigDecimal(fee.getMerchantprofit()).divide(new BigDecimal(100),2,RoundingMode.DOWN)   + "元，请查看";
				PushUtils.IOSPush(content, tonken);
				PushUtils.AndroidPush("分润通知", content, tonken); 
			}
			logger.info("订单编号:" + order.getOrderNumber() + " , 商户自己获得分润:" + fee.getMerchantprofit());
			obj = new Object[]{UtilsConstant.getUUID(),loginUser.getID(),fee.getMerchantprofit(), order.getTradeDate() + order.getTradeTime(),order.getID()};
			list.add(obj);
		}
		return list;
	}
	
	
	
	/**
	 *   计算固定码手续费
	 * @param order
	 * @param user
	 * @param qrcode
	 * @return
	 */
	public Fee YMFcalProfit(PayOrder order , TabLoginuser user , Map<String,Object> qrcode){
		
		logger.info("计算订单" + order.getOrderNumber() + "固定码手续费");
		
		Fee fee = new Fee();
		
		/** 到账类型 **/
		String tradeCode = order.getTradeCode();
		
		/** 固定码的手续费 **/
		String rate = UtilsConstant.ObjToStr(qrcode.get("Rate"));
		
		/** t0 附加费用 **/
		int T0additional = 0;
		
		/** 代理商信息 **/
		Map<String,Object> agentInfo = AgentDB.agentInfo(new Object[]{user.getAgentID()});
		/** 代理商费率配置信息 **/
		Map<String,Object> agentConfig = AgentDB.agentConfig(new Object[]{new Object[]{agentInfo.get("ID") , order.getPayChannel()}});
		
		
		/**   代理商签约成本 ,  渠道成本  **/
		String agentRate = "0" , channelRate ="0";
		
		/** 交易类型为T0 **/
		if(tradeCode.equals(Constant.T0)){
			
			logger.info("订单:" + order.getOrderNumber() + "为T0交易"); 
			
			//  T0代理商成本
			agentRate = UtilsConstant.ObjToStr(agentConfig.get("T0AgentRate"));
			//  T0渠道成本
			channelRate = UtilsConstant.ObjToStr(agentConfig.get("T0ChannelRate"));
			
			//  T0附加手续费
			T0additional = AgentDB.T0additional();
			
			logger.info(order.getOrderNumber() + "交易费率为：" + rate + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
			
		}else{
			
			logger.info("订单:" + order.getOrderNumber() + "为T1交易"); 
			
			/** 交易类型为T1 **/
			// 代理商成本费率
			agentRate = UtilsConstant.ObjToStr(agentConfig.get("AgentRate"));
			//  渠道成本
			channelRate = UtilsConstant.ObjToStr(agentConfig.get("ChannelRate"));
			
			logger.info(order.getOrderNumber()  + "交易费率为：" + rate + ",代理商成本：" + agentRate + ",T0渠道成本:" + channelRate + ",T0附加费用：" + T0additional);
		}
		
		/** 商户手续费 **/
		fee.setMerchantFee(AgentDB.makeFeeFurther(order.getAmount(), Double.valueOf(rate) , 0) + T0additional);
		
		/** 二级代理商商户交易 **/
		if(agentInfo.get("ParentAgentID")!=null&&!agentInfo.get("ParentAgentID").equals(agentInfo.get("ID"))){
			
			logger.info(order.getOrderNumber() + "订单的商户的代理商为二级代理商");
			
			//  获得父级代理商ID
			Map<String,Object> agentParent = AgentDB.agentInfo(new Object[]{agentInfo.get("ParentAgentID")});
			//  获取父类代理商配置信息
			Map<String,Object> agentParentConfig = AgentDB.agentConfig(new Object[]{agentParent.get("ID") , order.getPayChannel()});
			//  代理商ID
			fee.setAgentID(agentParent.get("ID").toString());
			//  二级代理商ID
			fee.setTwoAgentID(agentInfo.get("ID").toString());
			
			String parentAgentRate = "0";
			
			if(tradeCode.equals(Constant.T0)){
				parentAgentRate = agentParentConfig.get("T0AgentRate").toString();
			}else{
				parentAgentRate = agentParentConfig.get("AgentRate").toString();
			}
			//  代理商分润
			fee.setAgentProfit(AgentDB.makeFeeAbandon(order.getAmount(), AmountUtil.sub(agentRate ,parentAgentRate ) ,0));
			//  设置二级代理商分润
			fee.setTwoAgentProfit(AgentDB.makeFeeAbandon(order.getAmount(), AmountUtil.sub(rate ,agentRate), 0));
			
			agentRate = parentAgentRate;
		}else{
			logger.info(order.getOrderNumber() + "订单商户的代理商为一级代理商");
			// 代理商ID
			fee.setAgentID(agentInfo.get("ID").toString());
			//  代理商分润
			fee.setAgentProfit(AgentDB.makeFeeAbandon(order.getAmount(), AmountUtil.sub(rate ,agentRate) ,0));
		} 
		//计算平台手续费
		fee.setPlatCostFee(AgentDB.makeFeeFurther(order.getAmount(), Double.valueOf(channelRate),0));
		// 平台收益
		fee.setPlatformProfit(AgentDB.makeFeeFurther(order.getAmount(),  AmountUtil.sub(agentRate, channelRate),0));
		
		return fee;
	} 
}
