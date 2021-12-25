package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletResponse;

import io.github.libkodi.security.SecurityManager;

public interface AuthFilterAfterHandle {
	public Object call(SecurityManager context, HttpServletResponse response, Object value);
}
