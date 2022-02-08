package io.github.libkodi.security.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.github.libkodi.security.CacheManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpRequestUtils {
	/**
	 * 获取请求终端IP
	 */
	public static String getRemoteAddress(HttpServletRequest request) {
		String ip = (String) request.getAttribute("REQUEST_REMOTE_ADDRESS");
		
		if (ip == null) {
			ip = request.getHeader("X-Forwarded-For");
		       
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			   
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			   
			if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
	   
			try {
				String[] arr = ip.split(",");
				   
				if (arr.length > 0) {
					ip = arr[0];
				}
			} catch (Exception e) {
				log.error("", e);
			}
			
			request.setAttribute("REQUEST_REMOTE_ADDRESS", ip == null ? "" : ip);
		}
		
		return ip;
	}
	
	/**
	 * 读取请求主体
	 */
	public static byte[] getBody(HttpServletRequest request) {
		byte[] resultBody = new byte[0];
		
		if (!request.getMethod().equals("GET")) {
			try {
				// 读取body内容
				ServletInputStream is = request.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				
				try {
					int len = 0;
					byte[] buffer = new byte[1024];
					
					while ((len = is.read(buffer)) > 0) {
						bos.write(buffer, 0, len);
					}
					
					resultBody = bos.toByteArray();
				} catch (Exception e) {
					log.error("", e.getMessage());
				} finally {
					bos.close();
					is.close();
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		return resultBody;
	}
	
	/**
	 * 从请求中获取变量
	 */
	public static String getParameter(HttpServletRequest request, String key) {
		String res = request.getParameter(key);
		
		if (StringUtils.isEmpty(res)) {
			res = request.getHeader(key);
			
			if (StringUtils.isEmpty(res)) {
				Cookie[] cookies = request.getCookies();
				
				if (cookies != null) {
					for (Cookie c : cookies) {
						if  (c.getName().equals(key)) {
							res = c.getValue();
							break;
						}
					}
				}
			}
		}
		
		return res;
	}
	
	/**
	 * 
	 * 将请求中带的所有变量统一到一个对象中
	 *
	 * @param request
	 * @param cacheManager
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static JSONObject getAllParameter(HttpServletRequest request, CacheManager cacheManager) throws UnsupportedEncodingException {
		byte[] body = cacheManager.getThreadVar("$body", byte[].class);
		JSONObject allParams = cacheManager.getThreadVar("$params", JSONObject.class);
		
		/**
		 * 整合所有可用参数
		 */
		if (allParams == null) {
			Map<String, String[]> params = request.getParameterMap();
			
			if (body != null) {
				String bs = new String(body, "UTF-8");
				
				if (JSON.isValidObject(bs)) {
					allParams = JSON.parseObject(bs);
				} else {
					allParams = new JSONObject();
				}
			} else {
				allParams = new JSONObject();
			}
			
			for (Entry<String, String[]> p : params.entrySet()) {
				if (p.getValue().length > 1) {
					allParams.put(p.getKey(), p.getValue());
				} else {
					allParams.put(p.getKey(), p.getValue()[0]);
				}
			}
			
			cacheManager.addThreadVar("$params", allParams);
		}
		
		return allParams;
	}
}
