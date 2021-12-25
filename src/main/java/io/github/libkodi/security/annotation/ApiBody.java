package io.github.libkodi.security.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 * @author solitpine
 * @description 参数注解，用来获取请求主体 
 *
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ApiBody {
	boolean required() default true;
}
