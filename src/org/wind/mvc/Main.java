package org.wind.mvc;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.wind.mvc.annotation.controller.An_Controller;
import org.wind.mvc.annotation.controller.An_Response;
import org.wind.mvc.annotation.controller.An_URL;
import org.wind.mvc.annotation.controller.An_URLParam;
import org.wind.mvc.bean.ClassInfo;
import org.wind.mvc.bean.MethodParameter;
import org.wind.mvc.bean.URLMapping;
import org.wind.mvc.bean.URLMapping_dynamic;
import org.wind.mvc.bean.context.ActionContext;
import org.wind.mvc.exception.HTTPException;
import org.wind.mvc.init.Init_config;
import org.wind.mvc.init.Init_scan;
import org.wind.mvc.interceptor.Interceptor;
import org.wind.mvc.result.Result;
import org.wind.mvc.util._ObjectUtil;
import org.wind.mvc.util._RegexUtil;
import org.wind.mvc.util._ToolUtil;


/**
 * @描述 : 控制器调度（根据相对路径url，转发到不同的控制器上）
 * @详情 : 核心主类，包含启动后的初始化（含：扫描控制器、拦截器等）
 * @作者 : 胡璐璐
 * @时间 : 2020年8月2日 15:07:51
 */
@MultipartConfig
@WebServlet(value="/",loadOnStartup = 1)
@SuppressWarnings("unchecked")
public class Main extends HttpServlet{

	private static final long serialVersionUID = 1L;
	/************正则表达式************/
	public static final String regex_object="[a-zA-Z_]{1}[a-zA-Z\\d_]*\\.{1}[a-zA-Z_]{1}[a-zA-Z\\d_\\.]*";		//对象
	public static final String regex_array="[a-zA-Z_]{1}[a-zA-Z\\d_]*\\[\\d+]";		//数组
	public static final String regex_array_object="[a-zA-Z_]{1}[a-zA-Z\\d_]*\\[\\d+]\\.{1}[a-zA-Z_]{1}[a-zA-Z\\d_\\.]*";		//数组对象
	/************控制器对象************/
	private static final Map<Class<An_Controller>,Object> controllerObjectMap=new HashMap<Class<An_Controller>, Object>();
	/************拦截器对象************/
	private static final Map<Class<Interceptor>,Interceptor> interceptorObjectMap=new HashMap<Class<Interceptor>, Interceptor>();

	/**初始化**/
	public void init(ServletConfig config) throws ServletException {
		try {
			/*初始化——加载配置文件*/
			new Init_config().init();
			new Init_scan().init();
		}catch(Exception e) {
			e.printStackTrace();
		}
    }
	
