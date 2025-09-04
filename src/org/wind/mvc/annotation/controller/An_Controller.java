package org.wind.mvc.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 控制器注解——代表该类是控制器。value()是映射请求url的前缀（主目录）
 * @作者 : 胡璐璐
 * @时间 : 2020年8月3日 09:04:55
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface An_Controller {

	String name() default "";		//名称
	String value();		//请求URL的前缀（主目录，如：/test、/test/wind/等）
	String viewPrefix() default "";		//返回视图（页面）相对路径的前缀（默认：取value()的值）,完整的相对路径需要拼上后缀，也就是注解当前控制器类的方法返回的字符串值
	boolean isInherit() default true;		//是否继承父类（含深层次父类）的该注解的信息（如：value()、viewPrefix()等）
	boolean isScan() default true;		//是否允许被扫描处理
	Class<?> bind() default Object.class;		//绑定一个Class（可做扩展用）
	
}