package io.github.libkodi.security.interfaces;

import javax.servlet.http.HttpServletRequest;

public interface GetRemoteAddressHandle {
	public String call(HttpServletRequest request);
}
