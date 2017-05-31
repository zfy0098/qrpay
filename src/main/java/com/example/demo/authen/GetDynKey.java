package com.example.demo.authen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.example.demo.constant.Constant;
import com.example.demo.constant.StringEncoding;
import com.example.demo.mode.TJJQRequestData;
import com.example.demo.mode.TJJQResponseData;
import com.example.demo.util.DESUtil;
import com.example.demo.util.DateUtil;
import com.example.demo.util.MD5;
import com.example.demo.util.UtilsConstant;
import org.apache.commons.codec.binary.Base64;


import net.sf.json.JSONObject;



public class GetDynKey {

	public static String getDynKey() throws Exception{
		System.setProperty("javax.net.ssl.keyStore", "/home/slc/website/keystoresserver.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword","111111");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");
        
		System.setProperty("javax.net.ssl.trustStore", "/home/slc/website/keystoresserver.keystore");
		System.setProperty("javax.net.ssl.trustStorePassword","111111");
		HttpsURLConnection.setDefaultHostnameVerifier(new TrustAnyVerifier());
		
		
		String resultString = "";
		HttpsURLConnection httpURLConnection = null;
		URL url;
		try {
			 TrustManager[] tm = { (TrustManager) new MyX509TrustManager() };    
	          SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");    
	            sslContext.init(null, tm, new java.security.SecureRandom());    
	              
	            // 从上述SSLContext对象中得到SSLSocketFactory对象     
	          SSLSocketFactory ssf = sslContext.getSocketFactory();  
			
			//发送请求
			url = new URL(Constant.URL_DYN);
			httpURLConnection = (HttpsURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(50000);
			httpURLConnection.setReadTimeout(50000);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setSSLSocketFactory(ssf);

			PrintStream out = null;
			try {
				httpURLConnection.connect();
				out = new PrintStream(httpURLConnection.getOutputStream(), false,
						"UTF-8");
				out.print(getRandomPwdReq());
				out.flush();
			} catch (Exception e) {
				throw e;
			} finally {
				if (out != null) {
					out.close();
				}
			}

			//接收响应
			InputStream in = null;
			StringBuilder sb = new StringBuilder(1024);
			BufferedReader br = null;
			String temp = null;
			try {
				if (200 == httpURLConnection.getResponseCode()) {
					in = httpURLConnection.getInputStream();
					br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					while ((temp = br.readLine()) != null) {
						sb.append(temp);
					}
				} else {
					in = httpURLConnection.getErrorStream();
					br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					while ((temp = br.readLine()) != null) {
						sb.append(temp);
					}
				}

				resultString = sb.toString();
				System.out.println("应答报文:" + resultString);
				try{
					JSONObject jsonObject = JSONObject.fromObject(resultString);
					System.out.println(jsonObject.getString("status"));
					System.out.println(jsonObject.getString("msg"));
					/*
					 * 返回值前加数字，用于判断返回内容为动态密钥还是错误信息
					 * 00:正确信息
					 * 01:错误信息
					 * */
					return "01" + "状态：" + jsonObject.getString("status") + "信息：" + jsonObject.getString("msg");
				} catch(Exception e){
//					e.printStackTrace();
//					System.out.println("应答报文不是JSON格式，进行正常处理！");
				}
				
				resultString = new String(DESUtil.decryptMode(Base64.decodeBase64(resultString.substring(29)), Constant.DES3_KEY, StringEncoding.GBk), StringEncoding.GBk);
				
				TJJQResponseData response = XmlUtil.converyToJavaBean(resultString, TJJQResponseData.class);
				System.out.println("秘钥随机数：" + response.getRandom());
				String dyn3DesKey = MD5.md5(Constant.DES3_KEY + response.getRandom(), StringEncoding.GBk).substring(4, 28);
				System.out.println("秘钥：" + dyn3DesKey);
				Constant.DYN_3DES_KEY = dyn3DesKey;
				Constant.RANDOM = response.getRandom();
				/*
				 * 返回值前加数字，用于判断返回内容为动态密钥还是错误信息
				 * 00:正确信息
				 * 01:错误信息
				 * */
				return "00" + dyn3DesKey;
			}catch (Exception e) {
				throw e;
			} finally {
				if (br != null) {
					br.close();
				}
				if (in != null) {
					in.close();
				}
				if (httpURLConnection != null) {
					httpURLConnection.disconnect();
				}
			}
		}catch (Exception e1) {
			throw e1;
		}
	}
	
	public static String getRandomPwdReq() throws Exception {
		String result = "";
		
		TJJQRequestData request = new TJJQRequestData();
		request.setCharCode(StringEncoding.UTF_8);
		request.setVersion("1.0.0");
		request.setTradeType("0413");
		request.setChnlId(Constant.CHANNEL_ID);
		request.setUserId(Constant.CHANNEL_ID);
		request.setTradeSource("1");
		request.setOrderId(UtilsConstant.getOrderNumber());
		request.setTimeStamp(DateUtil.getDateTimeTemp());
		request.setMd5ConSec(MD5.genMd5ConSec(request));
		
		String xmlStr = XmlUtil.convertToXml(request);
		System.out.println("组装XML报文");
		System.out.println(xmlStr);
		
		byte[] xmlByte = DESUtil.encryptMode(xmlStr.getBytes(StringEncoding.UTF_8), Constant.DES3_KEY, StringEncoding.UTF_8);
		result = Constant.CHANNEL_ID + Constant.CHANNEL_ID + "1" + Base64.encodeBase64String(xmlByte);
		result = MD5.addZeroForNum(String.valueOf(xmlByte.length + 25), 4) + result;
		System.out.println("请求报文：" + result);
		return result;
	}
	
	
}
