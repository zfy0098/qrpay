package com.example.demo.util;


import com.example.demo.constant.StringEncoding;

public class MakeCipherText{


	/**
	 *   制作登录密码
	 * @param keyIndex
	 * @param pwd
	 * @param keyIndexOut
	 * @return
	 */
    public static String MakeLoginPwd(String keyIndex,String pwd,String keyIndexOut){
		//加密
		try {
			// 解析密钥明文
			pwd = DESUtil.rightPad(pwd, 16, " ");
			pwd = DESUtil.bytes2HexStr(pwd.getBytes(), false);
			String initKeyoutStr = LoadPro.loadProperties("jmj", keyIndexOut);
			return DESUtil.bcd2Str(DESUtil.encrypt3(pwd, initKeyoutStr));			
		} catch (Exception e) {
					
			e.printStackTrace();
			return "";
		}
	}
    
    
    public static String calLoginPwd(String usrID,String pwd ,String sendTime){
		// 加密
		try {
			String keyIndex= LoadPro.loadProperties("config", "protectINDEX");
			
			String initKey = LoadPro.loadProperties("jmj", keyIndex);
			// 解析密码明文
			String keyde = new String(DESUtil.decrypt3(pwd, initKey));
			
			return MD5.sign(usrID + sendTime + keyde.replace(" ", "") , StringEncoding.UTF_8 );

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
    }
    
    
    /**
     *   制作签名mac
     * @param macStr
     * @param key
     * @return
     */
	public static String makeMac(String macStr, String key) {
		System.out.println("macStr" + macStr + ", key :" + key);
		String initKey = LoadPro.loadProperties("config", "DBINDEX");
		return DESUtil.mac(macStr, key, initKey);
	}
}