	/**请求服务（进来后第一个执行的方法，该方法判断是get或post或其他方式的请求，调用不同的do方法）**/
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			request.setCharacterEncoding("UTF-8");		//请求字符集
			response.setCharacterEncoding("UTF-8");		//返回字符集
			/*****请求URL匹配【控制器+方法】*****/
			String uri=this.getStandardURL(request);
			String urlArr[]=uri.split("/");
			if(urlArr.length>0) {
				String urlLastItem=null;		//url最后一块内容
				StringBuffer url=new StringBuffer();
				for(int i=0;i<urlArr.length;i++) {
					String t_item=urlArr[i].trim();
					//当前段不是空字符串
					if(t_item.length()>0) {
						url.append("/").append(t_item);
						//最后一块
						if(i==urlArr.length-1) {
							urlLastItem=t_item;
						}
					}
					
				}
				if(url.length()>0) {
					URLMapping um=Init_scan.umMap.get(url.toString());		//精确匹配
					/*url适配信息*/
					Class<An_Controller> controllerClass=null;		//控制器类
					Method controllerMethod=null;		//控制器方法
					Map<String, MethodParameter> mpMap=null;		//controllerMethod的参数信息（key=参数名称；value=参数class类型）
					List<Class<Interceptor>> interceptorList=null;		//拦截器链
					String variable=null;		//动态变量
					if(um==null) {
						//若最后1个【/】不在第0位，则代表有多段
						int end=url.lastIndexOf("/");
						if(end>0) {
							String url_dynamic=url.substring(0, end);
							URLMapping_dynamic um_dynamic=Init_scan.umMap_dynamic.get(url_dynamic);
							if(um_dynamic!=null) {
								controllerClass=um_dynamic.getController();
								controllerMethod=um_dynamic.getMethod();
								mpMap=um_dynamic.getMpMap();
								interceptorList=um_dynamic.getInterceptorChain();
								variable=um_dynamic.getVariable();
							}
						}
					}else{
						controllerClass=um.getController();
						controllerMethod=um.getMethod();
						mpMap=um.getMpMap();
						interceptorList=um.getInterceptorChain();
					}
					
					/**找到：控制器+方法+方法参数信息。只有这些都有才继续后续的执行**/
					if(controllerClass!=null && controllerMethod!=null && mpMap!=null) {
						boolean isSupportRequestMethod=this.isSupportRequestMethod(request, controllerMethod);
						//支持当前方法请求
						if(isSupportRequestMethod) {
							Throwable exception=null;		//用户抛出的异常
							Object t_obj_controller=Main.getControllerObject(controllerClass);		//控制器对象
							/*操作上下文*/
							ActionContext context=new ActionContext();
							context.setRequest(request);
							context.setResponse(response);
							context.setController(t_obj_controller);
							context.setMethod(controllerMethod);
							
							/*拦截器——执行前*/
							boolean isContinue=true;		//是否继续（一般在拦截器before()方法里返回的结果）
							int lastHandlerIndex=-1;		//最后处理拦截器的before()方法的索引
							if(interceptorList!=null) {
								for(int i=0;i<interceptorList.size();i++) {
									Class<Interceptor> interceptorClass=interceptorList.get(i);
									Interceptor t_obj_interceptor=Main.getInterceptorObject(interceptorClass);
									try {
										isContinue=t_obj_interceptor.before(context);
									}catch(Exception e) {
										isContinue=false;
										exception=e;
									}
									lastHandlerIndex=i;
									if(!isContinue) {
										break;
									}
									
								}
							}
							//是否继续正常执行
							if(isContinue) {
								try {
									/*方法参数设置*/
									Object paramValueArr[]=this.getMethodParamObject(request, response, mpMap, variable, urlLastItem);
									
									/*反射执行控制器方法*/
									Object result=controllerMethod.invoke(t_obj_controller, paramValueArr);
									Result resultObj=null;
									boolean isRedirect=false;		//是否重定向
									An_Response an_response=controllerMethod.getAnnotation(An_Response.class);
									if(result!=null) {
										//返回类
										if(result instanceof Result) {
											resultObj=(Result) result;
										}else{
											resultObj=new Result();
											//字符串
											if(result instanceof String) {
												//实体数据
												if(an_response!=null && an_response.isBody()) {
													resultObj.setData(result.toString());
												//返回的本工程页面
												}else{
													String resultStr=result.toString();
													//重定向
													if(resultStr.indexOf("redirect:")==0) {
														resultObj.setUrl(resultStr.substring("redirect:".length()));
														isRedirect=true;
													//转发
													}else if(resultStr.indexOf("forward:")==0) {
														resultObj.setUrl(resultStr.substring("forward:".length()));
													}else{
														StringBuffer viewPath_middle=new StringBuffer();		//视图路径中间部分
														An_Controller t_an_Controller=controllerClass.getAnnotation(An_Controller.class);
														String t_viewPathPrefix=null;
														//返回路径首字符为【/】，则代表是从prefix标签下的view做为开始的根路径
														if(resultStr.indexOf("/")==0) {
															t_viewPathPrefix="";
														}else{
															t_viewPathPrefix=t_an_Controller.viewPrefix().trim();
															if(t_viewPathPrefix.length()<=0) {
																t_viewPathPrefix=t_an_Controller.value();
															}
														}
														//首字符必须为【/】
														if(t_viewPathPrefix.indexOf("/")!=0) {
															viewPath_middle.append("/");
														}
														viewPath_middle.append(t_viewPathPrefix);
														//非空字符
														if(resultStr.length()>0) {
															viewPath_middle.append("/");
															viewPath_middle.append(resultStr);
														}
														//
														String viewPrefix=Init_config.frameworkConfig.resource_view_prefix();		//前缀
														String viewSuffix=Init_config.frameworkConfig.resource_view_suffix();		//后缀
														StringBuffer viewPath=new StringBuffer();	//返回的视图（页面）路径
														if(viewPrefix!=null) {
															viewPath.append(viewPrefix);
														}
														
														viewPath.append(viewPath_middle);
														//有配置后缀，并且返回的路径没有后缀名
														if(viewSuffix!=null && viewPath_middle.indexOf(".")==-1) {
															viewPath.append(viewSuffix);
														}
														//
														resultObj.setUrl(viewPath.toString());
													}
												}
											}
										}
										resultObj.setMethodResult(result);		//设置方法返回的对象
									}else{
										resultObj=new Result();
									}
									/*拦截器——执行后*/
									if(interceptorList!=null) {
										for(int i=interceptorList.size()-1;i>=0;i--) {
											Class<Interceptor> interceptorClass=interceptorList.get(i);
											Interceptor t_obj_interceptor=Main.getInterceptorObject(interceptorClass);
											t_obj_interceptor.after(context,resultObj);
										}
									}
									//没有提交
									if(!response.isCommitted()) {
										if(an_response!=null) {
											String contentType=an_response.contentType();
											if(contentType!=null && !contentType.equals("")) {
												response.setContentType(contentType);
											}
											String headArr[]=an_response.head();
											for(String t_head:headArr) {
												String t_oneArr[]=t_head.split(":");
												if(t_oneArr.length==2) {
													response.addHeader(t_oneArr[0], t_oneArr[1]);
												}
											}
										}
										String data=resultObj.getData();
										if(data!=null) {
											response.getWriter().write(data);
										}else {
											//返回页面（重定向、转发）
											String resultUrl=resultObj.getUrl();
											if(resultUrl!=null) {
												//重定向
												if(isRedirect) {
													response.sendRedirect(resultUrl);
												//转发
												}else{
													request.getRequestDispatcher(resultUrl).forward(request, response);		//转发
												}
											}
										}
									}
								}catch(Throwable e) {
									if(e instanceof InvocationTargetException) {
										exception=((InvocationTargetException) e).getTargetException();
									}else{
										exception=e;
									}
								}
							}
							/*拦截器——完成后（所有处理完成后，包含页面渲染）*/
							if(interceptorList!=null) {
								for(int i=lastHandlerIndex;i>=0;i--) {
									Class<Interceptor> interceptorClass=interceptorList.get(i);
									Interceptor t_obj_interceptor=Main.getInterceptorObject(interceptorClass);
									t_obj_interceptor.complete(context,exception);
								}
							}
							//
							return;
						//不支持
						}else{
							throw new HTTPException(HttpServletResponse.SC_NOT_IMPLEMENTED, "不支持的请求方法（"+request.getMethod()+"）");
						}
					}
				}
			}
			//未找到控制（未找到的URL映射器，需要解决）
			throw new HTTPException(HttpServletResponse.SC_NOT_FOUND, "该资源不可用");
		}catch(HTTPException e) {
			response.sendError(e.getStatusCode(), e.getMessage());
		}catch(Exception e) {
			throw new ServletException("服务端异常", e);
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务端异常");
		}
	}
	
	//获取 : 将请求URL解析后的标准的URL
	private String getStandardURL(HttpServletRequest request) {
		String uri=request.getRequestURI(); 		//相对URL—— /jqueryLearn/resources/request.jsp
		String contextPath=request.getContextPath();		//项目名		// /jqueryLearns
		//不等于【/】，并且长度大于0
		if(!contextPath.equals("/") && contextPath.length()>0) {
			uri=uri.replaceFirst(contextPath, "");		//去掉项目名
		}
		return uri;
	}
	//是否支持当前的请求方法
	private boolean isSupportRequestMethod(HttpServletRequest request,Method controllerMethod) {
		An_URL t_an_url=controllerMethod.getAnnotation(An_URL.class);
		String t_urlMethod[]=t_an_url.method();		//支持的请求方法集
		if(t_urlMethod.length>0) {
			String method = request.getMethod().toUpperCase();		//当前请求方法
			for(String t_requestMethod:t_urlMethod) {
				if(method.equalsIgnoreCase(t_requestMethod)) {
					return true;
				}
			}
			return false;
		}else{
			return true;
		}
	}
	/**
	 * 获取 : 将请求参数解析后的对应的方法参数对象集（顺序是方法参数的顺序）
	 * @param request
	 * @param response
	 * @param mpList
	 * @param variable : 动态变量
	 * @param urlLastItem : 请求url最后一块内容（以url的"/"做分隔符，产生的数组，每一处代表一块内容）
	 */
	private Object[] getMethodParamObject(HttpServletRequest request,HttpServletResponse response,Map<String,MethodParameter> mpMap,String variable,String urlLastItem) throws Exception {
		/*方法参数设置*/
		Object paramValueArr[]=new Object[mpMap.size()];
		boolean isSpecial=false;		//是否有特殊参数（数组式List参数等）
		for(MethodParameter t_mp:mpMap.values()) {
			String t_paramName=t_mp.getName();	//对应的参数名
			int index=t_mp.getIndex();
			Class<?> t_paramClass=t_mp.getType();
			An_URLParam t_anUrlParam=t_mp.getAnUrlParam();
			Object t_value_set=null;
			if(t_anUrlParam!=null) {
				String nameMapping=t_anUrlParam.value();		//映射的参数名
				if(nameMapping.length()>0) {
					t_paramName=nameMapping;
				}
			}
			//Part上传文件类（servlet 3.0）
			if(t_paramClass==Part.class) {
				t_value_set=request.getPart(t_paramName);
			//ServletRequest
			}else if(ServletRequest.class.isAssignableFrom(t_paramClass)) {
				t_value_set=request;
			//ServletResponse
			}else if(ServletResponse.class.isAssignableFrom(t_paramClass)) {
				t_value_set=response;
			//HttpSession
			}else if(HttpSession.class.isAssignableFrom(t_paramClass)) {
				t_value_set=request.getSession();
			//List、数组
			}else if(List.class.isAssignableFrom(t_paramClass)) {
				t_value_set=getMethodParamObject_List(request, t_mp, t_paramName);
				//没找到
				if(t_value_set==null) {
//					Class<?> genericClass=t_mp.getGenericClass();	//泛型
					//如果是自定义类的话，用数组的方式解析（idList[0]、idList[1]；objList[0].sex.id、objList[1].sex.name等）
//					if(_ToolUtil.isCustomClass(genericClass)) {
//						isSpecial=true;
//					}
					isSpecial=true;
				}
			//自定义类
			}else if(_ToolUtil.isCustomClass(t_paramClass)) {
				isSpecial=true;
//				t_value_set=this.getMethodParamObject_custom(request, t_paramClass,t_paramName,0);
			}else{
				String t_paramValue=request.getParameter(t_paramName);
				if(t_paramValue==null) {
					//当前参数是URL的通配符（动态变量）
					if(t_paramName.equals(variable)) {
						t_paramValue=urlLastItem;
					}
				}
				if(t_paramValue!=null) {
					try {
						t_value_set=_ObjectUtil.cast(t_paramValue, t_paramClass);
					}catch(NumberFormatException e) {
						throw new NumberFormatException("参数【"+t_paramName+"】不能转换成【"+t_paramClass.getSimpleName()+"】");
					}catch(ClassCastException e) {
						throw new ClassCastException("参数【"+t_paramName+"】不能强制转换成【"+t_paramClass.getName()+"】");
					}
				}
			}
			paramValueArr[index]=t_value_set;
		}
		//特殊对象
		if(isSpecial) {
			this.setMethodParamObject_special(request, mpMap,paramValueArr);
		}
		return paramValueArr;
	}
	//设置 : 将请求参数解析后的对应的方法参数对象——特殊类（mpMap=方法参数对象信息；paramValueArr=方法参数值数组）
	private void setMethodParamObject_special(HttpServletRequest request,Map<String,MethodParameter> mpMap,Object paramValueArr[]) throws Exception{
		Map<String,String[]> paramMap=request.getParameterMap();
		if(paramMap.size()>0) {
			Map<Class<?>,ClassInfo> classInfoMap=Init_scan.classInfoMap;
			Iterator<Entry<String,String[]>> iter=paramMap.entrySet().iterator();
			//数组对象：主参数—>数组顺序—>次参数—>值【对象Map—>参数—>值【对象Map...】......如此循环】
			Map<String,TreeMap<Integer,Map<String,Object>>> paramMap_list=new HashMap<String, TreeMap<Integer,Map<String,Object>>>();
			int paramMap_list_maxIndex=-1;		//最大索引
			//数组：主参数—>数组顺序—>值
			Map<String,TreeMap<Integer,String>> paramMap_array=new HashMap<String, TreeMap<Integer,String>>();
			int paramMap_array_maxIndex=-1;		//最大索引
			//对象：主参数—>次参数—>值【对象Map—>参数—>值【对象Map...】......如此循环】
			Map<String,Map<String,Object>> paramMap_object=new HashMap<String, Map<String,Object>>();
			while(iter.hasNext()) {
				Entry<String,String[]> entry=iter.next();
				String t_key=entry.getKey();
				String t_valueArr[]=entry.getValue();
				//数组对象
				if(t_key.matches(regex_array_object)) {
					String t_paramName=_RegexUtil.getCull(t_key, null,"[");		//参数名
					MethodParameter t_mp=mpMap.get(t_paramName);
					if(t_mp!=null) {
						Class<?> t_genericClass=t_mp.getGenericClass();		//泛型
						ClassInfo classInfo=classInfoMap.get(t_genericClass);
						//存在
						if(classInfo!=null) {
							TreeMap<Integer,Map<String,Object>> t_map_sort=paramMap_list.get(t_paramName);		//顺序
							if(t_map_sort==null) {
								t_map_sort=new TreeMap<Integer,Map<String,Object>>();
								paramMap_list.put(t_paramName, t_map_sort);
							}
							//
							int index=Integer.valueOf(_RegexUtil.getCull(t_key, "[", "]"));		//索引
							Map<String,Object> t_map_sort_param=t_map_sort.get(index);		//顺序下的参数
							if(t_map_sort_param==null) {
								t_map_sort_param=new HashMap<String,Object>();
								t_map_sort.put(index, t_map_sort_param);
							}
							if(index>paramMap_list_maxIndex) {
								paramMap_list_maxIndex=index;
							}
							/**子参数**/
							String t_childParam=_RegexUtil.getCull(t_key, ".",null);		//子参数（主参数下的参数。例：sex.name、sex.z11.x22、sex.a1.s3.d5等）
							Map<String,Field> fieldMap=classInfo.getFieldMap();
							Field t_field=fieldMap.get(t_childParam);
							//基础数据类型（伪）
							if(t_field!=null) {
								t_map_sort_param.put(t_childParam, t_valueArr[0]);		
							//对象（至少是xxx.yyy）
							}else if(t_childParam.matches(regex_object)){
								String t_childParamArr[]=t_childParam.split("\\.");		//多字段串
								this.setObjectMapValue(t_childParamArr, t_valueArr[0],t_map_sort_param, classInfo, 0);
							}
							
						}
					}
				//数组
				}else if(t_key.matches(regex_array)){
					String t_paramName=_RegexUtil.getCull(t_key, null,"[");		//参数名
					MethodParameter t_mp=mpMap.get(t_paramName);
					if(t_mp!=null) {
						TreeMap<Integer,String> t_map_sort=paramMap_array.get(t_paramName);		//顺序
						if(t_map_sort==null) {
							t_map_sort=new TreeMap<Integer,String>();
							paramMap_array.put(t_paramName, t_map_sort);
						}
						int index=Integer.valueOf(_RegexUtil.getCull(t_key, "[", "]"));		//索引
						if(index>paramMap_array_maxIndex) {
							paramMap_array_maxIndex=index;
						}
						t_map_sort.put(index, t_valueArr[0]);
					}
				//对象
				}else if(t_key.matches(regex_object)){
					String t_paramName=_RegexUtil.getCull(t_key, null,".");		//参数名
					MethodParameter t_mp=mpMap.get(t_paramName);
					if(t_mp!=null) {
						Class<?> t_typeClass=t_mp.getType();		//处理的类型
						ClassInfo classInfo=classInfoMap.get(t_typeClass);
						//存在
						if(classInfo!=null) {
							Map<String,Object> paramMap_object_param=paramMap_object.get(t_paramName);		//主参数下的子参数
							if(paramMap_object_param==null) {
								paramMap_object_param=new HashMap<String, Object>();
								paramMap_object.put(t_paramName, paramMap_object_param);
							}
							/**子参数**/
							String t_childParam=_RegexUtil.getCull(t_key, ".",null);		//子参数（主参数下的参数。例：sex.name、sex.z11.x22、sex.a1.s3.d5等）
							Map<String,Field> fieldMap=classInfo.getFieldMap();
							Field t_field=fieldMap.get(t_childParam);
							//基础数据类型（伪）
							if(t_field!=null) {
								paramMap_object_param.put(t_childParam, t_valueArr[0]);		
							//对象（至少是xxx.yyy）
							}else if(t_childParam.matches(regex_object)){
								String t_childParamArr[]=t_childParam.split("\\.");		//多字段串
								this.setObjectMapValue(t_childParamArr, t_valueArr[0],paramMap_object_param, classInfo, 0);
							}
						}
					}
				}
			}
			
			/*数组对象*/
			if(paramMap_list.size()>0) {
				Iterator<Entry<String,TreeMap<Integer,Map<String,Object>>>> iter_list=paramMap_list.entrySet().iterator();
				while(iter_list.hasNext()) {
					Entry<String,TreeMap<Integer,Map<String,Object>>> entry=iter_list.next();
					String t_key=entry.getKey();
					MethodParameter t_mp=mpMap.get(t_key);
					if(t_mp!=null) {
						Class<?> t_genericClass=t_mp.getGenericClass();
						/***指定数组对象索引***/
						TreeMap<Integer,Map<String,Object>> t_value=entry.getValue();
						Object array[]=new Object[paramMap_list_maxIndex+1];
						Iterator<Entry<Integer,Map<String,Object>>> iter_list_value=t_value.entrySet().iterator();
						while(iter_list_value.hasNext()) {
							Entry<Integer,Map<String,Object>> entry_value=iter_list_value.next();
							int index=entry_value.getKey();
							Map<String,Object> t_value_paramMap=entry_value.getValue();
							Object obj=this.getMethodParamObject_custom(t_value_paramMap, t_genericClass, t_key+"["+index+"]");
							if(obj!=null) {
								array[index]=obj;
							}
						}
						//有值
						if(array.length>0) {
							paramValueArr[t_mp.getIndex()]=new ArrayList<Object>(Arrays.asList(array));
						}
					}
				}
			}
			/*数组*/
			if(paramMap_array.size()>0) {
				Iterator<Entry<String, TreeMap<Integer, String>>> iter_array=paramMap_array.entrySet().iterator();
				while(iter_array.hasNext()) {
					Entry<String,TreeMap<Integer,String>> entry=iter_array.next();
					String t_key=entry.getKey();
					MethodParameter t_mp=mpMap.get(t_key);
					if(t_mp!=null) {
						Class<?> t_genericClass=t_mp.getGenericClass();
						/***指定数组对象索引***/
						TreeMap<Integer,String> t_value=entry.getValue();
						Object array[]=new Object[paramMap_array_maxIndex+1];
						Iterator<Entry<Integer,String>> iter_array_value=t_value.entrySet().iterator();
						while(iter_array_value.hasNext()) {
							Entry<Integer,String> entry_value=iter_array_value.next();
							int index=entry_value.getKey();
							String t_value_param=entry_value.getValue();
							try {
								Object t_value_set=_ObjectUtil.cast(t_value_param, t_genericClass);
								if(t_value_set!=null) {
									array[index]=t_value_set;
								}
							}catch(NumberFormatException e) {
								throw new NumberFormatException("数组参数【"+t_key+"】不能转换成【"+t_genericClass.getSimpleName()+"】");
							}catch(ClassCastException e) {
								throw new ClassCastException("数组参数【"+t_key+"】不能强制转换成【"+t_genericClass.getName()+"】");
							}
						}
						//有值
						if(array.length>0) {
							paramValueArr[t_mp.getIndex()]=new ArrayList<Object>(Arrays.asList(array));
						}
					}
				}
			}
			/*对象*/
			if(paramMap_object.size()>0) {
				Iterator<Entry<String, Map<String, Object>>> iter_object=paramMap_object.entrySet().iterator();
				while(iter_object.hasNext()) {
					Entry<String, Map<String, Object>> entry=iter_object.next();
					String t_key=entry.getKey();
					MethodParameter t_mp=mpMap.get(t_key);
					if(t_mp!=null) {
						Map<String, Object> t_value=entry.getValue();
						Object obj=this.getMethodParamObject_custom(t_value, t_mp.getType(), t_key);
						//有值
						if(obj!=null) {
							paramValueArr[t_mp.getIndex()]=obj;
						}
					}
				}				
			}
		}
	}
	/**
	 * 设置 : 对象Map的value，途中层级的对象将会被创建Map
	 * @param paramArr : 请求参数段（如：obj.sex.aa.a1，拆分成：[obj,sex,aa,a1]）
	 * @param upperParamMap : 上一层Map
	 * @param ClassInfo : 上一层的Class信息
	 * @param currentIndex : 当前层索引
	 */
	private void setObjectMapValue(String paramArr[],String paramValue,Map<String,Object> upperParamMap,ClassInfo upperClassInfo,int currentIndex) {
		int lastIndex=paramArr.length-1;		//最后一个索引
		if(lastIndex>0) {
			//上一层ClassInfo存在，且当前索引不是最后一个。倒数最后1段之前的都是对象
			if(upperClassInfo!=null && lastIndex>currentIndex) {
				Map<String,Field> upperFieldMap=upperClassInfo.getFieldMap();		//上一层的字段列表
				//
				String param_current=paramArr[currentIndex];		//当前层参数名
				Field field_current=upperFieldMap.get(param_current);		//当前层字段
				if(field_current!=null) {
					Class<?> fieldTypeClass_current=field_current.getType();		//当前层字段类型
					//自定义类（必须是）
					if(_ToolUtil.isCustomClass(fieldTypeClass_current)) {
						Object t_obj=upperParamMap.get(param_current);
						if(t_obj==null) {
							t_obj=new HashMap<String,String>();
							upperParamMap.put(param_current, t_obj);
						}
						//Map
						if(t_obj instanceof Map) {
							Map<Class<?>,ClassInfo> classInfoMap=Init_scan.classInfoMap;
							//
							Map<String,Object> map_current=(Map<String, Object>) t_obj;		//当前层Map
							ClassInfo classInfo_current=classInfoMap.get(fieldTypeClass_current);		//当前层ClassInfo
							int nextIndex=currentIndex+1;		//下一层索引
							//下一层对象
							this.setObjectMapValue(paramArr, paramValue,map_current, classInfo_current,nextIndex);
						}
					}
				}
			//基础数据类型（请求参数最后一段）
			}else{
				upperParamMap.put(paramArr[lastIndex], paramValue);
			}
		}
	}
	
	//获取 : 将请求参数解析后的对应的方法参数对象——List类（paramName=参数名；currentLayer=当前循环依赖的层数）
	private List<Object> getMethodParamObject_List(HttpServletRequest request,MethodParameter mp,String paramName) throws Exception{
		Class<?> genericClass=mp.getGenericClass();
		List<Object> list=new ArrayList<Object>();
		//上传文件
		if(genericClass==Part.class) {
			Collection<Part> partList=request.getParts();
			if(partList!=null && partList.size()>0) {
				int maxIndex=0;
				Map<Integer, Part> t_partMap=new LinkedHashMap<Integer, Part>();
				for(Part obj:partList) {
					String t_partName=obj.getName();
					//数组
					if(t_partName.matches(regex_array)) {
						String t_paramKey=_RegexUtil.getCull(t_partName, null,"[");		//参数名
						//匹配
						if(paramName.equals(t_paramKey)) {
							int t_index=Integer.valueOf(_RegexUtil.getCull(t_partName, "[", "]"));		//索引
							if(maxIndex<t_index) {
								maxIndex=t_index;
							}
							t_partMap.put(t_index, obj);
						}
					}
				}
				//有最大
				if(maxIndex>0) {
					Object arr[]=new Object[maxIndex+1];
					for(Entry<Integer,Part> t_entry:t_partMap.entrySet()) {
						arr[t_entry.getKey()]=t_entry.getValue();
					}
					list=new ArrayList<Object>(Arrays.asList(arr));
				}
			}
		//其他
		}else{
			String valueArr[]=request.getParameterValues(paramName);
			if(valueArr!=null && valueArr.length>0) {
				for(String t_value:valueArr) {
					try {
						Object t_value_set=_ObjectUtil.cast(t_value, genericClass);
						list.add(t_value_set);
					}catch(NumberFormatException e) {
						throw new NumberFormatException("数组参数【"+paramName+"】不能转换成【"+genericClass.getSimpleName()+"】");
					}catch(ClassCastException e) {
						throw new ClassCastException("数组参数【"+paramName+"】不能强制转换成【"+genericClass.getName()+"】");
					}
				}	
			}
		}
		if(list.size()>0) {
			return list;
		}
		return null;
	}
	//获取 : 将请求参数解析后的对应的方法参数对象——自定义类（paramMap=参数，带子对象层级；paramNamePrefix=参数名前缀）
	private Object getMethodParamObject_custom(Map<String,Object> paramMap,Class<?> clazz,String paramNamePrefix) throws Exception{
		if(paramMap!=null && paramMap.size()>0) {
			Iterator<Entry<String, Object>> iter=paramMap.entrySet().iterator();
			Map<Class<?>,ClassInfo> classInfoMap=Init_scan.classInfoMap;
			ClassInfo classInfo=classInfoMap.get(clazz);
			if(classInfo!=null) {
				Map<String,Field> fieldMap=classInfo.getFieldMap();
				Map<String,Method> methodMap=classInfo.getMethodMap();
				boolean isHaveParam=false;		//是否有参数值
				Object obj=clazz.newInstance();	//当前主类对象
				while(iter.hasNext()) {
					Entry<String, Object> entry=iter.next();
					String t_key=entry.getKey();		//参数名
					Field t_paramField=fieldMap.get(t_key);		//参数对应的字段
					//存在该参数
					if(t_paramField!=null) {
						Object t_value=entry.getValue();
						Object t_value_set=null;
						Class<?> t_value_class=t_paramField.getType();
						//Map
						if(t_value instanceof Map) {
							//自定义类（循环依赖层数少于指定数）
							if(_ToolUtil.isCustomClass(t_value_class)) {
								t_value_set=this.getMethodParamObject_custom((Map<String, Object>) t_value, t_value_class,paramNamePrefix+"."+t_key);
							}
						//基础数据类型（含：字符串）
						}else{
							try {
								t_value_set=_ObjectUtil.cast(t_value.toString(), t_value_class);
							}catch(NumberFormatException e) {
								throw new NumberFormatException("参数【"+paramNamePrefix+"."+t_key+"】不能转换成【"+t_value_class.getSimpleName()+"】");
							}catch(ClassCastException e) {
								throw new ClassCastException("参数【"+paramNamePrefix+"."+t_key+"】不能强制转换成【"+t_value_class.getName()+"】");
							}
						}
						if(t_value_set!=null) {
							_ObjectUtil.set(obj, methodMap, t_paramField, t_value_set);
							isHaveParam=true;
						}
					}
				}
				if(isHaveParam) {
					return obj;
				}
			}else{
				throw new RuntimeException("参数【"+paramNamePrefix+"】没有找到ClassInfo，请联系作者");
			}
		}
		return null;
	}
	/**销毁**/
    public void destroy() {
       super.destroy();
    }
    
    /**********************静态方法**********************/
    /**获取 : 控制器对象**/
    private static Object getControllerObject(Class<An_Controller> controllerClass) throws Exception{
    	Object obj_controller=controllerObjectMap.get(controllerClass);
		if(obj_controller==null) {
			synchronized (controllerClass) {
				obj_controller=controllerObjectMap.get(controllerClass);
				if(obj_controller==null) {
					obj_controller=controllerClass.newInstance();
					controllerObjectMap.put(controllerClass, obj_controller);
				}
			}
		}
		return obj_controller;
    }
    /**获取 : 拦截器对象**/
    private static Interceptor getInterceptorObject(Class<Interceptor> interceptorClass) throws Exception {
    	Interceptor obj_controller=interceptorObjectMap.get(interceptorClass);
    	if(obj_controller==null) {
			synchronized (interceptorClass) {
				obj_controller=interceptorObjectMap.get(interceptorClass);
				if(obj_controller==null) {
					obj_controller=(Interceptor) interceptorClass.newInstance();
					interceptorObjectMap.put(interceptorClass, obj_controller);
				}
			}
    	}
    	return obj_controller;
    }
}
