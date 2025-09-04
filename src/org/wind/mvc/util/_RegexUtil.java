package org.wind.mvc.util;

/**
 * @描述 : 正则表达式：验证邮箱帐号、拆分字符串为List、获取所需信息等
 * @作者 : 胡璐璐
 * @时间 : 2012年10月10日 10:45:05
 */
public class _RegexUtil {

  	/**
	 * 截取字符串(单个截取)
	 * @param html : HTML内容
	 * @param start : 开始部分（null=0）
	 * @param end : 结尾部分(null=html最后一个字符串的位置)
	 * @return 返回开始和结尾中间的内容
	 */
	public static String getCull(String html, String start, String end) {
		if (html!=null) {
			int start1 =0;
			int end1 = html.length();
			if(start!=null){
				int start2=html.indexOf(start);
				if(start2!=-1){
					start1=start2+ start.length();
				}else{
					return "";
				}
			}
			if (end != null) {
				end1 = html.indexOf(end, start1);
			}
			if (start1 > end1) {
				return "";
			}
			return html.substring(start1, end1);
		} else {
			return "";
		}
	}
}