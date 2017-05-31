package com.example.demo.service;

import java.util.Map;

import com.example.demo.constant.RespCode;
import com.example.demo.db.YMFTradeDB;
import com.example.demo.mode.RequestData;
import com.example.demo.mode.ResponseData;
import com.example.demo.mode.TabLoginuser;
import com.example.demo.util.DateUtil;
import com.example.demo.util.LoggerTool;
import com.example.demo.util.UtilsConstant;

/**
 *   绑定固定码
 * @author a
 *
 */

public class BindedYMFService {

	LoggerTool logger = new LoggerTool(this.getClass());
	
	public void BindedYMF(TabLoginuser user , RequestData reqdata, ResponseData respdata){
		
		logger.info("用户" + user.getLoginID() +"绑定固定码 , 固定码地址为：" + reqdata.getQrcodeurl()); 
		
		String qrcodeurl = reqdata.getQrcodeurl();
		
		String code = qrcodeurl.substring(qrcodeurl.indexOf("=") +1 );
		
		Map<String,Object> map = YMFTradeDB.getYMFCode(new Object[]{code});
		
		if(map!=null&&!map.isEmpty()){
			
			String agentID = user.getAgentID();
			
			String qrcodeAgentID = UtilsConstant.ObjToStr(map.get("AgentID"));
			
			if(agentID.equals(qrcodeAgentID)){
				int ret = YMFTradeDB.updateBindedInfo(new Object[]{user.getID(), DateUtil.getNowTime(DateUtil.yyyyMMdd) , code});
				
				if(ret > 0){
					logger.info("码数据" + code + "更新绑定信息成功");
					respdata.setRespCode(RespCode.SUCCESS[0]);
					respdata.setRespDesc(RespCode.SUCCESS[1]);
				}else{
					logger.info("码数据" + code + "更新绑定信息失败");
					respdata.setRespCode(RespCode.ServerDBError[0]);
					respdata.setRespDesc(RespCode.ServerDBError[1]);
				}
			} else {
				logger.info("申请固定码代理商与用户代理商不是同一个人无法进行绑定" + reqdata.getQrcodeurl());
				respdata.setRespCode(RespCode.BindedErrir[0]);
				respdata.setRespDesc(RespCode.BindedErrir[1]);
			}
		}else{
			logger.info("码数据" + code + "查询信息失败");
			respdata.setRespCode(RespCode.DATANOTEXISTError[0]);
			respdata.setRespDesc(RespCode.DATANOTEXISTError[1]);
		}
	}
}
