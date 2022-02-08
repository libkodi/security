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
import io.github.libkodi.security.annotation.ApiRole;
import io.github.libkodi.security.error.Error401;
import io.github.libkodi.security.interfaces.RolesHandle;
import io.github.libkodi.security.utils.ServletRequestUtils;

/**
 * 规则组验证切入 
 */
@Configuration
@Aspect
@Order(-980)
public class ApiRoleAspect {
	@Autowired
	private SecurityManager context;
	
	@Autowired(required = false)
	private RolesHandle rolesHandle;
	
	@Around("@annotation(io.github.libkodi.security.annotation.ApiRole)")
	private Object joinPoint(ProceedingJoinPoint point) throws Throwable {
		if (rolesHandle != null) {
			HttpServletRequest request = ServletRequestUtils.getHttpServletRequest();
			HttpServletResponse response = ServletRequestUtils.getHttpServletResponse();
			
			MethodSignature method = (MethodSignature) point.getSignature();
			ApiRole role = method.getMethod().getAnnotation(ApiRole.class);
			
			if (!rolesHandle.call(context, request, role.value(), role.mode())) {
				return (new Error401("Unauthorized")).sync(request, response);
			}
		}
		
		return point.proceed();
	}
}
