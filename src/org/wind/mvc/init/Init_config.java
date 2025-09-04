package org.wind.mvc.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wind.mvc.bean.XMLNode;
import org.wind.mvc.bean.config.FrameworkConfig;
import org.wind.mvc.bean.config.XMLConfig;
import org.wind.mvc.util._ObjectUtil;

/**
 * @描述 : 初始化_配置
 * @作者 : 胡璐璐
 * @时间 : 2020年9月4日 13:01:03
 */
@SuppressWarnings("unchecked")
public class Init_config extends Init{

	public static FrameworkConfig frameworkConfig;	//框架配置
	
	/**初始化**/
	public void init() throws Exception{
		/**配置文件**/
		XMLNode xmlNode=getConfigObject();
		if(xmlNode!=null) {
			Map<String,List<XMLNode>> childNodeListMap=xmlNode.getChildNodeListMap();
			//
			frameworkConfig=getNodeObject(FrameworkConfig.class, childNodeListMap);
		}
	}
	//获取（生成） : 节点对应的bean结构性对象
	private <T extends XMLConfig> T getNodeObject(Class<T> clazz,Map<String,List<XMLNode>> childNodeListMap) throws Exception{
		XMLConfig obj=clazz.newInstance();
		Field fieldArr[]=_ObjectUtil.getField(clazz, Object.class);
		Field.setAccessible(fieldArr, true);		//取消安全限制
		for(Field t_field:fieldArr) {
			String t_fieldName=t_field.getName();		//变量名（标签名）
			Class<?> genericClass=_ObjectUtil.getGeneric(t_field);
			//多节点（一对多）
			if(genericClass!=null && XMLConfig.class.isAssignableFrom(genericClass)) {
				List<XMLNode> t_nodeList=getXMLNodeList(childNodeListMap, t_fieldName);
				List<XMLConfig> t_objList=new ArrayList<XMLConfig>();
				if(t_nodeList!=null && t_nodeList.size()>0) {
					for(XMLNode t_node:t_nodeList) {
						XMLConfig t_obj=getNodeObject((Class<XMLConfig>)genericClass, t_node.getChildNodeListMap());
						t_objList.add(t_obj);
					}
				}
				t_field.set(obj, t_objList);	//设置
			//单节点
			}else{
				XMLNode t_node=getXMLNode(childNodeListMap, t_fieldName);		//scan
				if(t_node!=null) {
					Class<XMLConfig> t_class=(Class<XMLConfig>) t_field.getType();
					Map<String,List<XMLNode>> t_childNodeListMap=t_node.getChildNodeListMap();
					XMLConfig t_obj=null;
					//子节点
					if(t_childNodeListMap.size()>0) {
						t_obj=getNodeObject(t_class, t_childNodeListMap);
					}else{
						t_obj=t_class.newInstance();
						t_obj.setText(t_node.getText());
					}
					t_field.set(obj, t_obj);	//设置
				}
			}
		}
		return (T) obj;
	}
	
	//获取配置对象
	private XMLNode getConfigObject(){
		String configFilePath="wind-mvc.xml";		//配置文件路径
		try{
			// step 1: 获得dom解析器工厂（工作的作用是用于创建具体的解析器）   
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
	        // step 2:获得具体的dom解析器   
	        DocumentBuilder db = dbf.newDocumentBuilder();   
	        URL fileURL=Thread.currentThread().getContextClassLoader().getResource(configFilePath);
	        if(fileURL==null) {
	        	throw new FileNotFoundException("缺少【wind-mvc.xml】文件");
	        }
	        String fielPath=java.net.URLDecoder.decode(fileURL.getFile(),"UTF-8");
	        // step3: 解析一个xml文档，获得Document对象（根结点）   
	        Document document = db.parse(new File(fielPath)); 
	        NodeList list = document.getElementsByTagName("wind-mvc");		//主节点（唯一）
	        if(list!=null && list.getLength()>0) {
	        	Node node = list.item(0);
	        	//元素
	        	if(node.getNodeType()==Node.ELEMENT_NODE){
	        		return getNodeDataMap((Element)node);
	        	}
	        }
		}catch(Exception e){
			throw new RuntimeException("wind-mvc配置错误："+e.getMessage(),e);
		}
		return null;
	}
	//获取节点数据Map
	private XMLNode getNodeDataMap(Element node) {
		XMLNode xmlNodeObj=new XMLNode();
		Map<String,List<XMLNode>> childXMLNodeListMap=new LinkedHashMap<String,List<XMLNode>>();
		Map<String,String> attributesMap=new HashMap<String,String>(); 
		//
		NodeList childNodeList=node.getChildNodes();
		//子节点
		if(childNodeList!=null && childNodeList.getLength()>0) {
			for(int j=0;j<childNodeList.getLength();j++){
	          	Node t_child_node=childNodeList.item(j);
	          	//元素
	        	if(t_child_node.getNodeType()==Node.ELEMENT_NODE){
	        		XMLNode childXMLNode=getNodeDataMap((Element)t_child_node);
	        		String child_tagName= t_child_node.getNodeName();		//节点名（标签名）
	        		List<XMLNode> list=childXMLNodeListMap.get(child_tagName);
	        		if(list==null) {
	        			list=new ArrayList<XMLNode>();
	        			childXMLNodeListMap.put(child_tagName, list);
	        		}
	        		list.add(childXMLNode);
	        	}
	        }
		}else{
			NamedNodeMap attributesNodeMap=node.getAttributes();		//属性节点Map
			for(int k=0;attributesNodeMap!=null && k<attributesNodeMap.getLength();k++){
	          	Node t_child_node=attributesNodeMap.item(k);
	          	String t_key= node.getNodeName();		//属性名
	          	String t_value=t_child_node.getNodeValue();
	          	//
	          	attributesMap.put(t_key, t_value);
	        }
		}
		String tagName= node.getNodeName();		//节点名（标签名）
		//
		xmlNodeObj.setName(tagName);
		if(childXMLNodeListMap==null || childXMLNodeListMap.size()<=0) {
			xmlNodeObj.setText(node.getTextContent());	//文本内容
		}
		xmlNodeObj.setChildNodeListMap(childXMLNodeListMap);
		xmlNodeObj.setAttributeMap(attributesMap);
		return xmlNodeObj;
	}
	
	//获取 : map里的list
	private List<XMLNode> getXMLNodeList(Map<String,List<XMLNode>> childNodeListMap,String tagName) {
		return childNodeListMap.get(tagName);
	}
	//获取 : map里的list的第1个节点
	private XMLNode getXMLNode(Map<String,List<XMLNode>> childNodeListMap,String tagName) {
		List<XMLNode> scanList=getXMLNodeList(childNodeListMap, tagName);
		if(scanList!=null && scanList.size()>0) {
			return scanList.get(0);
		}
		return null;
	}
	
}