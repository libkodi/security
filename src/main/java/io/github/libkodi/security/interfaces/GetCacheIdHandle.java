package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;

public interface GetCacheIdHandle {
	public String call(HttpServletRequest request, String key);
}
