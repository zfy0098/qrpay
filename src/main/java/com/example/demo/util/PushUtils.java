package com.example.demo.util;

import com.example.demo.constant.Constant;
import com.tencent.xinge.Message;
import com.tencent.xinge.Style;
import com.tencent.xinge.XingeApp;


public class PushUtils {

	
	static LoggerTool log = new LoggerTool(PushUtils.class);
	
	public static void IOSPush(String content,String tonken){
		log.info(XingeApp.pushTokenIos(Constant.XingeApp_IOS_ACCESS_ID, Constant.XingApp_IOS_ACCESS_KEY, content, tonken,  XingeApp.IOSENV_PROD).toString());
	
		
//		XingeApp xinge = new XingeApp(Constant.XingeApp_IOS_ACCESS_ID, Constant.XingApp_IOS_ACCESS_KEY);
//		MessageIOS remoteMessageIOS = new MessageIOS();
//		remoteMessageIOS.setType(MessageIOS.TYPE_REMOTE_NOTIFICATION);
//		remoteMessageIOS.setRaw(content);
//		System.out.println(xinge.pushSingleDevice(tonken, remoteMessageIOS , XingeApp.IOSENV_PROD));
	}
	
	public static void AndroidPush(String title, String content , String token){
		XingeApp xinge = new XingeApp(Constant.XingApp_Android_ACCESS_ID, Constant.XingApp_Android_ACCESS_KEY);
		Style style = new Style(0, 1, 1, 1, 0, 1,0,1);
		Message message = new Message();
        message.setTitle(title);
        message.setContent(content);
        message.setType(Message.TYPE_NOTIFICATION);
        message.setExpireTime(0);
        message.setStyle(style);
        log.info(xinge.pushSingleDevice(token, message).toString().toLowerCase());
	}
	
	
	public static void main(String[] args) {
		// 7ee4fa40739a0dcd13c8c1f1b8602e4c65d812f919644d5f5d27181f32916221
		PushUtils.IOSPush("你有新收入了 111", "3757c792b36f9ebe1adcc5eb4999e6639eef384afc0a5a59de0b26a16a9a9170");
		
//		AndroidPush("测试的推送", "你有新hjkhjkhkjhjkhjkh了", "c9f647d285603bcc4c67acac206007aa42505165");
		
		
	}
}
