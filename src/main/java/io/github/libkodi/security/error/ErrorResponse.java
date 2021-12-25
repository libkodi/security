package io.github.libkodi.security.error;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

import lombok.Data;

@Data
public class ErrorResponse {
	private int status;
	private String error;
	private String path;
	private Date timestamp;
	
	public ErrorResponse(int status, String error) {
		timestamp = new Date();
		this.status = status;
		this.error = error;
	}
	
	public Object sync(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(status);
		this.path = request.getRequestURI();
		
		ServletOutputStream os = null;
		
		try {
			os = response.getOutputStream();
			os.write(JSON.toJSONString(this).getBytes());
			
		} catch (Exception e) {} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {}
			}
		}
		
		return null;
	}
}
