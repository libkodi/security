package io.github.libkodi.security.aspect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import io.github.libkodi.security.SecurityManager;
import io.github.libkodi.security.error.Error401;
import io.github.libkodi.security.error.Error402;
import io.github.libkodi.security.error.Error500;
import io.github.libkodi.security.factory.FilterManager;
import io.github.libkodi.security.interfaces.AuthFilterHandle;
import io.github.libkodi.security.interfaces.ExceptionHandle;
import io.github.libkodi.security.utils.ServletRequestUtils;

/**
 * 
 * @author solitpine
 * @description 权限验证切入
 *
 */
@Aspect
@Order(-1000)
public class ApiAspect {
	/**
	 * 注入自定义的接口筛选
	 */
	@Autowired(required = false)
	private FilterManager filter;
	
	@Autowired
	private SecurityManager context;
	
	@Autowired(required = false)
	private ExceptionHandle errorCallback;
	
	@Around("@annotation(io.github.libkodi.security.annotation.Api)")
	private Object joinPoint(ProceedingJoinPoint point) throws Throwable {
		HttpServletRequest request = ServletRequestUtils.getHttpServletRequest();
		HttpServletResponse response = ServletRequestUtils.getHttpServletResponse();
		
		if (filter != null) {
			if (!context.getAccessManager().verify(request)) {
				return (new Error402("Requests are too frequent. Please try again later")).sync(request, response);
			}
			
			if (!filter.doBeforeFilter(context, request)) {
				return (new Error401("Unauthorized")).sync(request, response);
			}
			
			// 查找出与当前请求url匹配的过滤器
			AuthFilterHandle handler = filter.getFilter(request);
			
			if (handler != null) {
				if (!handler.call(context, request)) { // 调用过滤器判断是否有访问权限
					return (new Error401("Unauthorized")).sync(request, response);
				}
			}
		}
		
		try {
			return filter.doAfterFilter(context, response, point.proceed());
		} catch (Exception e) {
			if (errorCallback != null) {
				try {
					errorCallback.call(request, response, e);
				} catch (Exception e2) {}
			}
			
			return (new Error500(e.getMessage())).sync(request, response);
		}
	}
	
}
