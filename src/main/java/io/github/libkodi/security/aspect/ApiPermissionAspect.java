package io.github.libkodi.security.aspect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.github.libkodi.security.SecurityManager;
import io.github.libkodi.security.annotation.ApiPermission;
import io.github.libkodi.security.error.Error401;
import io.github.libkodi.security.interfaces.PermissionsHandle;
import io.github.libkodi.security.utils.ServletRequestUtils;

/**
 * 许可权限验证切入 
 */
@Aspect
@Configuration
@Order(-960)
public class ApiPermissionAspect {
	@Autowired
	private SecurityManager context;
	
	@Autowired(required = false)
	private PermissionsHandle promissionHandle;
	
	@Around("@annotation(io.github.libkodi.security.annotation.ApiPermission)")
	private Object joinPoint(ProceedingJoinPoint point) throws Throwable {
		if (promissionHandle != null) {
			HttpServletRequest request = ServletRequestUtils.getHttpServletRequest();
			HttpServletResponse response = ServletRequestUtils.getHttpServletResponse();
			
			MethodSignature method = (MethodSignature) point.getSignature();
			ApiPermission permission = method.getMethod().getAnnotation(ApiPermission.class);
			
			if (!promissionHandle.call(context, request, permission.value(), permission.mode())) {
				return (new Error401("Unauthorized")).sync(request, response);
			}
		}
		
		return point.proceed();
	}
}
