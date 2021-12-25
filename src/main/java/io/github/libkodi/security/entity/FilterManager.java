package io.github.libkodi.security.entity;

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
 * 
 * @author solitpine
 * @description api过滤器 
 *
 */
public class FilterManager {
	private ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
	private AuthFilterBeforeHandle beforeHandler = null;
	private AuthFilterAfterHandle afterHandler = null;
	
	public FilterManager() {}
	
	public FilterManager add(String match, AuthFilterHandle handler) {
		HashMap<String, Object> filter = new HashMap<String, Object>();
		filter.put("regexp", match);
		filter.put("handler", handler);
		list.add(filter);
		return this;
	}
	
	public FilterManager setBeforeFilter(AuthFilterBeforeHandle handler) {
		beforeHandler  = handler;
		return this;
	}
	
	public FilterManager setAfterFilter(AuthFilterAfterHandle handler) {
		afterHandler = handler;
		return this;
	}
	
	public boolean doBeforeFilter(SecurityManager context, HttpServletRequest request) {
		if (beforeHandler != null) {
			return beforeHandler.call(context, request);
		}
		
		return true;
	}
	
	public Object doAfterFilter(SecurityManager context, HttpServletResponse response, Object value) {
		if (afterHandler != null) {
			return afterHandler.call(context, response, value);
		}
		
		return value;
	}
	
	public AuthFilterHandle getFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();
		Iterator<HashMap<String, Object>> iter = list.iterator();
		HashMap<String, Object> filter;
		String match;
		AuthFilterHandle handler = null;
		
		while (iter.hasNext()) {
			filter = iter.next();
			match = (String) filter.get("regexp");
			
			if (Pattern.matches(match, uri)) {
				handler = (AuthFilterHandle) filter.get("handler");
				break;
			}
		}
		
		return handler;
	}
}
