package org.wind.mvc.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @描述 : 通用工具类
 * @作者 : 胡璐璐
 * @时间 : 2020年10月1日 17:16:51
 */
public class _ToolUtil {

	/*非自定义类*/
	private static Set<Class<?>> nonCustomClassSet=new HashSet<Class<?>>();
	
	static {
		nonCustomClassSet.add(ServletRequest.class);
		nonCustomClassSet.add(ServletResponse.class);
		nonCustomClassSet.add(HttpSession.class);
	}
	
	/**判断 : 指定的类是否为用户自定义类**/
	public static boolean isCustomClass(Class<?> clazz) {
		if(clazz.getClassLoader()!=null) {
			Iterator<Class<?>> iter=nonCustomClassSet.iterator();
			while(iter.hasNext()) {
				Class<?> nonCustomClass=iter.next();
				if(nonCustomClass.isAssignableFrom(clazz)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
