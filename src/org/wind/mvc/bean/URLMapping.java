package org.wind.mvc.bean;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.interceptor.Interceptor;

/**
 * @描述 : 【完全】URL映射器——相对URL映射对应的：控制器+方法
 * @作者 : 胡璐璐
 * @时间 : 2020年8月3日 18:58:16
 */
public class URLMapping {

	protected String url;		//url
	
	/**匹配到该处的控制器和方法**/
	protected Class<An_Controller> controller;		//控制器类
	protected Method method;		//方法
	protected Map<String,MethodParameter> mpMap;		//上面Method属性对应的参数信息（key=参数名称；value=参数class类型）
	protected List<Class<Interceptor>> interceptorChain;		//拦截器链（执行顺序按照拦截器对象设置的order执行，若有相同的顺序，则会按照最先扫描到的先执行）
	
	public URLMapping(String url,Class<An_Controller> controller,Method method,Map<String,MethodParameter> mpMap) {
		this.url=url;
		this.controller=controller;
		this.method=method;
		this.mpMap=mpMap;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Class<An_Controller> getController() {
		return controller;
	}
	public void setController(Class<An_Controller> controller) {
		this.controller = controller;
	}
	public Map<String, MethodParameter> getMpMap() {
		return mpMap;
	}
	public void setMpMap(Map<String, MethodParameter> mpMap) {
		this.mpMap = mpMap;
	}
	public List<Class<Interceptor>> getInterceptorChain() {
		return interceptorChain;
	}
	public void setInterceptorChain(List<Class<Interceptor>> interceptorChain) {
		this.interceptorChain = interceptorChain;
	}
	
}
