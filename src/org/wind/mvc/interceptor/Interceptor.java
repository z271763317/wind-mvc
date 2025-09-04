package org.wind.mvc.interceptor;

import org.wind.mvc.bean.context.ActionContext;
import org.wind.mvc.result.Result;

/**
 * @描述 : 拦截器接口
 * @详情 : 拥有【执行控制器方法前before()、执行控制器后after()、完成complete()】的处理。<br />
 * 				拦截器执行顺序：先按【正序】，执行所有拦截器的before()，后按【倒序】，执行所有拦截器的after()，最后按【倒序】，执行所有拦截器的complete() <br />
 * 				记录最后一次成功返回true的before()方法所属的拦截器的列表索引位置，执行complete()时，从该处倒序执行<br />
 * 				方法执行顺序：1、before()；2、after()；3、complete()	<br />
 * 				例：有拦截器：Interceptor1、Interceptor3、Interceptor3。当3个都触发了，则处理逻辑是：<br /><br />
 * 
 * 					Interceptor1 : before()—>Interceptor2 : before()—>Interceptor3 : before()		<br />
 * 					—>控制器方法—>		<br/>
 * 					Interceptor3 : after()—>Interceptor2 : after()—>Interceptor1 : after()		<br />
 * 					Interceptor3 : complete()—>Interceptor2 : complete()—>Interceptor1 : complete()		<br /><br />
 * 
 * 					<b style="color:red">注</b>：如果before()其中一个返回为【false】（若抛异常也算做false，并在所有拦截器complete()处的参数Throwable是该异常对象），则停止后续执行，直接跳到complete()部分，并从最后一次返回true的拦截器的索引位置倒序执行
 * @作者 : 胡璐璐
 * @时间 : 2020年8月28日 14:45:19
 */
public interface Interceptor {

	/**执行前（返回值为：true=继续往下执行；false=停止执行）**/
	public boolean before(ActionContext context) throws Exception;
	/**执行后（result=控制器方法返回的对象，包含：页面视图相对路径）**/
	public void after(ActionContext context,Result result);
	/**完成后（所有处理完，渲染页面后执行。若在此之前出现了异常，则exception会有值，可做异常处理）**/
	public void complete(ActionContext context,Throwable exception);
	
}
