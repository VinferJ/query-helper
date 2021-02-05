package me.learnjava.queryhelper.support;



import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 用于封装处理分页查询的mapper接口信息
 *
 * @author Vinfer
 * @date 2021-02-02    14:34
 **/
public interface Mapper {

    /**
     * 获取Mapper接口的名称
     * @return  Mapper接口的全限类名
     */
    String getMapperName();

    /**
     * 处理分页查询的Mapper接口的类对象
     * @return  Mapper接口的类对象
     */
    Class<?> getMapperClass();

    /**
     * 获取Mapper接口中开启了的所有方法对象
     * @return  Mapper接口中的所有方法对象
     */
    Map<String,Method> getInterceptionMethods();

    /**
     * 获取Mapper接口中的开启拦截的方法对象上标记的所有注解
     * @return  Mapper接口中的方法对象上标记的所有注解
     */
    Map<String,Annotation[]> getInterceptionMethodAnnotations();

    /**
     * 是否对Mapper中所有的方法都开启拦截，默认为关闭
     * 当{@link me.learnjava.queryhelper.annotation.EnableInterception}直接标记在Mapper接口上时
     * 即开启对全部方法的拦截
     * @return  是否开启了接口所有方法的拦截，开启了返回true，关闭时返回false
     */
    boolean isEnableAllMethodIntercept();

}
