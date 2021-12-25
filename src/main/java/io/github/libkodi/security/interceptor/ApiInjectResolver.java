package io.github.libkodi.security.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.github.libkodi.security.CacheManager;
import io.github.libkodi.security.SecurityManager;
import io.github.libkodi.security.annotation.ApiBody;
import io.github.libkodi.security.annotation.ApiCache;
import io.github.libkodi.security.annotation.ApiParam;
import io.github.libkodi.security.interfaces.Cache;
import io.github.libkodi.security.interfaces.GetCacheIdHandle;
import io.github.libkodi.security.properties.AuthProperties;
import io.github.libkodi.security.utils.HttpRequestUtils;
import io.github.libkodi.security.utils.ServletRequestUtils;
import io.github.libkodi.security.utils.StringUtils;

/**
 * 
 * @author solitpine
 * @description 参数注解支持 
 *
 */
public class ApiInjectResolver implements HandlerMethodArgumentResolver {

	private CacheManager cacheManager;
	private AuthProperties properties;
	private GetCacheIdHandle getCacheIdHandle;

	public ApiInjectResolver(SecurityManager context) {
		this.cacheManager = context.getCacheManager();
		this.properties = context.getProperties();
		this.getCacheIdHandle = context.getCacheIdHandle();
	}
	
	/**
	 * 支持的注解
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(ApiParam.class) ||
				parameter.hasParameterAnnotation(ApiBody.class) ||
				parameter.hasParameterAnnotation(ApiCache.class);
	}
	
	/**
	 * 处理注解获取返回值
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = ServletRequestUtils.getHttpServletRequest();
		/**
		 * 获取请求主体
		 */
		byte[] body = cacheManager.get("$body", byte[].class);
		
		if (body == null) {
			if (request instanceof WebHttpServletRequestWarpper) {
				body = ((WebHttpServletRequestWarpper) request).getBody();
			} else {
				body = HttpRequestUtils.getBody(request);
			}
			
			cacheManager.put("$body", body);
		}
		
		// 如果获取请求主体
		if (parameter.hasParameterAnnotation(ApiBody.class)) {
			ApiBody apiBody = parameter.getParameterAnnotation(ApiBody.class);
			
			if (apiBody.required() && (body == null || body.length < 1)) {
				throw new Exception("The API requires a request body, but the body is empty");
			} else {
				if (parameter.getParameterType().equals(byte[].class)) {
					return body;
				}
				
				return JSON.parseObject(new String(body, "UTF-8"), parameter.getParameterType());
			}
		}
		// 如果获取请求参数
		else if (parameter.hasParameterAnnotation(ApiParam.class)) {
			ApiParam apiParam = parameter.getParameterAnnotation(ApiParam.class);
			JSONObject allParams = HttpRequestUtils.getAllParameter(request, cacheManager);
			
			/**
			 * 判断并返回需要的数据类型
			 */
			if (apiParam.required() && !allParams.containsKey(apiParam.name()) && "".equals(apiParam.defaultValue())) {
				throw new Exception("The parameter requires a content, but the content is empty");
			} else {
				if (!apiParam.required() && !allParams.containsKey(apiParam.name()) && "".equals(apiParam.defaultValue())) {
					return null;
				} else if (!allParams.containsKey(apiParam.name())) {
					return JSON.parseObject(apiParam.defaultValue(), parameter.getParameterType());
				}
				
				return allParams.getObject(apiParam.name(), parameter.getParameterType());
			}
		} 
		// 如果获取会话
		else if (parameter.hasParameterAnnotation(ApiCache.class)) {
			ApiCache apiCache = parameter.getParameterAnnotation(ApiCache.class);
			String cacheId = getCacheIdHandle != null ? getCacheIdHandle.call(request, properties.getCacheKey()) : HttpRequestUtils.getParameter(request, properties.getCacheKey());
			
			if (!StringUtils.isEmpty(cacheId)) {
				Cache cache = null;
				
				if (cacheManager.contains(cacheId)) {
					cache = cacheManager.instance(cacheId);
				} else if (apiCache.create()) {
					cache = cacheManager.instance();
				}
				
				return cache;
			} else if (apiCache.create()) {
				return cacheManager.instance();
			}
		}
		
		return null;
	}
}
