package com.example.demo.util;

import org.apache.log4j.Logger;

public class LoggerTool {

	private String claName;


	public LoggerTool(Class<?> cls){
		
		this.claName = cls.getName();
	}
	
	public void info(String content){
		Logger log = Logger.getLogger(claName);
		log.info(content);

	}
	
	
	
	public void error(String content){
		Logger log = Logger.getLogger(claName);
		log.error(content);
	}
}
