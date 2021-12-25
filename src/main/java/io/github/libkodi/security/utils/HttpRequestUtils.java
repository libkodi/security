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
	 * 
	 * @description 获取请求终端IP
	 * @author solitpine
	 * @date 6 Sep 2021
	 * @time 15:06:45
	 * @param request
	 * @return
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
	 *
	 * @description 读取请求主体
	 * @author solitpine
	 * @date 6 Sep 2021
	 * @time 15:07:08
	 * @param request
	 * @return
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
	 * @description 从请求中获取SessionId
	 * @author solitpine
	 * @date 6 Sep 2021
	 * @time 15:07:24
	 * @param request
	 * @return
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

	public static JSONObject getAllParameter(HttpServletRequest request, CacheManager cacheManager) throws UnsupportedEncodingException {
		byte[] body = cacheManager.get("$body", byte[].class);
		JSONObject allParams = cacheManager.get("$params", JSONObject.class);
		
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
			
			cacheManager.put("$params", allParams);
		}
		
		return allParams;
	}
}
