package io.github.libkodi.security.error;

public class Error401 extends ErrorResponse {

	public Error401(String error) {
		super(401, error);
	}

}
