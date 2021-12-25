package com.test.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResult {
	private int code;
	private String msg;
	private Object data;
}
