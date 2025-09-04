package org.wind.mvc.bean.config.framework;

import org.wind.mvc.bean.config.XMLConfig;
import org.wind.mvc.bean.config.framework.scan._controller;
import org.wind.mvc.bean.config.framework.scan._interceptor;

/**
 * @描述 : 扫描器
 * @作者 : 胡璐璐
 * @时间 : 2020年9月5日 07:40:03
 */
public class _scan extends XMLConfig{

	private _controller controller;		//控制器
	private _interceptor interceptor;		//拦截器
	
	public _controller getController() {
		return controller;
	}
	public void setController(_controller controller) {
		this.controller = controller;
	}
	public _interceptor getInterceptor() {
		return interceptor;
	}
	public void setInterceptor(_interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
}
