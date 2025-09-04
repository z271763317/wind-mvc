package org.wind.mvc.init;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.annotation.controller.An_URL;
import org.wind.mvc.annotation.controller.An_URLParam;
import org.wind.mvc.annotation.interceptor.An_Interceptor;
import org.wind.mvc.bean.ClassInfo;
import org.wind.mvc.bean.MethodParameter;
import org.wind.mvc.bean.URLMapping;
import org.wind.mvc.bean.URLMapping_dynamic;
import org.wind.mvc.interceptor.Interceptor;
import org.wind.mvc.util.MethodParameterUtil;
import org.wind.mvc.util._FileOS;
import org.wind.mvc.util._RegexUtil;
import org.wind.mvc.util._ObjectUtil;
import org.wind.mvc.util._ToolUtil;

/**
 * @描述 : 初始化_扫描器
 * @详情 : 扫描url映射和实例化：控制器、拦截器、Class信息等。
 * @作者 : 胡璐璐
 * @时间 : 2020年8月29日 11:27:55
 */
@SuppressWarnings("unchecked")
public class Init_scan extends Init{

	/************匹配规则************/
	//匹配规则：精确—>扩展名—>路径（带通配符）—>任意URL
	public static final Map<String,URLMapping> umMap=new HashMap<String,URLMapping>();		//精确（key=相对URL）
	public static final Map<String,URLMapping_dynamic> umMap_dynamic=new HashMap<String,URLMapping_dynamic>();		//动态路径（key=前缀UR）
	
	/************Class的信息************/
	public static final Map<Class<?>,ClassInfo> classInfoMap=new HashMap<Class<?>,ClassInfo>();
	
	/***********成员变量***********/
	private Map<Class<?>,String> classUrlMap=new HashMap<Class<?>,String>();		//Class对应的Url（前缀）
	
	/**构造方法**/
	public Init_scan() {
		
	}
	
