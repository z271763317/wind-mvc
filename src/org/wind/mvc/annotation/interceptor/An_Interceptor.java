package org.wind.mvc.annotation.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 拦截器注解——代理控制器的执行，先执行拦截器前部分，在执行控制器部分，后执行拦截器后部分
 * @作者 : 胡璐璐
 * @时间 : 2020年8月28日 13:44:33
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface An_Interceptor {

	String name() default "";		//名称
	String[] value();		//拦截的URL（可拦截多个）
	int order() default 1;		//顺序（多个拦截器的执行顺序，若顺序相同，则按照扫描到的顺序执行）
	
}