package com.example.demo.mode;

import java.io.Serializable;


/**
 *   请求实体类
 * @author a
 *
 */
public class RequestData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1010811141837310709L;

	/** 登录账号 **/
	private String loginID;
	
	/** 登录密码 **/
	private String loginPwd;
	
	/** 新密码 **/
	private String newLoginPwd;

	/**  请求类型 **/
	private String txndir;
	
	/** 发起交易时间 **/
	private String sendTime;
	
	/**  交易金额  **/
	private String amount;
	
	/**  终端流水号 **/
	private String sendSeqId;
	
	/** 支付类型 微信或支付宝 **/
	private String payChannel;
	
	/** 终端版本号 **/
	private String version;
	
	private String agencyNumber;
	/**
	 * 信用卡类别（区分银行）
	 */
	private String bankId;

	/**
	 * 信用卡类别（区分银行）
	 */
	private String bankType;
	
	/** 终端信息 **/
	private String terminalInfo;
	

	/**  注册推广人 **/
	private String tgr;
	
	/**
	 * 手机号
	 */
	private String phoneNumber;
	
	/** 身份证号 **/
	private String idNumber;
	
	/** 真实姓名 **/
	private String realName;
	
	
	/** 商户名称 **/
	private String merchantName;

	/** 银行名称 **/
	private String bankName;
	
	/** 分行名称 **/
	private String bankSubbranch;
	
	/** 银行卡号 **/
	private String bankCardNo;
	
	/** 所在省份 **/
	private String state;
	
	/** 联系地址 **/
	private String address;
	
	/** 邮箱 **/
	private String email;
	
	
	/** 手持身份证照片 **/
	private String handheldIDPhoto;
	
	/** 身份证正面照片 **/
	private String iDCardFrontPhoto;
	
	/** 身份证反面照片 **/
	private String iDCardReversePhoto;
	
	/** 银行卡照片 **/
	private String bankCardPhoto;
	
	/** 营业执照照片 **/
	private String businessPhoto;
	
	private String transAmount;
	
	/**  到账类型 T1 或者T0 **/
	private String tradeCode;
	
	/** 固定码访问地址  **/
	private String qrcodeurl;
	
	
	private String mac;
	
	/** 提现类型  信用卡提现和 分润提现 **/
	private String txType;
	
	/** 短信验证码 **/
	private String smsCode;
	
	
	/** ios设备token **/
	private String deviceToken;
	
	/** 设备类型 **/
	private String deviceType;
	
	/** 银行预留手机号 **/
	private String payerPhone;
	/** 订单号 **/
	private String orderNumber;
	/** 商户号 **/
	private String merchantNo;
	
	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getMerchantNo() {
		return merchantNo;
	}

	public void setMerchantNo(String merchantNo) {
		this.merchantNo = merchantNo;
	}

	public String getPayerPhone() {
		return payerPhone;
	}

	public void setPayerPhone(String payerPhone) {
		this.payerPhone = payerPhone;
	}

	public String getLoginID() {
		return loginID;
	}

	public void setLoginID(String loginID) {
		this.loginID = loginID;
	}

	public String getLoginPwd() {
		return loginPwd;
	}

	public void setLoginPwd(String loginPwd) {
		this.loginPwd = loginPwd;
	}

	public String getTxndir() {
		return txndir;
	}

	public void setTxndir(String txndir) {
		this.txndir = txndir;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getSendSeqId() {
		return sendSeqId;
	}

	public void setSendSeqId(String sendSeqId) {
		this.sendSeqId = sendSeqId;
	}

	public String getPayChannel() {
		return payChannel;
	}

	public void setPayChannel(String payChannel) {
		this.payChannel = payChannel;
	}


	public String getTerminalInfo() {
		return terminalInfo;
	}
	public void setTerminalInfo(String terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public String getAgencyNumber() {
		return agencyNumber;
	}

	public void setAgencyNumber(String agencyNumber) {
		this.agencyNumber = agencyNumber;
	}

	public String getBankType() {
		return bankType;
	}

	public void setBankType(String bankType) {
		this.bankType = bankType;
	}


	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankSubbranch() {
		return bankSubbranch;
	}

	public void setBankSubbranch(String bankSubbranch) {
		this.bankSubbranch = bankSubbranch;
	}

	public String getBankCardNo() {
		return bankCardNo;
	}

	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHandheldIDPhoto() {
		return handheldIDPhoto;
	}

	public void setHandheldIDPhoto(String handheldIDPhoto) {
		this.handheldIDPhoto = handheldIDPhoto;
	}

	public String getIDCardFrontPhoto() {
		return iDCardFrontPhoto;
	}

	public void setIDCardFrontPhoto(String iDCardFrontPhoto) {
		this.iDCardFrontPhoto = iDCardFrontPhoto;
	}

	public String getIDCardReversePhoto() {
		return iDCardReversePhoto;
	}

	public void setIDCardReversePhoto(String iDCardReversePhoto) {
		this.iDCardReversePhoto = iDCardReversePhoto;
	}

	public String getBankCardPhoto() {
		return bankCardPhoto;
	}

	public void setBankCardPhoto(String bankCardPhoto) {
		this.bankCardPhoto = bankCardPhoto;
	}

	public String getBusinessPhoto() {
		return businessPhoto;
	}

	public void setBusinessPhoto(String businessPhoto) {
		this.businessPhoto = businessPhoto;
	}

	public String getTgr() {
		return tgr;
	}

	public void setTgr(String tgr) {
		this.tgr = tgr;
	}


	public String getTransAmount() {
		return transAmount;
	}

	public void setTransAmount(String transAmount) {
		this.transAmount = transAmount;
	}

	public String getNewLoginPwd() {
		return newLoginPwd;
	}

	public void setNewLoginPwd(String newLoginPwd) {
		this.newLoginPwd = newLoginPwd;
	}

	public String getTradeCode() {
		return tradeCode;
	}

	public void setTradeCode(String tradeCode) {
		this.tradeCode = tradeCode;
	}

	public String getQrcodeurl() {
		return qrcodeurl;
	}

	public void setQrcodeurl(String qrcodeurl) {
		this.qrcodeurl = qrcodeurl;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getTxType() {
		return txType;
	}

	public void setTxType(String txType) {
		this.txType = txType;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}
}
