package org.wind.mvc.bean.config;

/**
 * @描述 : XML通用配置信息（配置类需要继承该类）
 * @作者 : 胡璐璐
 * @时间 : 2020年9月5日 08:04:38
 */
public abstract class XMLConfig {

	protected String text;		//文本内容

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
