package com.example.demo.bean;

import java.beans.PropertyVetoException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *   c3p0  配置bean
 * @author a
 *
 */

@Configuration
public class ConfigC3P0 {
	@Value("${c3p0.datasource.driver-class-name}")
	protected String driverClassName;

	@Value("${c3p0.datasource.url}")
	protected String url;

	@Value("${c3p0.datasource.username}")
	protected String username;

	@Value("${c3p0.datasource.password}")
	protected String password;

	@Value("${c3p0.datasource.initialPoolSize}")
	protected int initialPoolSize;

	@Value("${c3p0.datasource.minPoolSize}")
	protected int minPoolSize;

	@Value("${c3p0.datasource.maxPoolSize}")
	protected int maxPoolSize;

	@Value("${c3p0.datasource.maxStatements}")
	protected int maxStatements;

	@Bean(name="jdbcTemplate")
	public JdbcTemplate jdbcTemplate() throws PropertyVetoException
	{
		return new JdbcTemplate(comboPooledDataSource());
	}

	@Bean(name="comboPooledDataSource")
	public ComboPooledDataSource comboPooledDataSource() throws PropertyVetoException
	{
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		cpds.setDriverClass(driverClassName);
		cpds.setJdbcUrl(url);
		cpds.setUser(username);
		cpds.setPassword(password);
		cpds.setInitialPoolSize(initialPoolSize);
		cpds.setMinPoolSize(minPoolSize);
		cpds.setMaxPoolSize(maxPoolSize);
		cpds.setMaxStatements(maxStatements);
		return cpds;
	}
}
