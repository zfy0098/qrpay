package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.example.demo.constant.Constant;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.service.H5PerfectInfoService;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.UtilsConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import net.sf.json.JSONObject;

@Controller
public class MerchantAction {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	
	@Autowired
	private H5PerfectInfoService h5perfectInfoService;
	
	@RequestMapping(value = "/in")
	@ResponseBody
	public Object merchantIn(HttpServletRequest request ){ 
		JSONObject js = new JSONObject();
		try {
			String reqContent = request.getParameter("data") ;
			
			System.out.println(reqContent); 
			
			JSONObject json = JSONObject.fromObject(reqContent);
			// 用户登录的手机号
			String loginID = json.getString("loginID");
			
			EhcacheUtil ehcache = EhcacheUtil.getInstance();
			
			TabLoginuser user = null;
			
			Object obj = ehcache.get(Constant.cacheName,loginID + "UserInfo" );
			if(obj == null){
				logger.info("查询数据库");
				user = h5perfectInfoService.loginuser(loginID);
				if(user == null){
					logger.info("未查到用户 " + loginID +"信息");
					return "01";
				}
				ehcache.put(Constant.cacheName, loginID + "UserInfo" , user);
			}else{
				logger.info("查询缓存");
				user = (TabLoginuser) obj;
			}
			
			//  商户名称
			String merchantName = json.getString("merchantName"); 
			//  签购单显示名称
			String merchantBillName = json.getString("merchantName");
			//  商户联系人名称
			String merchantPersonName = json.getString("merchantPersonName");
			//  商户联系邮箱
			String merchantPersonEmail = json.getString("merchantPersonEmail");
			// 营业执照号
			String businessLicense = UtilsConstant.RandCode();
			json.put("businessLicense", businessLicense);
			// 法人
			String legalPersonName = json.getString("merchantPersonName");
			// 法人身份证
			String legalPersonID = json.getString("legalPersonID");
			// 安装省份
			String installProvince = json.getString("installProvince");
			//  安装城市
			String installCity = json.getString("installCity");
			// 安装区
			String installCounty = json.getString("installCounty");
			// 经营地址
			String operateAddress = json.getString("operateAddress");
			// 商户类型
			String merchantType = "PERSON";
			
			
			// 开户人名称
			String accountName = json.getString("merchantPersonName");
			// 开户上账号
			String accountNo = json.getString("accountNo");
			// 开户银行
			String bankName = json.getString("bankName");
			// 支行名称
			String bankBranch = json.getString("bankBranch");

			//  结算人信用卡
			String creditCardNo = json.getString("creditCardNo");
			// 结算账户性质  对公或对私
			String bankType = "TOPRIVATE";
			// 开户行省份
			String bankProv = json.getString("bankProv");
			// 开户行城市
			String bankCity = json.getString("bankCity");
			
			
			int x = h5perfectInfoService.updateUserInfo(user.getID(), json);
			
			ehcache.clear(Constant.cacheName);
			
			logger.info("用户" + loginID + "完善资料,受影响行数：" + x + " , 请求数据" + json.toString());
			
			if(x > 0){
				js.put("respCode", "00");
				js.put("respMsg", "提交成功，等待进一步审核");
			}else{
				js.put("respCode", "01");
				js.put("respMsg", "提交异常");
			}
		} catch (Exception e) {
			e.printStackTrace();
			js.put("respCode", "01");
			js.put("respMsg", "提交异常");
		}
		
		
//		int alipaylength = Constant.alipayMCCType.length;
//		
//		Random random = new Random(alipaylength-1);
//		int index = random.nextInt(alipaylength-1);
//		String alipaymcccNumber = Constant.alipayMCCType[index];
//		
//		int wxlength = Constant.wxMCCType.length;
//		random = new Random(wxlength-1);
//		index = random.nextInt(wxlength-1);
//		Integer wxmcccNumber = Constant.wxMCCType[index];
//		
//		
//		Map<String,Object> wxmap = h5perfectInfoService.getUserConfig(new Object[]{user.getID(),1});
//		Map<String,Object> alipaymap = h5perfectInfoService.getUserConfig(new Object[]{user.getID(),2});
//		
//		Map<String,Object> map = new TreeMap<String, Object>();
//		
//		map.put("channelName", Constant.REPORT_CHANNELNAME);
//		map.put("channelNo", Constant.REPORT_CHANNELNO);
//		map.put("merchantName", merchantName);
//		map.put("merchantBillName", merchantBillName);
//		map.put("installProvince", installProvince);
//		map.put("installCity",  installCity);
//		map.put("installCounty", installCounty);
//		map.put("operateAddress", operateAddress);
//		map.put("merchantType", merchantType);
//		map.put("businessLicense", businessLicense);
//		map.put("legalPersonName", legalPersonName);
//		map.put("legalPersonID", legalPersonID);
//		map.put("merchantPersonName", merchantPersonName);
//		map.put("merchantPersonPhone",  loginID);
//		
//		map.put("wxType", wxmcccNumber);
//		map.put("wxT1Fee", wxmap.get("T1SaleRate").toString());
//		map.put("wxT0Fee",  wxmap.get("T0SaleRate").toString());
//		
//		map.put("alipayType", alipaymcccNumber);
//		map.put("alipayT1Fee", alipaymap.get("T1SaleRate").toString());
//		map.put("alipayT0Fee", alipaymap.get("T0SaleRate").toString());
//		
//		map.put("bankType", bankType);
//		map.put("accountName", accountName);
//		map.put("accountNo", DESUtil.encode(Constant.REPORT_DES3_KEY,accountNo));
//		map.put("bankName", bankName);
//		map.put("bankProv", bankProv);
//		map.put("bankCity", bankCity);
//		map.put("bankBranch", bankBranch);
//		map.put("bankCode", bankCode);
//
//		
//		logger.info("需要签名的的数据：" + JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY); 
//		
//		String sign = MD5.sign( JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY , StringEncoding.UTF_8);
//		map.put("sign", sign.toUpperCase());
//		
//		
//		logger.info("用户" + loginID + "入网请求报文:" + map.toString());
//		
//		
//		JSONObject js = new JSONObject();
//		try {
//			String content = HttpClient.post(Constant.REPORT_URL, map, null);
//			
//			logger.info("入网响应报文:" + content);
//			JSONObject respJS = JSONObject.fromObject(content);
//			
//			String respCode = respJS.getString("respCode");
//			
//			if(Constant.payRetCode.equals(respCode)){
//				
//				String merchantNo = respJS.getString("merchantNo");// 商户号
//				String signKey = respJS.getString("signKey");		//  微信签名秘钥
//				String desKey = respJS.getString("desKey");			//  微信des秘钥
//				String queryKey = respJS.getString("queryKey");		//  查询秘钥
//				
//				String AlipaySignKey = respJS.getString("AlipaySignKey");	// 支付宝签名秘钥
//				String AlipaydesKey = respJS.getString("AlipaydesKey");		// 支付des秘钥
//				//MerchantID,MerchantName,SignKey,DESKey,QueryKey,UserID,PayType
//				
//				/*
//				 *  保存商户秘钥等信息 
//				 *
//				 */
//				List<Object[]> list = new ArrayList<Object[]>();
//				Object[] objs = new Object[]{merchantNo,merchantName,signKey,desKey,queryKey,user.getID(),Constant.PayChannelWXScancode};
//				list.add(objs);
//				objs = new Object[]{merchantNo,merchantName,AlipaySignKey,AlipaydesKey,queryKey,user.getID(),Constant.payChannelAliScancode};
//				list.add(objs);
//				h5perfectInfoService.saveMerchantInfo(list);
//				
//				h5perfectInfoService.updateUserBankStatus(new Object[]{1, 1 , loginID});
//				
//				js.put("respCode", "00");
//				js.put("respMsg", "提交成功");
//			}else{
//				h5perfectInfoService.updateUserBankStatus(new Object[]{2 , 2 , loginID});
//				
//				js.put("respCode", "01");
//				js.put("respMsg", "信息已完善，等待进一步审核");
//			}
//		} catch (Exception e) {
//			logger.error(loginID + "入网异常：" + e.getMessage());
//			js.put("respCode", "01");
//			js.put("respMsg", e.getMessage());
//		}
		
		
//		Map<String,Object> wxmap = h5perfectInfoService.getUserConfig(new Object[]{user.getID(),1});
//		Map<String,Object> alipaymap = h5perfectInfoService.getUserConfig(new Object[]{user.getID(),2});
//		
//		Map<String,Object> map = new TreeMap<String, Object>();
//		
//		map.put("channelName", Constant.REPORT_CHANNELNAME);
//		map.put("channelNo", Constant.REPORT_CHANNELNO);
//		map.put("merchantName", merchantName);
//		map.put("merchantBillName", merchantBillName);
//		map.put("installProvince", installProvince);
//		map.put("installCity",  installCity);
//		map.put("installCounty", installCounty);
//		map.put("operateAddress", operateAddress);
//		map.put("merchantType", merchantType);
//		map.put("businessLicense", businessLicense);
//		map.put("legalPersonName", legalPersonName);
//		map.put("legalPersonID", legalPersonID);
//		map.put("merchantPersonName", merchantPersonName);
//		map.put("merchantPersonPhone",  loginID);
//		
//		map.put("wxType", wxmcccNumber);
//		map.put("wxT1Fee", wxmap.get("T1SaleRate").toString());
//		map.put("wxT0Fee",  wxmap.get("T0SaleRate").toString());
//		
//		map.put("alipayType", alipaymcccNumber);
//		map.put("alipayT1Fee", alipaymap.get("T1SaleRate").toString());
//		map.put("alipayT0Fee", alipaymap.get("T0SaleRate").toString());
//		
//		map.put("bankType", bankType);
//		map.put("accountName", accountName);
//		map.put("accountNo", DESUtil.encode(Constant.REPORT_DES3_KEY,accountNo));
//		map.put("bankName", bankName);
//		map.put("bankProv", bankProv);
//		map.put("bankCity", bankCity);
//		map.put("bankBranch", bankBranch);
//		map.put("bankCode", bankCode);
//
//		
//		logger.info("需要签名的的数据：" + JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY); 
//		
//		String sign = MD5.sign( JSONObject.fromObject(map).toString() + Constant.REPORT_SIGN_KEY , StringEncoding.UTF_8);
//		map.put("sign", sign.toUpperCase());
//		
//		
//		logger.info("用户" + loginID + "入网请求报文:" + map.toString());
//		
//		
//		JSONObject js = new JSONObject();
//		try {
//			String content = HttpClient.post(Constant.REPORT_URL, map, null);
//			
//			logger.info("入网响应报文:" + content);
//			JSONObject respJS = JSONObject.fromObject(content);
//			
//			String respCode = respJS.getString("respCode");
//			
//			if(Constant.payRetCode.equals(respCode)){
//				
//				String merchantNo = respJS.getString("merchantNo");// 商户号
//				String signKey = respJS.getString("signKey");		//  微信签名秘钥
//				String desKey = respJS.getString("desKey");			//  微信des秘钥
//				String queryKey = respJS.getString("queryKey");		//  查询秘钥
//				
//				String AlipaySignKey = respJS.getString("AlipaySignKey");	// 支付宝签名秘钥
//				String AlipaydesKey = respJS.getString("AlipaydesKey");		// 支付des秘钥
//				//MerchantID,MerchantName,SignKey,DESKey,QueryKey,UserID,PayType
//				
//				/*
//				 *  保存商户秘钥等信息 
//				 *
//				 */
//				List<Object[]> list = new ArrayList<Object[]>();
//				Object[] objs = new Object[]{merchantNo,merchantName,signKey,desKey,queryKey,user.getID(),Constant.PayChannelWXScancode};
//				list.add(objs);
//				objs = new Object[]{merchantNo,merchantName,AlipaySignKey,AlipaydesKey,queryKey,user.getID(),Constant.payChannelAliScancode};
//				list.add(objs);
//				h5perfectInfoService.saveMerchantInfo(list);
//				
//				h5perfectInfoService.updateUserBankStatus(new Object[]{1, loginID});
//				
//				js.put("respCode", "00");
//				js.put("respMsg", "提交成功");
//			}else{
//				h5perfectInfoService.updateUserBankStatus(new Object[]{0 , loginID});
//				
//				js.put("respCode", "01");
//				js.put("respMsg", "提交失败");
//			}
//		} catch (Exception e) {
//			logger.error(loginID + "入网异常：" + e.getMessage());
//			js.put("respCode", "01");
//			js.put("respMsg", e.getMessage());
//		}
//		
		return js;
	}
	
