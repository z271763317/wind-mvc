package org.wind.mvc.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *@描述 : 文件操作集
 *@版权 : 胡璐璐
 *@时间 : 2019年11月22日 17:57:30
 */
public class _FileOS {

	/**获取 : 项目路径，指定匹配关键词（endIndexAfter=结束位置的后【负数为前】几位，负数情况下，如果绝对值大于pattcherStr大小，将直接返回为null）**/
	public static String getProjectPath(String pattcherStr,int endIndexAfter) {
		try {
			URL url =Thread.currentThread().getContextClassLoader().getResource("");
			String path = url.getFile();
			if(pattcherStr!=null) {
				int end=path.indexOf(pattcherStr)+pattcherStr.length();
				if(endIndexAfter<0 && end<(Math.abs(endIndexAfter))) {
					return null;
				}
				path = path.substring(0, end+(endIndexAfter));
			}
			if (path.indexOf("file:") > -1) {
				path = path.substring(5);
			} else if (path.indexOf("ile:") > -1) {
				path = path.substring(path.indexOf(":") + 2);
			}
			return java.net.URLDecoder.decode(path,"utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    /**  
     * 获取 : 指定文件目录路径下的所有深层次Class类
     * @param fileDirectoryPath : 文件夹（文件目录）路径
     * @param packagePrefix : 包名前缀（会拼上文件名，形成类的全路径）
     */  
    public static List<Class<?>> getAllClassList(String fileDirectoryPath,String packagePrefix) {
    	List<Class<?>> classList = new ArrayList<Class<?>>();
    	try{
	        File fileDirectory= new File(fileDirectoryPath);
	        if(fileDirectory.exists() && fileDirectory.isDirectory()) {
	        	packagePrefix=packagePrefix!=null && packagePrefix.trim().length()>0?packagePrefix.trim():null;
	        	if(packagePrefix!=null) {
	        		packagePrefix+=".";
	        	}else{
	        		packagePrefix="";
	        	}
        		for (File t_file : fileDirectory.listFiles()){
        			String name = t_file.getName();
 		            //class文件
 		            if (name.endsWith(".class")) {   
 		            	classList.add(Class.forName(packagePrefix + name.substring(0, name.lastIndexOf("."))));
 		            //目录
 		            }else if(t_file.isDirectory()){
 		            	classList.addAll(getAllClassList(t_file, packagePrefix+t_file.getName()));
 		            }
 	            }
	        } 
    	}catch(Exception e){
    		e.printStackTrace();
    	}
        return classList; 
    }
    /**  
     * 获取 : 指定文件目录下的所有深层次Class类
     * @param fileDirectory : 文件夹（文件目录）对象
     * @return packagePrefix : 包名前缀（会拼上文件名，形成类的全路径）
     */  
    public static List<Class<?>> getAllClassList(File fileDirectory,String packagePrefix) {
    	List<Class<?>> classList = new ArrayList<Class<?>>();
    	try{
    		//目录
	        if (fileDirectory.exists() && fileDirectory.isDirectory()) {
	        	packagePrefix=packagePrefix!=null && packagePrefix.trim().length()>0?packagePrefix.trim():null;
	        	if(packagePrefix!=null) {
	        		packagePrefix+=".";
	        	}
        		for (File t_file : fileDirectory.listFiles()){
        			String name = t_file.getName();
 		            //class文件
 		            if (name.endsWith(".class")) {   
 		            	classList.add(Class.forName(packagePrefix + name.substring(0, name.lastIndexOf("."))));
 		            //目录
 		            }else if(t_file.isDirectory()){
 		            	classList.addAll(getAllClassList(t_file, packagePrefix+t_file.getName()));
 		            }
 	            }
	        } 
    	}catch(Exception e){
    		e.printStackTrace();
    	}
        return classList; 
    }
}