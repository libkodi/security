package io.github.libkodi.security.interceptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import io.github.libkodi.security.CacheManager;
import io.github.libkodi.security.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author solitpine
 * @description 请求对象容器，实现body的重复利用 
 *
 */
@Slf4j
public class WebHttpServletRequestWarpper extends HttpServletRequestWrapper {
	private final byte[] body;
	
	public WebHttpServletRequestWarpper(HttpServletRequest request, CacheManager cacheManager) {
		super(request);
		
		body = HttpRequestUtils.getBody(request); // 读取body并保存
		cacheManager.put("$body", body);
	}
	
	public byte[] getBody() {
		return body;
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}
	
	/**
	 * 重写getInputStream
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream bis = new ByteArrayInputStream(body);
		
		/**
		 * 返回一个读取流，并在读取完数据后自动关闭流
		 */
		return new ServletInputStream() {
			private boolean closed = false;
			
			@Override
			public int read() throws IOException {
				int c = bis.read();
				
				if (c < 0 && !closed) {
					close();
				}
				
				return c;
			}
			
			@Override
			public int read(byte[] b) throws IOException {
				int len = bis.read(b);
				
				if (len < 1) {
					close();
				}
				
				return len;
			}
			
			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				int l = bis.read(b, off, len);
				
				if (l < 1) {
					close();
				}
				
				return l;
			}
			
			/**
			 * 关闭读取流
			 */
			@Override
			public void close() throws IOException {
				try {
					bis.close();
					closed = true;
				} catch (Exception e) {
					log.error("", e);
				}
			}
			
			@Override
			public synchronized void reset() throws IOException {
				throw new IOException("Resetting the read stream is not supported");
			}
			
			@Override
			public boolean markSupported() {
				return false;
			}
			
			@Override
			public void setReadListener(ReadListener listener) {
				
			}
			
			@Override
			public boolean isReady() {
				return true;
			}
			
			@Override
			public boolean isFinished() {
				return closed;
			}
		};
	}
}
