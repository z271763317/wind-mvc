package org.wind.mvc.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.wind.mvc.util._ObjectUtil;

/**
 * @描述 : 类信息（主要将Class原有的字段和方法等的存储结构转化下）
 * @详情 : 类的信息还包含所有深层次父类的信息（如：Field、Method）
 * @作者 : 胡璐璐
 * @时间 : 2020年9月18日 11:49:45
 */
public class ClassInfo {

	private Class<?> clazz;		//主类
	private Map<String,Field> fieldMap;	//key=字段名
	private Map<String,Method> methodMap;		//key=方法名+参数类型。全小写
	
	public ClassInfo(Class<?> clazz) {
		this.clazz=clazz;
		this.fieldMap=_ObjectUtil.getFieldMap(clazz, Object.class);
		this.methodMap=_ObjectUtil.getMethodMap(clazz, Object.class, "set");		//只需set方法
	}
	//
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public Map<String, Field> getFieldMap() {
		return fieldMap;
	}
	public void setFieldMap(Map<String, Field> fieldMap) {
		this.fieldMap = fieldMap;
	}
	public Map<String, Method> getMethodMap() {
		return methodMap;
	}
	public void setMethodMap(Map<String, Method> methodMap) {
		this.methodMap = methodMap;
	}
}
