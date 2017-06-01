package com.example.demo.service;

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
@CacheConfig(cacheNames = "sampleCache2")
public class TestService {

    @Autowired
    private TestDB testDB;

    @Cacheable(value  ="sampleCache2" , key = "T(String).valueOf(#LoginID).concat('userinfo')")
    public List<Map<String,Object>> testlist(String LoginID){
        return testDB.testlist(LoginID);
    }


    @Cacheable(value = "sampleCache2" ,  key = "T(String).valueOf(#id).concat('merchant')")
    public List<Map<String,Object>> merchantlist(String id){
        return testDB.merchantlist(id);
    }
}
