package com.example.demo.controller;

import com.example.demo.constant.Constant;
import com.example.demo.util.EhcacheUtil;
import com.example.demo.util.LoggerTool;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/clearcache")
public class ClearCacheController{

	LoggerTool logger = new LoggerTool(this.getClass());
	
	@RequestMapping("")
	@CacheEvict(value = Constant.cacheName, allEntries = true)
	public Object ClearCache(){
		logger.info("清除缓存中所有内容");
		EhcacheUtil ehcache = EhcacheUtil.getInstance();
		ehcache.clear(Constant.cacheName);
		return "SUCCESS";
	}
}
