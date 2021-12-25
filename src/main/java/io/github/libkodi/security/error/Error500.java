package io.github.libkodi.security.error;

public class Error500 extends ErrorResponse {

	public Error500(String error) {
		super(500, error);
	}

}
