package io.github.libkodi.security.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.github.libkodi.security.SecurityManager;
import io.github.libkodi.security.interceptor.ApiInjectResolver;

/**
 * 
 * @author solitpine
 * @description 为参数注解添加支持 
 *
 */
@Configuration
public class ApiArgumentsConfig implements WebMvcConfigurer {
	@Autowired
	private SecurityManager context;
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new ApiInjectResolver(context));
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new CacheInterceptor(context));
	}
	
}
