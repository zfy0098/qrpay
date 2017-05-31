package com.example.demo.authen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

import com.example.demo.constant.Constant;
import com.example.demo.constant.StringEncoding;
import com.example.demo.mode.TJJQRequestData;
import com.example.demo.mode.TJJQResponseData;
import com.example.demo.util.DESUtil;
import com.example.demo.util.MD5;
import org.apache.commons.codec.binary.Base64;



public class QueryTrans {

	public static TJJQResponseData queryTrans(String userId, String orderId, String timeStamp) throws Exception{
		System.setProperty("javax.net.ssl.keyStore", "/home/slc/website/keystoresserver.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword","111111");
        System.setProperty("javax.net.ssl.keyStoreType", "JKS");
        
		System.setProperty("javax.net.ssl.trustStore", "/home/slc/website/keystoresserver.keystore");
		System.setProperty("javax.net.ssl.trustStorePassword","111111");
		HttpsURLConnection.setDefaultHostnameVerifier(new TrustAnyVerifier());
		String requestUrl = Constant.URL+"/auth-web/trans/transQuery";
		
		
		String resultString = "";
		HttpsURLConnection httpURLConnection = null;
		URL url;
		try {
			//发送请求
			url = new URL(requestUrl);
			httpURLConnection = (HttpsURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(50000);
			httpURLConnection.setReadTimeout(50000);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestProperty("Content-type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			httpURLConnection.setRequestMethod("POST");

			PrintStream out = null;
			try {
				httpURLConnection.connect();
				out = new PrintStream(httpURLConnection.getOutputStream(), false,
						"UTF-8");
				out.print(getTransReq(userId, orderId, timeStamp));
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
				System.out.println(resultString);
				if(null == resultString || "".equals(resultString.trim()) || resultString.length() < 29) {
					return null;
				} else {
					resultString = new String(DESUtil.decryptMode(Base64.decodeBase64(resultString.substring(29)), Constant.DYN_3DES_KEY, StringEncoding.UTF_8), StringEncoding.UTF_8);
				}				
				System.out.println(resultString);
				TJJQResponseData response = XmlUtil.converyToJavaBean(resultString, TJJQResponseData.class);

				System.out.println("交易结果：" + response.getResultCode() + response.getResultDesc());
				return response;
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
	
	public static String getTransReq(String userId, String orderId, String timeStamp) throws Exception {
		String result = "";
		
		TJJQRequestData request = new TJJQRequestData();
		request.setCharCode(StringEncoding.UTF_8);
		request.setVersion("1.0.0");
		request.setTradeType("0415");
		request.setChnlId(Constant.CHANNEL_ID);
		request.setUserId(userId);
		request.setTradeSource("1");
		request.setOrderId(orderId);
		request.setTimeStamp(timeStamp);
		request.setMd5ConSec(MD5.genMd5ConSec(request));
		
		String xmlStr = XmlUtil.convertToXml(request);
		System.out.println("组装XML报文");
		System.out.println(xmlStr);
		
		byte[] xmlByte = DESUtil.encryptMode(xmlStr.getBytes(StringEncoding.UTF_8), Constant.DYN_3DES_KEY, StringEncoding.UTF_8);
		result = MD5.appendLeft(userId, " ", 12) + Constant.CHANNEL_ID + "1" + Base64.encodeBase64String(xmlByte);
		result = MD5.addZeroForNum(String.valueOf(xmlByte.length + 25), 4) + result;
		
		return result;
	}
	
	
}
