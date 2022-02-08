package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Api出错的接收句柄
 */
public interface ExceptionHandle {
	public Object call(HttpServletRequest request, HttpServletResponse response, Throwable error);
}
