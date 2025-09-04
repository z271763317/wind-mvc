package org.wind.mvc.exception;

/**
 * @描述 : HTTP异常（带HTTP状态码）
 * @作者 : 胡璐璐
 * @时间 : 2020年8月21日 16:27:25
 */
public class HTTPException extends Exception{

	private static final long serialVersionUID = 1L;
	private int statusCode = -1;

    public HTTPException(int statusCode,String msg) {
        super(msg);
        this.statusCode = statusCode;
    }
    /**获取 : 状态码**/
    public int getStatusCode() {
        return this.statusCode;
    }
	
}
