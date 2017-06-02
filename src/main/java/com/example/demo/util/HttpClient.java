package com.example.demo.util;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class HttpClient {

	static LoggerTool log = new LoggerTool(HttpClient.class);
	
	/**
	 *     发送http post 请求
	 * @param url     请求地址 
	 * @param params  请求参数
	 * @param paramtype  参数形式 1：key-value形式 其他 json形式
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public static  String post(String url, Map<String, Object> params, String paramtype) {
		
		RequestConfig config = RequestConfig.custom()
				.setConnectionRequestTimeout(400000)
				.setConnectTimeout(400000)
				.setSocketTimeout(400000)
				.build();

		HttpPost httppost = new HttpPost(url);

		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();// 设置进去

		if (params != null) {
			try {
				if (paramtype != null && paramtype.equals("1")) {
					// key value 形式
					List<NameValuePair> formparams = new ArrayList<NameValuePair>();
					for (Map.Entry<String, Object> entry : params.entrySet()) {
						formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
					}
					UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
					httppost.setEntity(uefEntity);
				} else {
					StringEntity rsqentity;
					rsqentity = new StringEntity(JSONObject.fromObject(params).toString(), "utf-8");
					rsqentity.setContentEncoding("UTF-8");
					rsqentity.setContentType("application/json");
					httppost.setEntity(rsqentity);
				}
			} catch (UnsupportedEncodingException e) {
				return "";
			}
		}
		HttpResponse rsp = null;
		
		Long startTime = System.currentTimeMillis();
		
		log.info("=======================开始发送http请求:" + startTime);
		
		try {
			rsp = httpClient.execute(httppost);
			if (rsp != null) {
				HttpEntity entity = rsp.getEntity();
				InputStream in = entity.getContent();

				String temp;
				BufferedReader data = new BufferedReader(new InputStreamReader(in, "utf-8"));
				StringBuffer result = new StringBuffer();
				while ((temp = data.readLine()) != null) {
					result.append(temp);
					temp = null;
				}
				log.info("=======================http 请求结束-------------- , 用时：" + (System.currentTimeMillis()-startTime)/1000 + "秒"); 
				return result.toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
}
