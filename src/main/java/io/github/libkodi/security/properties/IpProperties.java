package io.github.libkodi.security.properties;

import lombok.Data;

@Data
public class IpProperties {
	private boolean enable = false; // 是否开启请求次数限制
	private int limit = 100; // 指定时间内的请求最大数
	private int range = 60; // 计数器存活时间
	private int block = 300; // 到过次数后阻止访问的时间
}
