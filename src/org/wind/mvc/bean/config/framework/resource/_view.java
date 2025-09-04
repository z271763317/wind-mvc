package org.wind.mvc.bean.config.framework.resource;

import org.wind.mvc.bean.config.XMLConfig;
import org.wind.mvc.bean.config.framework.resource.view._prefix;
import org.wind.mvc.bean.config.framework.resource.view._suffix;

/**
 * @描述 : 资源_视图
 * @作者 : 胡璐璐
 * @时间 : 2020年9月5日 07:40:03
 */
public class _view extends XMLConfig{

	private _prefix prefix;		//前缀
	private _suffix suffix;		//后缀
	
	public _prefix getPrefix() {
		return prefix;
	}
	public void setPrefix(_prefix prefix) {
		this.prefix = prefix;
	}
	public _suffix getSuffix() {
		return suffix;
	}
	public void setSuffix(_suffix suffix) {
		this.suffix = suffix;
	}
		
}