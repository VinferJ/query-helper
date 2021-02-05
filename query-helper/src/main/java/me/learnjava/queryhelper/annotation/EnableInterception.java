package me.learnjava.queryhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vinfer
 * @date 2021-02-02    01:54
 **/
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableInterception {

    /*
    * 查询拦截开启的开关标识注解
    * 当该注解在Mapper接口的类对象上使用时，那么会对该接口中所有的方法都进行查询拦截
    * 当该注解在某个方法上进行标注，那么只对该方法进行查询拦截
    * */

}
