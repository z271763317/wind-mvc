package org.wind.mvc.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @描述 : 响应类注解（如：返回给调用者的头部参数）
 * @详情 : 加在控制器方法的上
 * @作者 : 胡璐璐
 * @时间 : 2021年5月15日 00:53:03
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface An_Response {

	/**是否响应实体（对返回类型：String（字符串）的生效。表示返回的数据是响应的实体数据，如servlet输出的html、json、xml内容等）**/
	boolean isBody() default false;
	/**内容类型**/
	String contentType() default "";
	/**头部参数**/
	String[] head() default {};
	
}