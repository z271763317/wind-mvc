package org.wind.mvc.bean;

import java.lang.reflect.Method;
import java.util.Map;

import org.wind.mvc.annotation.controller.An_Controller;

/**
 * @描述 : 【动态路径】URL映射器——前缀URL，映射对应的：控制器+方法
 * @作者 : 胡璐璐
 * @时间 : 2020年8月3日 18:58:16
 */
public class URLMapping_dynamic extends URLMapping{

	private String variable;	//动态变量名（如：${test}里，test就是变量名）
	
	public URLMapping_dynamic(String url,String variable,Class<An_Controller> controller,Method method,Map<String,MethodParameter> mpMap) {
		super(url, controller, method, mpMap);
		this.variable=variable;
	}
	public String getVariable() {
		return variable;
	}
	public void setVariable(String variable) {
		this.variable = variable;
	}
	
}
