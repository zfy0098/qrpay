package com.example.demo.service;

import java.util.Map; 
import java.util.TreeMap;

import com.example.demo.constant.Constant;
import com.example.demo.constant.RespCode;
import com.example.demo.constant.StringEncoding;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.OpenKuaiDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.*;

import net.sf.json.JSONObject;

/**
 *   开通无卡快捷
 * @author a
 *
 */
public class OpenKuaiService {


	LoggerTool logger = new LoggerTool(this.getClass());
	
	
	@SuppressWarnings("unchecked")
	public void openKuai(TabLoginuser user , RequestData reqData , ResponseData repData){
		
		logger.info("用户：" + user.getLoginID() + "请求开通无卡快捷支付请求 , 开通银行卡卡号：(密文)" + reqData.getBankCardNo());
		
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		
		/** 查询用户交易配置信息  **/
		Map<String,Object> userconfigmap = null;
		Object obj = ehcache.get(Constant.cacheName, user.getID() + "4userConfig");
		if(obj == null){
			logger.info("缓存读取用户支付配置信息失败，从数据中读取， 用户：" + user.getID() + " , 支付类型:" + 4);
			userconfigmap = TradeDB.getUserConfig(new Object[]{ user.getID() , "4"});
			if(userconfigmap==null||userconfigmap.isEmpty()){
				logger.info("用户：" + user.getID() + "支付类型：" + "4" + "系统为查到该类型配置信息" );
				repData.setRespCode(RespCode.TradeTypeConfigError[0]);
				repData.setRespDesc(RespCode.TradeTypeConfigError[1]); 
				return ;
			}
			ehcache.put(Constant.cacheName, user.getID() + "4" + "userConfig" , userconfigmap);
		}else{
			logger.info("用户：" + user.getID() + " , 支付类型:" + "4" + "缓存读取信息成功 继续操作");
			userconfigmap = (Map<String,Object>) obj;
			obj = null;
		}
		
		Map<String, Object> termKey = LoginUserDB.selectTermKey(user.getID());
		String initKey = LoadPro.loadProperties("config", "DBINDEX");
		
		
		Map<String,Object> merchantMap = TradeDB.getMerchantInfo(new Object[]{user.getID(),1});
		Map<String,Object> queryMap = new TreeMap<>();
		
		String bankCardno = "";
		
		try {
			String desckey = DESUtil.deskey(UtilsConstant.ObjToStr(termKey.get("MacKey")), initKey);
			bankCardno = DES3.decode(reqData.getBankCardNo(), desckey);
			logger.info("用户：" + user.getLoginID() + "请求开通无卡快捷支付请求 , 开通银行卡卡号：(原文)" + bankCardno);
			
			queryMap.put("accNo",  DESUtil.encode(Constant.REPORT_DES3_KEY , bankCardno));
			
		} catch (Exception e) {
			e.printStackTrace();
			repData.setRespCode(RespCode.SYSTEMError[0]);
			repData.setRespDesc(RespCode.SYSTEMError[1]);
			return ;
		}
		
		queryMap.put("merchantNo" ,merchantMap.get("MerchantID"));
		
		String querysign = MD5.sign(JSONObject.fromObject(queryMap).toString() + Constant.REPORT_QUERY_KEY , StringEncoding.UTF_8);
		queryMap.put("sign", querysign.toUpperCase());
		
		logger.info("银行卡卡号：" + bankCardno + "开通无卡快捷报文：" + queryMap.toString());
		
		try {
			logger.info("查询银行卡号:" + bankCardno + "是否开通无卡快捷");
			String queryContent = HttpClient.post(LoadPro.loadProperties("http", "OPENKUAIQUERY_URL"), queryMap, null);
			
			logger.info("查询银行卡号:" + bankCardno + "是否开通无卡快捷 响应报文：" + queryContent);
 			
			JSONObject queryjson = JSONObject.fromObject(queryContent);
			
			String respCode = queryjson.getString("respCode");
			
			//  查询交易配置信息
			
			Map<String,Object> tradeConfig = null;
			obj = ehcache.get(Constant.cacheName, "tradeConfig");
			if(obj == null){
				logger.info("缓存中获取交易配置信息失败,从数据库中查询");
				tradeConfig = TradeDB.getTradeConfig(); 
				ehcache.put(Constant.cacheName, "tradeConfig", tradeConfig); 
			}else{
				logger.info("缓存查询交易配置信息");
				tradeConfig = (Map<String,Object>) obj;
				obj = null;
			}
			
			String nowtime = DateUtil.getNowTime(DateUtil.HHmm);
			
			Integer nowHour = Integer.parseInt(nowtime.split("-")[0]);
			
			String KuaiT0Time = UtilsConstant.ObjToStr(tradeConfig.get("KuaiT0Time"));
			
			/** T0交易开始小时 **/
			Integer startHour = Integer.parseInt(KuaiT0Time.split("-")[0]);
			/** T0交易结束小时 **/
			Integer EndHour = Integer.parseInt(KuaiT0Time.split("-")[1]);
			
			logger.info("快捷T0交易时间段:" + KuaiT0Time + ", 当前时间:" + nowHour);
			
			if(Constant.payRetCode.equals(respCode)){
				String result = queryjson.getString("result");
				
				OpenKuaiDB.save(new Object[]{UtilsConstant.getUUID(),user.getID(),bankCardno,result , "00" ,result});
				
				if(!"3".equals(result)){
					logger.info("银行卡卡号：" + bankCardno + "需要调用开通请求");
					
					Map<String,Object> map  = new TreeMap<String,Object>();
					map.put("accNo", DESUtil.encode(Constant.REPORT_DES3_KEY , bankCardno));
					map.put("merchantNo", merchantMap.get("MerchantID"));
					map.put("orderNum", UtilsConstant.getOrderNumber());
					
					int status = 0;
					
					if(("0".equals(result)||"2".equals(result))&&nowHour>=startHour&&nowHour<EndHour){
						/**
						 *  当前属于T0时间段， 没有开通任何类型 或者只开通了T1 将请求开通T0
						 */
						map.put("encrypt", "T0");
					}else if(("1".equals(result)||"0".equals(result))&&(nowHour<startHour||nowHour+1 > EndHour)){
						/**
						 *  当前属于T1时间段， 没有开通任何类型 或者只开通了T0 将请求开通T1
						 */
//						Map<String,Object> map  = new TreeMap<String,Object>();
//						map.put("accNo", DESUtil.encode(Constant.REPORT_DES3_KEY , bankCardno));
//						map.put("merchantNo", merchantMap.get("MerchantID"));
//						map.put("orderNum", UtilsConstant.getOrderNumber());
						map.put("encrypt", "T1");
						status = 1;
						
//						logger.info(JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY); 
//						
//						String sign = MD5.sign(JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY , StringEncoding.UTF_8);
//						map.put("sign", sign.toUpperCase());
//						String content = HttpClient.post(LoadPro.loadProperties("http", "OPENKUAI_URL"), map, null);
//						logger.info("银行卡卡号：" + bankCardno + "开通快捷响应:" + content);
//						
//						if(content.length() > 100){
//							repData.setRespCode(RespCode.SUCCESS[0]);
//							repData.setRespDesc(content);
//							repData.setTerminalInfo("当前开通快捷支付T1到账功能");
//						}else{
//							JSONObject js = JSONObject.fromObject(content);
//							if("2021".equals(js.getString("respCode"))){
//								repData.setRespCode("01");
//							}else{
//								repData.setRespCode(js.getString("respCode"));
//							}
//							repData.setRespDesc(js.getString("respMsg")); 
//						}
					}else{
						logger.info("银行卡卡号：" + reqData.getBankCardNo() + " 已经开通无卡快捷功能");
						repData.setRespCode("01");
						repData.setRespDesc("该卡片已经开通过快捷支付功能");
						return ;
					}
					
					logger.info(JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY); 
					
					String sign = MD5.sign(JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY , StringEncoding.UTF_8);
					map.put("sign", sign.toUpperCase());
					String content = HttpClient.post(LoadPro.loadProperties("http", "OPENKUAI_URL"), map, null);
					logger.info("银行卡卡号：" + reqData.getBankCardNo() + "开通快捷响应:" + content);
					
					if(content.length() > 100){
						repData.setRespCode(RespCode.SUCCESS[0]);
						repData.setRespDesc(content);
						if(status == 0){
							repData.setTerminalInfo("当前开通快捷支付T0到账功能");
						}else{
							repData.setTerminalInfo("当前开通快捷支付T1到账功能"); 
						}
						
						
					}else{
						JSONObject js = JSONObject.fromObject(content);
						if("2021".equals(js.getString("respCode"))){
							repData.setRespCode("01");
						}else{
							repData.setRespCode(js.getString("respCode"));
						}
						repData.setRespDesc(js.getString("respMsg")); 
					}
					
				}else{
					logger.info("银行卡卡号：" + reqData.getBankCardNo() + " 已经开通无卡快捷功能");
					repData.setRespCode("01");
					repData.setRespDesc("该卡片已经开通过快捷支付功能");
				}
			} else {
				repData.setRespCode(queryjson.getString("respCode"));
				repData.setRespDesc(queryjson.getString("respMsg"));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			repData.setRespCode(RespCode.SYSTEMError[0]);
			repData.setRespDesc(RespCode.SYSTEMError[1]);
			return ;
		}
	}
}
