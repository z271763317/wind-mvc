package org.wind.mvc.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 请求URL参数注解
 * @作者 : 胡璐璐
 * @时间 : 2020年8月8日 10:51:40
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface An_URLParam {
	
	/**映射请求的参数名**/
	String value() default "";
	/**是否必须（需要自行拦截器实现）**/
	boolean isRequired() default false;
	
}