	/**初始化**/
	public void init() throws Exception{
		/**扫描**/
		try {
//			String projectPath=_FileOS.getProjectPath("/WEB-INF/classes/", 0);
			String projectPath=_FileOS.getProjectPath(null, 0);
			List<List<Class<?>>> allClassList=new ArrayList<List<Class<?>>>();
			/*控制器*/
	    	StringBuffer controllerPath=new StringBuffer(projectPath);
	    	String scan_controller=Init_config.frameworkConfig.scan_controller();		//扫描的控制器目录
	    	if(scan_controller!=null) {
	    		controllerPath.append(scan_controller.trim().replace(".", "/")).append("/");
	    	}
	    	List<Class<?>> controllerClassList=_FileOS.getAllClassList(controllerPath.toString(), scan_controller);
	    	allClassList.add(controllerClassList);
	    	/*拦截器*/
	    	String scan_interceptor=Init_config.frameworkConfig.scan_interceptor();		//扫描的拦截器目录
	    	if(scan_interceptor!=null && scan_interceptor.trim().length()>0) {
	    		StringBuffer interceptorPath=new StringBuffer(projectPath);
	    		interceptorPath.append(scan_interceptor.trim().replace(".", "/")).append("/");
	    		List<Class<?>> interceptorClassList=_FileOS.getAllClassList(interceptorPath.toString(), scan_interceptor);
	    		allClassList.add(interceptorClassList);
	    	}
	    	//
	    	TreeMap<Integer,Set<Class<Interceptor>>> interceptorSetMap=new TreeMap<Integer,Set<Class<Interceptor>>>();
	    	for(List<Class<?>> t_classList:allClassList) {
	    		for(Class<?> t_class:t_classList) {
		    		/*控制器*/
		    		if(t_class.isAnnotationPresent(An_Controller.class)) {
		    			this.URLMapping((Class<An_Controller>) t_class);
		    		/*拦截器*/
		    		}else if(t_class.isAnnotationPresent(An_Interceptor.class) && Interceptor.class.isAssignableFrom(t_class)) {
		    			An_Interceptor t_an_interceptoe=t_class.getAnnotation(An_Interceptor.class);
		    			int t_order=t_an_interceptoe.order();
		    			Set<Class<Interceptor>> t_set=interceptorSetMap.get(t_order);
		    			if(t_set==null) {
		    				t_set=new HashSet<Class<Interceptor>>();
		    				interceptorSetMap.put(t_order, t_set);
		    			}
		    			t_set.add((Class<Interceptor>) t_class);
		    		}
		    	}
	    	}
	    	/*拦截器处理*/
	    	this.interceptor(interceptorSetMap);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	/********************URL映射器********************/
	//解析URL映射器
	private void URLMapping(Class<An_Controller> controllerClass) throws Exception, IllegalAccessException {
		//允许扫描
		if(controllerClass.getAnnotation(An_Controller.class).isScan()) {
			String urlPrefix=this.getUrlPrefix(controllerClass);	//前缀URL
			/*url（后缀，方法）*/
			if(urlPrefix.length()>0) {
				Method methodArr[]=_ObjectUtil.getMethod(controllerClass,Object.class);
				Set<String> existUrlSet=new HashSet<String>();		//已存在的url
				for(Method method:methodArr) {
					An_URL t_an_url=method.getAnnotation(An_URL.class);
					if(t_an_url!=null) {
						String t_urlList[]=t_an_url.value();
						for(String t_url:t_urlList) {
							//没有被设置过
		    				if(!existUrlSet.contains(t_url)) {
								Map<String,MethodParameter> mpMap=MethodParameterUtil.getMethodParam_Map(method);
								//有后缀
								if(t_url.length()>0) {
				    				String t_urlArr[]=t_url.split("/");
				    				String variable=null;		//动态变量名
				    				int end=-1;
				    				for(int i=t_urlArr.length-1;i>=0;i--) {
				    					String t_item=t_urlArr[i].trim();
				    					//当前段不是空字符串
				    					if(t_item.length()>0) {
			    							if(t_item.matches("\\{[a-zA-Z_]{1}[a-zA-Z\\d_]*\\}")) {
			    								variable=_RegexUtil.getCull(t_item, "{", "}");
			    								end=i;
			    							}
			    							break;
				    					}
				    				}
				    				if(end==-1) {
				    					end=t_urlArr.length;
				    				}
				    				StringBuffer urlSuffix=new StringBuffer();	//后缀URL
				    				for(int i=0;i<end;i++) {
				    					String t_item=t_urlArr[i].trim();
				    					//当前段不是空字符串
				    					if(t_item.length()>0) {
				    						urlSuffix.append("/").append(t_item);
				    					}
				    				}
				    				//
				    				String url_complete=urlPrefix+urlSuffix.toString();	//完整相对URL
				    				//有通配符
				    				if(variable!=null) {
				    					umMap_dynamic.put(url_complete, new URLMapping_dynamic(url_complete, variable,controllerClass, method,mpMap));
				    				}else{
				    					umMap.put(url_complete, new URLMapping(url_complete, controllerClass, method,mpMap));
				    				}
			    				//无后缀，代表前缀就是一个请求URL（可覆盖）
				    			}else{
				    				umMap.put(urlPrefix, new URLMapping(urlPrefix,controllerClass, method,mpMap));
				    			}
								/*通用处理——方法参数*/
								this.methodParam(method, mpMap);
								existUrlSet.add(t_url);
		    				}
						}
					}
				}
			}
		}
	}
	//获取 : 指定控制器Class的URL前缀
	private String getUrlPrefix(Class<An_Controller> controllerClass) {
		An_Controller t_an_controller=controllerClass.getAnnotation(An_Controller.class);
		StringBuffer urlPrefix=new StringBuffer();		//前缀URL（当前类）
		//没有解析
		if(this.classUrlMap.get(controllerClass)==null) {
			/*url（前缀，控制器）*/
			String url=t_an_controller.value();		//相对url前缀
			String urlArr[]=url.split("/");
			for(int i=0;i<urlArr.length;i++) {
				String t_item=urlArr[i].trim();
				//当前段不是空字符串
				if(t_item.length()>0) {
					urlPrefix.append("/").append(t_item);
				}
			}
			if(urlPrefix.length()>0) {
				//继承父类的信息
				boolean isInherit=t_an_controller.isInherit();		//是否继承父类的URL前缀
				if(isInherit) {
					Class<?> fatherClass=controllerClass.getSuperclass();
					if(fatherClass!=null && fatherClass.isAnnotationPresent(An_Controller.class)) {
						String fatherUrl=this.getUrlPrefix((Class<An_Controller>) fatherClass);
						if(fatherUrl.length()>0) {
							urlPrefix.insert(0, fatherUrl);
						}
					}
				}
				this.classUrlMap.put(controllerClass, urlPrefix.toString());			//每个控制器Class对应的URL前缀
			}
		//已解析过
		}else{
			urlPrefix.append(this.classUrlMap.get(controllerClass));
		}
		return urlPrefix.toString();
	}
	//通用处理：方法参数（将参数信息设置到对应的MethodParameter对象里）
	private void methodParam(Method method,Map<String,MethodParameter> mpMap) {
		Annotation paramAnArr[][]=method.getParameterAnnotations();
		Class<?> paramClassArr[]=method.getParameterTypes();
		Type paramTypeArr[]=method.getGenericParameterTypes();
		for(MethodParameter t_mp:mpMap.values()) {
			int index=t_mp.getIndex();
			Class<?> typeClass=paramClassArr[index];
			t_mp.setType(typeClass);
			//主类：自定义类
			this.generateClassInfo(typeClass);
			
			//泛型
			Type t_type=paramTypeArr[index];
			Class<?> genericClass=null;
			if(t_type instanceof ParameterizedType){
            	 ParameterizedType pt = (ParameterizedType) t_type;  
            	 Type tArr[]=pt.getActualTypeArguments();
            	 Type type=tArr[0];
            	 genericClass=(Class<?>)type;
            	 t_mp.setGenericClass(genericClass);
            	 //泛型：自定义类
				this.generateClassInfo(genericClass);
            }
			
			//注解
			Annotation t_anArr[]=paramAnArr[index];
			for(Annotation t_an:t_anArr) {
				if(t_an.annotationType()==An_URLParam.class) {
					t_mp.setAnUrlParam((An_URLParam) t_an);
					break;
				}
			}
		}
	}
	
	/********************URL拦截器********************/
	//拦截器处理
	private void interceptor(TreeMap<Integer,Set<Class<Interceptor>>> interceptorSetMap) {
		for(Entry<Integer, Set<Class<Interceptor>>> entry:interceptorSetMap.entrySet()) {
    		Set<Class<Interceptor>> t_set=entry.getValue();
    		for(Class<Interceptor> t_class:t_set) {
    			An_Interceptor t_an_interceptoe=t_class.getAnnotation(An_Interceptor.class);
    			String t_urlArr[]=t_an_interceptoe.value();
    			for(String t_bindUrl:t_urlArr) {
    				//全局通配符（绑定所有URL映射的控制器方法）
    				if(t_bindUrl.matches("/\\*+")) {
    					/*普通url映射器*/
    					this.interceptorToURLMapping(t_class, umMap, null,true);
						/*动态url映射器*/
    					this.interceptorToURLMapping(t_class, umMap_dynamic, null,true);
    				}else{
	    				String bindUrlArr[]=t_bindUrl.split("/");
	    				StringBuffer t_url_standard=new StringBuffer();
	    				boolean isWildcard=false;		//是否带通配符（带“*”号就是）
						for(int i=0;i<bindUrlArr.length;i++) {
							String t_item=bindUrlArr[i].trim();
							//当前段不是空字符串
							if(t_item.length()>0) {
								//通配符（不在往后判断）
								if(t_item.matches("\\*+")) {
									isWildcard=true;
									break;
								}else{
									t_url_standard.append("/").append(t_item);
								}
							}
						}
						//有指定的URL
						if(t_url_standard.length()>0) {
							/*普通url映射器*/
							this.interceptorToURLMapping(t_class, umMap, t_url_standard.toString(),isWildcard);
							/*动态url映射器*/
							this.interceptorToURLMapping(t_class, umMap_dynamic, t_url_standard.toString(),isWildcard);
						}
    				}
    			}
    		}
    	}
	}
	//拦截器—>（绑定）到对应的url映射器（isWildcard=是否通配符匹配，优先判断url是否为空，若为空，则不需要考虑isWildcard）
	private void interceptorToURLMapping(Class<?> controllerClass,Map<String,? extends URLMapping> map,String url,boolean isWildcard) {
		if(url!=null) {
			if(isWildcard) {
				for(Entry<String, ? extends URLMapping> entry:map.entrySet()) {
					String t_key=entry.getKey();
					URLMapping t_value=entry.getValue();
					if(t_key.equals(url) || t_key.matches(url+".+")) {
						this.interceptorToURLMapping(controllerClass, t_value);
					}
				}
			}else{
				this.interceptorToURLMapping(controllerClass, map.get(url));
			}
		//没有指定绑定的url，则绑定所有控制器方法
		}else{
			for(URLMapping t_urlMapping:map.values()) {
				this.interceptorToURLMapping(controllerClass, t_urlMapping);
			}
		}
	}
	//拦截器—>（绑定）到对应的url映射器（指定的单个url映射器）
	private void interceptorToURLMapping(Class<?> controllerClass,URLMapping t_urlMapping) {
		if(t_urlMapping!=null) {
			List<Class<Interceptor>> t_interceptorList=t_urlMapping.getInterceptorChain();
			if(t_interceptorList==null) {
				t_interceptorList=new ArrayList<Class<Interceptor>>();
				t_urlMapping.setInterceptorChain(t_interceptorList);
			}
			t_interceptorList.add((Class<Interceptor>) controllerClass);
		}
	}
	
	/********************其他********************/
	//生成 : 指定Class的Classs信息对象（包含指定class内字段所属的自定义类的Class信息，深层次到底，若有循环依赖则不生成）
	private void generateClassInfo(Class<?> clazz) {
		//自定义类
		if(clazz!=null && _ToolUtil.isCustomClass(clazz) && !classInfoMap.containsKey(clazz)) {
			ClassInfo t_classInfo=new ClassInfo(clazz);
			classInfoMap.put(clazz, t_classInfo);
			for(Field t_field:t_classInfo.getFieldMap().values()) {
				this.generateClassInfo(t_field.getType());
			}
		}
	}
}
