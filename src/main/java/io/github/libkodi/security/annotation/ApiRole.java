package io.github.libkodi.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.libkodi.security.variables.ApiCheckMode;

/**
 * 判断是否有指定角色权限
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiRole {
	String[] value();
	String mode() default ApiCheckMode.OR;
}
