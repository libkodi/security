package io.github.libkodi.security.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.libkodi.security.SecurityManager;
import io.github.libkodi.security.interfaces.AuthFilterAfterHandle;
import io.github.libkodi.security.interfaces.AuthFilterBeforeHandle;
import io.github.libkodi.security.interfaces.AuthFilterHandle;

/**
 * Api过滤器
 */
public class FilterManager {
	private ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	private AuthFilterBeforeHandle beforeHandler = null;
	private AuthFilterAfterHandle afterHandler = null;
	
	public FilterManager() {}
	
	/**
	 * 
	 * 添加一个匹配处理
	 *
	 * @param match 正则表达式
	 * @param handler 匹配到后的处理句柄
	 * @return FilterManager
	 */
	public FilterManager add(String match, AuthFilterHandle handler) {
		HashMap<String, Object> filter = new HashMap<String, Object>();
		filter.put("regexp", match);
		filter.put("handler", handler);
		list.add(filter);
		return this;
	}
	
	/**
	 * 
	 * 筛选前的处理句柄
	 *
	 * @param handler 处理句柄
	 * @return FilterManager
	 */
	public FilterManager setBeforeFilter(AuthFilterBeforeHandle handler) {
		beforeHandler  = handler;
		return this;
	}
	
	/**
	 * 
	 * 筛选后的处理句柄
	 *
	 * @param handler 处理句柄
	 * @return FilterManager
	 */
	public FilterManager setAfterFilter(AuthFilterAfterHandle handler) {
		afterHandler = handler;
		return this;
	}
	
	/**
	 * 
	 * 执行筛选前的处理
	 *
	 * @param context 上下文
	 * @param request 请求类
	 * @return true/false
	 */
	public boolean doBeforeFilter(SecurityManager context, HttpServletRequest request) {
		if (beforeHandler != null) {
			return beforeHandler.call(context, request);
		}
		
		return true;
	}
		
	/**
	 * 
	 * 执行筛选后的处理
	 *
	 * @param context 上下文
	 * @param response 响应类
	 * @param value Api执行返回的结果
	 * @return 处理后的结果
	 */
	public Object doAfterFilter(SecurityManager context, HttpServletResponse response, Object value) {
		if (afterHandler != null) {
			return afterHandler.call(context, response, value);
		}
		
		return value;
	}
	
	/**
	 * 
	 * 查找匹配的句柄
	 *
	 * @param request 请求类
	 * @return AuthFilterHandle
	 */
	public AuthFilterHandle getFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		Iterator<HashMap<String, Object>> iter = list.iterator();
		HashMap<String, Object> filter;
		String match;
		AuthFilterHandle handler = null;
		
		while (iter.hasNext()) {
			filter = iter.next();
			match = (String) filter.get("regexp");
			Pattern reg = Pattern.compile(match);
			
			if (reg.matcher(uri).find()) {
				handler = (AuthFilterHandle) filter.get("handler");
				break;
			}
		}
		
		return handler;
	}
}
