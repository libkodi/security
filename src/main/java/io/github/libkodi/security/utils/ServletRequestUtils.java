package io.github.libkodi.security.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletRequestUtils {
	/**
	 * 获取当前请求对象
	 */
	public static HttpServletRequest getHttpServletRequest() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attr.getRequest();
	}
	
	/**
	 * 获取当前响应对象
	 */
	public static HttpServletResponse getHttpServletResponse() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attr.getResponse();
	}
}
