package io.github.libkodi.security.variables;

/**
 * 
 * @author solitpine
 * @description 验证角色与许可权限时的判断模式 
 *
 */
public @interface ApiCheckMode {
	public static String AND = "and";
	public static String OR = "or";
}