	@RequestMapping(value = "/merchanttypelist", method = RequestMethod.POST )
	@ResponseBody
	public Object merchantType(){
		List<Map<String,Object>> list = h5perfectInfoService.merchantTypeList();
		JSONObject json = new JSONObject();
		json.put("list", list);
		return json;
	}
	
	
	@RequestMapping(value = "/getmerchantinfo" , method = RequestMethod.POST )
	@ResponseBody
	public Object getmerchantinfo(HttpServletRequest request){
		String loginID = request.getParameter("loginID");
		TabLoginuser user = h5perfectInfoService.getMerchantInfoByLoginID(loginID);
		try {
			JSONObject json = JSONObject.fromObject(user);
			return json;
		} catch (Exception e) {
			return null;
		}
	}
	
	
	@RequestMapping(value = "getuserbankcard" , method = RequestMethod.POST)
	@ResponseBody
	public Object getUserBankCard(HttpServletRequest request){
		String loginID = request.getParameter("loginID");
		TabLoginuser user = h5perfectInfoService.getMerchantInfoByLoginID(loginID);
		Map<String,Object> map = h5perfectInfoService.getUserBankCard(user.getID());
		try {
			return JSONObject.fromObject(map);
		} catch (Exception e) {
			return null;
		}
	}
}
