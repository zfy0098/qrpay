package com.example.demo.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggerTool {

	private String claName;
	
	public LoggerTool(Class<?> cls){
		
		this.claName = cls.getName();
	}
	
	
	
	public void info(String content){
		Log log = LogFactory.getLog("TermMsg");
		log.info(claName + "^" +  content);
	}
	
	
	
	public void error(String content){
		Log log = LogFactory.getLog("error");
		log.error(claName + "^" + content);
	}
}
