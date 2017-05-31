package com.example.demo.db;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DBBase {
	
    @Autowired
    public JdbcTemplate jdbc;

    public static JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init(){
        System.out.println("执行了这个方法");
        jdbcTemplate = jdbc;
    }

    
    /**
     * 
     * 
     *   被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器调用一次，类似于Serclet的inti()方法。被@PostConstruct修饰的方法会在构造函数之后，init()方法之前运行。
     *   
     *   
     *   
     *   被@PreConstruct修饰的方法会在服务器卸载Servlet的时候运行，并且只会被服务器调用一次，类似于Servlet的destroy()方法。被@PreConstruct修饰的方法会在destroy()方法之后运行，在Servlet被彻底卸载之前。
     * 
     */
}
