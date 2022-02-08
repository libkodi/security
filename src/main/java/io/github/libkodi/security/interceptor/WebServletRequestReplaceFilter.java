package io.github.libkodi.security.interceptor;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import io.github.libkodi.security.CacheManager;

/**
 * 
 * 拦截请求对象并以自定义请求对象容器替换 
 */
public class WebServletRequestReplaceFilter implements Filter {

	private CacheManager cacheManager;

	public WebServletRequestReplaceFilter(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		ServletRequest requestWarpper = null;
		
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getServletContext());
		
		/**
		 * 判断是否multipart/form-data
		 * 如果是则先解析文件数据
		 */
		if (multipartResolver.isMultipart((HttpServletRequest) request)) {
			requestWarpper = new WebHttpServletRequestWarpper(multipartResolver.resolveMultipart((HttpServletRequest) request), cacheManager);
		} else if (request instanceof HttpServletRequest) {
			requestWarpper = new WebHttpServletRequestWarpper((HttpServletRequest) request, cacheManager);
		}
		
		if (requestWarpper != null) {
			chain.doFilter(requestWarpper, response);
		} else {
			chain.doFilter(request, response);
		}
	}
	
	@Override
	public void destroy() {
		
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

}
