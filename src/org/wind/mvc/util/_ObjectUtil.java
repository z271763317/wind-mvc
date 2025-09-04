package org.wind.mvc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @描述 : 对象工具类
 * @作者 : 胡璐璐
 * @时间 : 2020年6月22日 15:15:46
 */
public class _ObjectUtil {
	
	/**
  	 * 获取：指定类的所有Field字段，包含父类，直到指定父类停止（不在获取）
  	 * @param tableClass : 要获取所有Field字段的Class类
  	 * @param parentClass : 指定要停止获取父类Field的Class（不能为空，否则返回空）
  	 */
  	public static Field[] getField(Class<?> tableClass,Class<?> parentClass){
  		Map<String,Field> fieldMap=getFieldMap(tableClass, parentClass);
		Field fArr[]=new Field[fieldMap.size()];
		int i=0;
		for(Field t_field:fieldMap.values()){
			fArr[i]=t_field;
			i++;
		}
		return fArr;
  	}
  	/**
  	 * 获取：指定类的所有Field字段，包含父类，直到指定父类停止（不在获取，Map式）
  	 * @param tableClass : 要获取所有Field字段的Class类
  	 * @param parentClass : 指定要停止获取父类Field的Class（不能为空，否则返回空）
  	 */
  	public static Map<String,Field> getFieldMap(Class<?> tableClass,Class<?> parentClass){
  		Map<String,Field> fieldMap=new LinkedHashMap<String, Field>();		//key=字段名称
  		if(tableClass!=null){
	  		if(parentClass!=null){
	  			Class<?> t_parentClass=tableClass;
	  			while(t_parentClass!=null && t_parentClass!=parentClass){
	  				Field t_fieldArr[]=t_parentClass.getDeclaredFields();
		  			for(Field f:t_fieldArr){
		  				String t_fieldName=f.getName();
		  				//若不存在
		  				if(!fieldMap.containsKey(t_fieldName)){
		  					fieldMap.put(t_fieldName,f);
		  				}
		  			}
		  			t_parentClass=t_parentClass.getSuperclass();
	  			}
	  			return fieldMap;
	  		}
  		}
  		return fieldMap;
  	}
  	/**
  	 * 获取指定类的所有Method方法，包含父类，直到指定父类停止（不在获取）
  	 * @param tableClass : 要获取所有Method字段的Class类
  	 * @param parentClass : 指定要停止获取父类Method的Class（为空则一直获取到底）
  	 * @return tableClass为空则返回null
  	 */
  	public static Method[] getMethod(Class<?> tableClass,Class<?> parentClass){
  		if(tableClass!=null){
	  		if(parentClass!=null){
	  			Map<String,Method> methodMap=new LinkedHashMap<String, Method>();		//key=方法名称
	  			Class<?> t_parentClass=tableClass;
	  			while(t_parentClass!=null && t_parentClass!=parentClass){
	  				Method t_methodArr[]=t_parentClass.getDeclaredMethods();
		  			for(Method m:t_methodArr){
		  				StringBuffer key=new StringBuffer(m.getName());
		  				Class<?> paramArr[]=m.getParameterTypes();
						for(int j=0;j<paramArr.length;j++){
							key.append(paramArr[j].getSimpleName());
						}
						//是否不存在
						if(!methodMap.containsKey(key.toString())){
							methodMap.put(key.toString(), m);
						}
		  			}
		  			t_parentClass=t_parentClass.getSuperclass();
	  			}
	  			Method mArr[]=new Method[methodMap.size()];
	  			int i=0;
	  			for(Method t_m:methodMap.values()){
	  				mArr[i]=t_m;
	  				i++;
	  			}
	  			return mArr;
	  		}else{
	  			return tableClass.getMethods();	
	  		}
  		}else{
  			return null;
  		}
  	}
  	
