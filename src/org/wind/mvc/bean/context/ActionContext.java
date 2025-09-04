package org.wind.mvc.bean.context;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @描述 : 操作上下文（请求处理上下文）
 * @作者 : 胡璐璐
 * @时间 : 2020年9月9日 16:18:22
 */
public class ActionContext {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;
	//
	private Object controller;		//被执行的控制器
	private Method method;		//被执行的控制器方法
	
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public HttpServletResponse getResponse() {
		return response;
	}
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	public HttpSession getSession() {
		return session;
	}
	public void setSession(HttpSession session) {
		this.session = session;
	}
	public Object getController() {
		return controller;
	}
	public void setController(Object controller) {
		this.controller = controller;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	
}
