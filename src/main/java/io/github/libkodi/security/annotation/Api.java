package io.github.libkodi.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 启用权限验证
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Api {

}
