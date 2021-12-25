package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;

import io.github.libkodi.security.SecurityManager;

public interface AuthFilterHandle {
	public boolean call(SecurityManager context, HttpServletRequest request);
}
