package io.github.libkodi.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.libkodi.security.variables.ApiCheckMode;

/**
 * 
 * @author solitpine
 * @description 方法注解，判断是否有指定许可权限
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiPermission {
	String[] value();
	String mode() default ApiCheckMode.AND;
}
