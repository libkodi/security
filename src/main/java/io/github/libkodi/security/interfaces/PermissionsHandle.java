package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;

import io.github.libkodi.security.SecurityManager;

public interface PermissionsHandle {
	public boolean call(SecurityManager context, HttpServletRequest request, String[] values, String mode);
}
