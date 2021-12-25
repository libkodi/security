package io.github.libkodi.security.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletRequestUtils {
	/**
	 * @description 获取当前请求对象
	 * @author solitpine
	 * @date 6 Sep 2021
	 * @time 15:08:52
	 * @return
	 */
	public static HttpServletRequest getHttpServletRequest() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attr.getRequest();
	}
	
	/**
	 * @description 获取当前响应对象
	 * @author solitpine
	 * @date 6 Sep 2021
	 * @time 15:08:52
	 * @return
	 */
	public static HttpServletResponse getHttpServletResponse() {
		ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return attr.getResponse();
	}
}
