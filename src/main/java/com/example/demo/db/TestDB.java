package com.example.demo.db;


import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by a on 2017/6/1.
 */
@Repository
public class TestDB  extends  DBBase{


    public List<Map<String,Object>> testlist(String loginid){
        try {
            System.out.println("查询数据库睡眠三秒钟");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String sql = "select * from tab_loginuser where id=?";
        return jdbc.queryForList(sql, new Object[]{loginid});
    }

    public List<Map<String,Object>> merchantlist(String id){
        try {
            System.out.println("查询数据库睡眠三秒钟");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String sql = "select * from tab_pay_merchant where userid=?";
        return jdbc.queryForList(sql, new Object[]{id});
    }
}
