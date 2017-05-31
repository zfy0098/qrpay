package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo.constant.Constant;
import com.example.demo.db.LoginUserDB;
import com.example.demo.db.TradeDB;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.UtilsConstant;
import org.springframework.stereotype.Service;


import net.sf.json.JSONObject;

@Service
public class H5PerfectInfoService {

	
	/**
	 *   更新用户信息
	 * @param json
	 * @return
	 */
	public int updateUserInfo(String userID , JSONObject json){
		
		// 用户登录的手机号
		String loginID = json.getString("loginID");
		//  商户名称
		String merchantName = json.getString("merchantName"); 
		//  签购单显示名称
		String merchantBillName = json.getString("merchantName");
		//  商户联系人名称
		String merchantPersonName = json.getString("merchantPersonName");
		//  商户联系邮箱
		String merchantPersonEmail = json.getString("merchantPersonEmail");
		// 营业执照号
		String businessLicense = json.getString("businessLicense");
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

		LoginUserDB.h5updateUserInfo(new Object[]{merchantType,legalPersonName,legalPersonID,merchantName,installProvince,
				installCity,installCounty,businessLicense,operateAddress,merchantPersonEmail,merchantBillName,merchantPersonName,loginID});
		
		
		/**********  更新结算信息   *************************/
		
		// 开户人名称
		String accountName = json.getString("merchantPersonName");
		// 开户上账号
		String accountNo = json.getString("accountNo");
		// 开户银行
		String bankName = json.getString("bankName");
		// 支行名称
		String bankBranch = json.getString("bankBranch");
		// 开户行省份
		String bankProv = json.getString("bankProv");
		// 开户行城市
		String bankCity = json.getString("bankCity");
		//  银联号
		
		String bankCode = TradeDB.getBankNoByBankCardNo(accountNo);
		
		//  结算人信用卡
		String creditCardNo = json.getString("creditCardNo");
		// 结算账户性质  对公或对私
		String bankType = "INSTITUTION";
		
		List<Map<String,Object>> payChannelList = TradeDB.getPayChannel();
		
		List<Object[]> userConfig = new ArrayList<Object[]>();
		for (Map<String, Object> map : payChannelList) {
			Object[] obj = new Object[]{UtilsConstant.getUUID(),userID,map.get("ID"),0,0, Constant.FeeRate ,Constant.FeeRate ,Constant.SettlementRate, Constant.SettlementRate};
			userConfig.add(obj);
		}
		TradeDB.saveUserConfig(userConfig);
		
		return LoginUserDB.saveOrUpBankInfo(new Object[]{UtilsConstant.getUUID(),userID,accountName,accountNo,bankBranch,bankProv,bankCity,bankCode,bankName,creditCardNo,bankType
				,accountName,accountNo,bankBranch,bankProv,bankCity,bankCode,bankName,creditCardNo,bankType});
	}
	
	
	
	public List<Map<String,Object>> merchantTypeList(){
		return LoginUserDB.merchantTypeList();
	}
	
	
	public TabLoginuser getMerchantInfoByLoginID(String loginID){
		return LoginUserDB.loginuser(loginID);
	}
	
	
	public Map<String,Object> getUserBankCard(String userID){
		return LoginUserDB.getUserBankCard(userID);
	}
	
	
	public Map<String,Object> getUserConfig(Object[] obj){
		return TradeDB.getUserConfig(obj);
	}
	
	
	public int[] saveMerchantInfo(List<Object[]> list){
		return TradeDB.saveMerchantInfo(list);
	}
	
	
	public TabLoginuser loginuser(String loginID){
		return LoginUserDB.loginuser(loginID);
	}
	
	public int updateUserBankStatus(Object[] obj){
		return LoginUserDB.updateUserBankStatus(obj);
	}
	
	
}