  	/**
	 * 获取：该Class所有的Method（Map式）
	 * @param tableClass : 继承Table的Class
	 * @param prefix : 方法名前缀，可模糊取prefix匹配的方法,传null则不验证
	 * @return 返回tableClass所有的Method（Map式），格式：【小写】方法名+所有参数类型名（按顺序）
	 */
	public static Map<String,Method> getMethodMap(Class<?> tableClass,Class<?> parentClass,String prefix){
		Method methodArr[]=getMethod(tableClass,parentClass);
		Map<String,Method> methodMap=new HashMap<String,Method>();	//Key方法名（小写）映射的所有参数Class
		for(int i=0;i<methodArr.length;i++){
			Method method=methodArr[i];
			String key=method.getName();
			if(prefix==null || key.indexOf(prefix)!=-1){
				Class<?> paramArr[]=method.getParameterTypes();
				for(int j=0;j<paramArr.length;j++){
					key+=paramArr[j].getSimpleName();
				}
				methodMap.put(key.toLowerCase(), method);	//key=小写（方法名+参数类型名） value=Method对象
			}
		}
		return methodMap;
	}
	/**
	 * 对象类型转换
	 * @param source : 源对象
	 * @param converTypeClass : 目标类型Class
	 * @return 返回目标类型对象
	 */
	public static Object cast(Object source,Class<?> dstTypeClass){
		Object dstObj=null;
		if(source!=null && dstTypeClass!=null){
			if(source instanceof Number){
				Number number=(Number)source;
				if(dstTypeClass==Byte.class){
					dstObj=number.byteValue();
				}else if(dstTypeClass==Double.class){
					dstObj=number.doubleValue();
				}else if(dstTypeClass==Float.class){
					dstObj=number.floatValue();
				}else if(dstTypeClass==Integer.class){
					dstObj=number.intValue();
				}else if(dstTypeClass==Long.class){
					dstObj=number.longValue();
				}else if(dstTypeClass==Short.class){
					dstObj=number.shortValue();
				}
			}
			//不是Number、基础数据类型
			if(dstObj==null){
				if (dstTypeClass.isAssignableFrom(source.getClass())){
					dstObj=dstTypeClass.cast(source);
				//String
				}else if(dstTypeClass==String.class){
					dstObj=source.toString();
				}else if(source.toString().trim().length()>0){
					source=source.toString().trim();
					//Integer
					if(dstTypeClass==Integer.class){
						dstObj=Integer.parseInt(source.toString());
					//Long
					}else if(dstTypeClass==Long.class){
						dstObj=Long.parseLong(source.toString());
					//Float
					}else if(dstTypeClass==Float.class){
						dstObj=Float.parseFloat(source.toString());
					//Double
					}else if(dstTypeClass==Double.class){
						dstObj=Double.parseDouble(source.toString());
					//Byte
					}else if(dstTypeClass==Byte.class){
						dstObj=Byte.parseByte(source.toString());
					//Short
					}else if(dstTypeClass==Short.class){
						dstObj=Short.parseShort(source.toString());
					//Boolean
					}else if(dstTypeClass==Boolean.class){
						dstObj=Boolean.parseBoolean(source.toString());
					}else{
						dstObj=source;
					}
				}
			}
		}else{
			dstObj=source;
		}
		return dstObj;
	}
	/**
	 * 【set方法】：反射执行对象set方法的Method后的返回值
	 * @param obj : java对象
	 * @param methodMap : obj的所有Method对象，并且key是以"set"+【小写f字段名】
	 * @param f : obj的一个属性Field对象
	 * @papram paramValue : 参数值
	 * @return 返回反射执行setXXX()方法后的返回值
	 * @throws Exception 
	 */
	public static Object set(Object obj,Map<String,Method> methodMap,Field field,Object paramValue) throws Exception {
		if(field!=null){
			StringBuffer menthod=new StringBuffer("set"+field.getName());
			Class<?> t_class=field.getType();
			menthod.append(t_class.getSimpleName());
			menthod=new StringBuffer(menthod.toString().toLowerCase());
			Method m=methodMap.get(menthod.toString());
			try {
				Object paramValueArr[]={paramValue};
				return m.invoke(obj, paramValueArr);
			} catch (Exception e) {
				throw e;
			}
		}else{
			return null;
		}
	}
	/** 获取：List字段的泛型Class**/
	public static Class<?> getGeneric(Field genericField){
		//List类型
		if(genericField!=null && List.class.isAssignableFrom(genericField.getType())){
             Type fc = genericField.getGenericType(); // 关键的地方，如果是List类型，得到其Generic的类型  
             if(fc instanceof ParameterizedType){
            	 ParameterizedType pt = (ParameterizedType) fc;  
            	 Type tArr[]=pt.getActualTypeArguments();
            	 Type type=tArr[0];
            	 return (Class<?>)type;
             }
		}
        return null;
	}
}