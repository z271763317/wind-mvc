package org.wind.mvc.annotation.controller;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.wind.mvc.bean.URLMethod;

/**
 * @描述 : 请求URL注解——当前类控制器下的请求URL（相对路径）
 * @说明 : 一个完整的相对请求URL，会拼上当前控制器类的url前缀（@An_Controller注解的value()）。如：当前类的@An_Controller的value()为“/test”，注解该方法的value()为"wind"（或/wind），则完整的相对URL为：/test/wind <br />
 * 				该注解支持通配符“*”，代表匹配后面所有的字符，直到遇到下一个“/”为止
 * @作者 : 胡璐璐
 * @时间 : 2020年8月3日 09:04:55
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface An_URL {

	/**名称**/
	String name() default "";
	/**请求URL（如：/test、/test/wind/等。默认为空，代表直接使用：{@link An_Controller}注解的value()）**/
	String[] value() default "";		
	/**支持的请求方法（默认：支持所有。取自：{@link URLMethod}的值）**/
	String[] method() default {};
	
}