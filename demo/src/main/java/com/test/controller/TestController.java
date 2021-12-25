package com.test.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.test.utils.ApiResult;

import io.github.libkodi.security.annotation.Api;
import io.github.libkodi.security.annotation.ApiCache;
import io.github.libkodi.security.factory.TokenManager;
import io.github.libkodi.security.interfaces.Cache;

@RestController
public class TestController {
	@Autowired(required = false)
	private TokenManager tokenManager;
	
	@Api
	@RequestMapping("/login")
	public ApiResult login(@RequestParam(name = "username") String username, @RequestParam(name = "password") String password, HttpServletResponse response, HttpServletRequest request) throws Exception {
		if (username.equals("admin") || username.equals("user")) {
			if (password.equals("123456")) {
				String token = tokenManager.create();
				Cache c = tokenManager.getCache();
				c.put("name", username);
				return new ApiResult(200, null, token);
			} else {
				return new ApiResult(403, "Invalid Password.", null);
			}
		} else {
			return new ApiResult(403, "Invalid Username.", null);
		}
	}
	
	@Api
	@RequestMapping("/v1/whoami")
	public ApiResult whoami() {
		Cache token = tokenManager.getCache();
		
		if (token == null) {
			return new ApiResult(403, "Invalid token", null);
		} else {
			return new ApiResult(200, null, token.get("name", String.class));
		}
	}
	
	@Api
	@RequestMapping("/session")
	public ApiResult getSession(@ApiCache(create = true) Cache session, HttpServletResponse response) {
		Object time = session.get("time");
		
		if (time == null) {
			time = System.currentTimeMillis();
			session.put("time", time);
		}
		
		return new ApiResult(200, null, time);
	}
}
