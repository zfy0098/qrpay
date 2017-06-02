package com.example.demo.service;

import com.example.demo.constant.Constant;
import com.example.demo.db.TestDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
/**
 * Created by a on 2017/6/1.
 */
@Service
@CacheConfig(cacheNames = Constant.cacheName)
public class TestService {

    @Autowired
    private TestDB testDB;

    @Cacheable(key = "T(String).valueOf(#LoginID).concat('userinfo')")
    public List<Map<String,Object>> testlist(String LoginID){
        return testDB.testlist(LoginID);
    }


    @Cacheable(key = "T(String).valueOf(#id[0]).concat('merchant')")
    public List<Map<String,Object>> merchantlist(Object[] id){
        return testDB.merchantlist(id[0].toString());
    }
}
