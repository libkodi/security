package io.github.libkodi.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author solitpine
 * @description 参数注解，用来获取请求参数(包含json形式的主体参数) 
 *
 */
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {
	boolean required() default true;
	String name();
	String defaultValue() default "";
}
