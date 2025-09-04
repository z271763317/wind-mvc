package org.wind.mvc.bean;

import org.wind.mvc.annotation.controller.An_URLParam;

/**
 * @描述 : 方法参数信息
 * @作者 : 胡璐璐
 * @时间 : 2020年8月7日 22:42:33
 */
public class MethodParameter {

	private int index;		//参数所在的方法的顺序
	private String name;		//参数名（参数变量名）
	private Class<?> type;		//参数所属的class
	private Class<?> genericClass;		//参数的泛型类型
	private An_URLParam anUrlParam;		//注解
	
	public MethodParameter(String name) {
		this.name=name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class<?> getType() {
		return type;
	}
	public void setType(Class<?> type) {
		this.type = type;
	}
	public An_URLParam getAnUrlParam() {
		return anUrlParam;
	}
	public void setAnUrlParam(An_URLParam anUrlParam) {
		this.anUrlParam = anUrlParam;
	}
	public Class<?> getGenericClass() {
		return genericClass;
	}
	public void setGenericClass(Class<?> genericClass) {
		this.genericClass = genericClass;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}

}
