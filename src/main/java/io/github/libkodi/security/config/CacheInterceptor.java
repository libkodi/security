package io.github.libkodi.security.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import io.github.libkodi.security.SecurityManager;

public class CacheInterceptor extends HandlerInterceptorAdapter {
	private SecurityManager context;

	public CacheInterceptor(SecurityManager context) {
		this.context = context;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		context.getCacheManager().delete();
	}
}
