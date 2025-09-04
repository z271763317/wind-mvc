package org.wind.mvc.bean.config.framework;

import org.wind.mvc.bean.config.XMLConfig;
import org.wind.mvc.bean.config.framework.resource._view;

/**
 * @描述 : 资源
 * @作者 : 胡璐璐
 * @时间 : 2020年9月5日 07:40:03
 */
public class _resource extends XMLConfig{

	private _view view;		//视图

	public _view getView() {
		return view;
	}
	public void setView(_view view) {
		this.view = view;
	}
	
}
