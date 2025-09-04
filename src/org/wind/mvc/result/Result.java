package org.wind.mvc.result;

/**
 * @描述 : 返回响应类——执行控制器方法后返回的数据（含：页面视图路径等）
 * @作者 : 胡璐璐
 * @时间 : 2020年9月1日 15:30:37
 */
public class Result {
	
	private Object methodResult;		//方法返回对象
	/**
	 * 返回的路径（一般是本项目的视图相对路径，会加上配置的前缀和后缀）。有以下几种情况将做不同的处理（不会加上前缀和后缀）。<br/>
	 * 前缀为“/”:——转发到其他的URL控制器，或其他路径下的视图页面<br/>
	 * 前缀为“redirect:”——重定向到其他的URL。可以是本项目的相对路径，也可以是全路径，如：redirect:/test/kzq2、redirect:http://www.tcin.cn/<br/>
	 */
	private String url;
	private String data;		//响应数据（如：html，json格式等）

	public Result() {
		
	}
	public Result(String url,String data) {
		this.url=url;
		this.data=data;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Object getMethodResult() {
		return methodResult;
	}
	public void setMethodResult(Object methodResult) {
		this.methodResult = methodResult;
	}
	
}
