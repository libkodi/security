package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ExceptionHandle {
	public Object call(HttpServletRequest request, HttpServletResponse response, Throwable error);
}
