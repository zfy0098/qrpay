package com.example.demo.bean;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

/**
 *    ehcache  配置bean
 * @author a
 *
 */

@EnableCaching
public class CacheConfiguration {
	@Bean
	public CacheManager  cacheManager() {
		return new EhCacheCacheManager(ehCacheCacheManager().getObject());
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		
		EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
        cmfb.setConfigLocation(new ClassPathResource("conf/ehcache.xml"));
        cmfb.setShared(true);
        return cmfb;
	}
	
	
	/**
	 * 
	 * 使用ehcache
	 *
	 * 使用ehcache主要通过spring的缓存机制，上面我们将spring的缓存机制使用了ehcache进行实现，
	 * 所以使用方面就完全使用spring缓存机制就行了。 具体牵扯到几个注解：
	 *
	 * @Cacheable：负责将方法的返回值加入到缓存中，参数3 @CacheEvict：负责清除缓存，参数4
	 *
	 * 参数解释：
	 *
	 * value：缓存位置名称，不能为空，如果使用EHCache，就是ehcache.xml中声明的cache的name
	 * key：缓存的key，默认为空，既表示使用方法的参数类型及参数值作为key，支持SpEL
	 * condition：触发条件，只有满足条件的情况才会加入缓存，默认为空，既表示全部都加入缓存，支持SpEL
	 *
	 * allEntries：CacheEvict参数，true表示清除value中的全部缓存，默认为false
	 * 
	 *
	 *
	 * 具体缓存的配置此处不再介绍，重点对于key的配置进行说明：
	 *		1、基本形式
	 *			@Cacheable(value="cacheName", key"#id")
	 *			public ResultDTO method(int id);
	 *		2. 组合形式
	 *			@Cacheable(value="cacheName", key"T(String).valueOf(#name).concat('-').concat(#password))
	 *			public ResultDTO method(int name, String password);
	 *		3、对象形式
	 *			@Cacheable(value="cacheName", key"#user.id)
	 *			public ResultDTO method(User user);
	 *		4、自定义Key生成器
	 *			@Cacheable(value="gomeo2oCache", keyGenerator = "keyGenerator")
	 *			public ResultDTO method(User user);
	 */
}
