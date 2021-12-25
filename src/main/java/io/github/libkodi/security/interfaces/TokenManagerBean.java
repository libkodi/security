package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;

public interface TokenManagerBean {
	public String create() throws Exception;
	public boolean hasToken(HttpServletRequest request);
	public Cache getCache(HttpServletRequest request);
}
