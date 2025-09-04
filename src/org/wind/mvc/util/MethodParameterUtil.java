package org.wind.mvc.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.wind.mvc.bean.MethodParameter;
import org.wind.tool.third.asm.ClassAdapter;
import org.wind.tool.third.asm.ClassReader;
import org.wind.tool.third.asm.ClassWriter;
import org.wind.tool.third.asm.Label;
import org.wind.tool.third.asm.MethodAdapter;
import org.wind.tool.third.asm.MethodVisitor;
import org.wind.tool.third.asm.Type;

/**
 * @描述 : 方法参数工具类
 * @作者 : 胡璐璐
 * @时间 : 2020年8月7日 15:52:37
 */
public final class MethodParameterUtil {
	
	/** 使用字节码工具ASM来获取方法的参数信息（含：参数名+类型） 。Map式，有序*/
    public static Map<String,MethodParameter> getMethodParam_Map(final Method method) throws Exception {
    	final String methodName = method.getName();
        final Class<?>[] methodParameterTypes = method.getParameterTypes();
        final int paramSize=methodParameterTypes.length;
        final String className = method.getDeclaringClass().getName();
        final Map<String,MethodParameter> mpList=new LinkedHashMap<String,MethodParameter>();
        
        ClassReader cr = new ClassReader(className);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassAdapter(cw) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = cw.visitMethod(access, name, desc, signature, exceptions);
                final Type[] argTypes = Type.getArgumentTypes(desc);

                //参数类型不一致
                if (!methodName.equals(name) || !matchTypes(argTypes, methodParameterTypes)) {
                    return mv;
                }
                return new MethodAdapter(mv) {
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        //如果是静态方法，第一个参数就是方法参数，非静态方法，则第一个参数是 this ,然后才是方法的参数
//                     System.out.println("【"+(index+1)+"】"+name+"—>"+desc);
                        //参数名不需要【this】块
                        if(!name.equals("this")) {
                        	//已获取到的参数小于总长度时，则继续执行
                            if(mpList.size()<paramSize) {
                            	MethodParameter t_mp=new MethodParameter(name);
                            	t_mp.setIndex(mpList.size());
                            	mpList.put(name,t_mp);
                            }
                        }
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }
                };
            }
        }, 0);
        return mpList;
    }
	/** 使用字节码工具ASM来获取方法的参数信息（含：参数名+类型） */
    public static List<MethodParameter> getMethodParam(final Method method) throws Exception {
        final String methodName = method.getName();
        final Class<?>[] methodParameterTypes = method.getParameterTypes();
        final int paramSize=methodParameterTypes.length;
        final String className = method.getDeclaringClass().getName();
//        final boolean isStatic = Modifier.isStatic(method.getModifiers());
        final List<MethodParameter> mpList=new ArrayList<MethodParameter>();

        ClassReader cr = new ClassReader(className);
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cr.accept(new ClassAdapter(cw) {
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = cw.visitMethod(access, name, desc, signature, exceptions);
                final Type[] argTypes = Type.getArgumentTypes(desc);

                //参数类型不一致
                if (!methodName.equals(name) || !matchTypes(argTypes, methodParameterTypes)) {
                    return mv;
                }
                return new MethodAdapter(mv) {
                    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                        //如果是静态方法，第一个参数就是方法参数，非静态方法，则第一个参数是 this ,然后才是方法的参数
//                     System.out.println("【"+(index+1)+"】"+name+"—>"+desc);
                        //参数名不需要【this】块
                        if(!name.equals("this")) {
                        	//已获取到的参数小于总长度时，则继续执行
                            if(mpList.size()<paramSize) {
                            	MethodParameter t_mp=new MethodParameter(name);
                            	t_mp.setIndex(mpList.size());
                            	mpList.add(t_mp);
                            }
                        }
                        super.visitLocalVariable(name, desc, signature, start, end, index);
                    }
                };
            }
        }, 0);
        return mpList;
    }

    /**比较参数是否一致**/
    private static boolean matchTypes(Type[] types, Class<?>[] parameterTypes) {
        if (types.length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(parameterTypes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }
	
}
