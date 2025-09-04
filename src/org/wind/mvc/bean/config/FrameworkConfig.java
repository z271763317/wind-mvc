package org.wind.mvc.bean.config;

import org.wind.mvc.bean.config.framework._resource;
import org.wind.mvc.bean.config.framework._scan;
import org.wind.mvc.bean.config.framework.resource._view;
import org.wind.mvc.bean.config.framework.resource.view._prefix;
import org.wind.mvc.bean.config.framework.resource.view._suffix;
import org.wind.mvc.bean.config.framework.scan._controller;
import org.wind.mvc.bean.config.framework.scan._interceptor;

/**
 * @描述 : 框架配置
 * @作者 : 胡璐璐
 * @时间 : 2020年9月5日 07:40:03
 */
public class FrameworkConfig extends XMLConfig{

	private _scan scan;		//扫描器
	private _resource resource;		//资源
	
	public _scan getScan() {
		return scan;
	}
	public void setScan(_scan scan) {
		this.scan = scan;
	}
	public _resource getResource() {
		return resource;
	}
	public void setResource(_resource resource) {
		this.resource = resource;
	}
	
	/****************获取指定配置区****************/
	/**scan**/
	public String scan_controller() {
		if(this.scan!=null) {
			_controller controller=this.scan.getController();
			if(controller!=null) {
				return controller.getText();
			}
		}
		return null;
	}
	public String scan_interceptor() {
		if(this.scan!=null) {
			_interceptor interceptor=this.scan.getInterceptor();
			if(interceptor!=null) {
				return interceptor.getText();
			}
		}
		return null;
	}
	/**resource**/
	public String resource_view_prefix() {
		if(this.resource!=null) {
			_view view=this.resource.getView();
			if(view!=null) {
				_prefix prefix=view.getPrefix();
				if(prefix!=null) {
					return prefix.getText();
				}
			}
		}
		return null;
	}
	public String resource_view_suffix() {
		if(this.resource!=null) {
			_view view=this.resource.getView();
			if(view!=null) {
				_suffix suffix=view.getSuffix();
				if(suffix!=null) {
					return suffix.getText();
				}
			}
		}
		return null;
	}
}
