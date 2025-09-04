package org.wind.mvc.bean;

import java.util.List;
import java.util.Map;

/**
 * @描述 : XML的节点信息
 * @作者 : 胡璐璐
 * @时间 : 2020年9月4日 17:18:09
 */
public class XMLNode {

	private String name;		//节点名（标签名）
	private String text;		//节点文本内容（如果有childNodeListMap子节点，这里为空）
	private Map<String,String> attributeMap;		//属性Map（key=属性名；value=属性值）
	//
	private Map<String,List<XMLNode>> childNodeListMap;		//子节点列表
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Map<String, String> getAttributeMap() {
		return attributeMap;
	}
	public void setAttributeMap(Map<String, String> attributeMap) {
		this.attributeMap = attributeMap;
	}
	public Map<String, List<XMLNode>> getChildNodeListMap() {
		return childNodeListMap;
	}
	public void setChildNodeListMap(Map<String, List<XMLNode>> childNodeListMap) {
		this.childNodeListMap = childNodeListMap;
	}
}