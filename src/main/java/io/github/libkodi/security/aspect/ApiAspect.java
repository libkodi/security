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
 * 权限验证切入
 */
@Aspect
@Order(-1000)
public class ApiAspect {
	/**
	 * 注入自定义的接口筛选
	 */
	@Autowired(required = false)
	private FilterManager filter;
	
	/**
	 * 权限控制上下文对象，用来获取其它管理器与配置属性
	 */
	@Autowired
	private SecurityManager context;
	
	/**
	 * Api出错的接收句柄
	 */
	@Autowired(required = false)
	private ExceptionHandle errorCallback;
	
	@Around("@annotation(io.github.libkodi.security.annotation.Api)")
	private Object joinPoint(ProceedingJoinPoint point) throws Throwable {
		HttpServletRequest request = ServletRequestUtils.getHttpServletRequest();
		HttpServletResponse response = ServletRequestUtils.getHttpServletResponse();
		
		/**
		 * 检测IP是否被阻止访问
		 */
		if (!context.getAccessManager().verify(request)) {
			return (new Error402("Frequent requests")).sync(request, response);
		}
		
		if (filter != null) {
			// Api执行前的处理回调
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
			if (filter != null) {
				return filter.doAfterFilter(context, response, point.proceed());
			} else {
				return point.proceed();
			}
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
