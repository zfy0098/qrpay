package com.example.demo.authen;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Map;

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
import com.example.demo.util.OrderUtil;
import org.apache.commons.codec.binary.Base64;


import net.sf.json.JSONObject;

public class ApiTrans {

	public static TJJQResponseData doTrans(Map<String, String> map) throws Exception {
		System.setProperty("javax.net.ssl.keyStore", "/home/slc/website/keystoresserver.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "111111");
		System.setProperty("javax.net.ssl.keyStoreType", "JKS");

		System.setProperty("javax.net.ssl.trustStore", "/home/slc/website/keystores/server.keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "111111");
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

			// 发送请求
			url = new URL(Constant.URL_TRADE);
			httpURLConnection = (HttpsURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout(50000);
			httpURLConnection.setReadTimeout(50000);
			httpURLConnection.setDoInput(true);
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setSSLSocketFactory(ssf);

			PrintStream out = null;
			try {
				httpURLConnection.connect();
				out = new PrintStream(httpURLConnection.getOutputStream(), false, "UTF-8");
				out.print(getTransReq(map));
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			} finally {
				if (out != null) {
					out.close();
				}
			}

			// 接收响应
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
				try {
					JSONObject jsonObject = JSONObject.fromObject(resultString);
					System.out.println(jsonObject.getString("status"));
					System.out.println(jsonObject.getString("msg"));
					/*
					 * 返回值前加数字，用于判断返回内容为动态密钥还是错误信息 00:正确信息 01:错误信息
					 */
					TJJQResponseData responseData = new TJJQResponseData();
					responseData.setResultCode(jsonObject.getString("status"));
					responseData.setResultDesc(jsonObject.getString("msg"));
					return responseData;
				} catch (Exception e) {
					// System.out.println("应答报文不是JSON格式，进行正常处理！");
				}

				if (null == resultString || "".equals(resultString.trim()) || resultString.length() < 29) {
					return null;
				} else {
					resultString = new String(DESUtil.decryptMode(Base64.decodeBase64(resultString.substring(29)),
							Constant.DYN_3DES_KEY, StringEncoding.UTF_8), StringEncoding.UTF_8);
				}
				TJJQResponseData response = XmlUtil.converyToJavaBean(resultString, TJJQResponseData.class);

				System.out.println(resultString);
				System.out.println("验证结果：" + response.getResultCode() + response.getResultDesc());
				System.out.println("机构号" + response.getChnlId());
				return response;
			} catch (Exception e) {
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
		} catch (Exception e1) {
			throw e1;
		}
	}

	public static String getTransReq(Map<String, String> map) throws Exception {
		String result = "";

		TJJQRequestData request = new TJJQRequestData();
		request.setCharCode(StringEncoding.UTF_8);
		request.setVersion("1.0.0");
		request.setTradeType(Constant.TRADE_TYPE);
		request.setChnlId(Constant.CHANNEL_ID);
		request.setUserId(Constant.USER_ID);
		request.setTradeSource("1");
		request.setOrderId(OrderUtil.getDateTimeTemp() + OrderUtil.getRandomStr(2));
		request.setTimeStamp(DateUtil.getDateTimeTemp());
		request.setAccNo(map.get("accNo"));
		request.setName(map.get("name"));
		request.setCertificateCode(map.get("certificateCode"));
		request.setNbr(map.get("nbr"));
		request.setCVN2(map.get("cvn2"));
		request.setExpired(map.get("expired"));
		request.setParams(map.get("params"));
		request.setBusinessName(map.get("businessName"));
		request.setMd5ConSec(MD5.genMd5ConSec(request));

		String xmlStr = XmlUtil.convertToXml(request);
		System.out.println("组装XML报文");
		System.out.println(xmlStr);

		byte[] xmlByte = DESUtil.encryptMode(xmlStr.getBytes(StringEncoding.UTF_8), Constant.DYN_3DES_KEY,
				StringEncoding.UTF_8);
		result = MD5.appendLeft(Constant.USER_ID, " ", 12) + Constant.CHANNEL_ID + "1"
				+ Base64.encodeBase64String(xmlByte);
		result = MD5.addZeroForNum(String.valueOf(xmlByte.length + 25), 4) + result;
		System.out.println("请求报文：" + result);
		return result;
	}
}
