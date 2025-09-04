package org.wind.mvc.exception.param;

/**
 * @描述 : 参数验证异常——Null（为空）
 * @作者 : 胡璐璐
 * @时间 : 2020年8月21日 16:27:25
 */
public class ParamValidException_null extends IllegalArgumentException{

	private static final long serialVersionUID = 1L;

    public ParamValidException_null(String msg) {
        super(msg);
    }
	
}
