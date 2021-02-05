package me.learnjava.queryhelper.core;


import me.learnjava.queryhelper.constant.InterceptMode;
import me.learnjava.queryhelper.core.processor.InterceptionPostProcessor;
import me.learnjava.queryhelper.support.Mapper;

/**
 * 查询辅助的定义接口，用于保存查询辅助的一些属性以及方法的元数据
 *
 * @author Vinfer
 * @date 2021-02-04    03:10
 **/
public interface QueryHelperDefinition {

    /**
     * QueryHelper注解是否用于实现分页查询
     * @return  用于实现分页查询返回true，否则返回false
     */
    boolean isPaginationQuery();

    /**
     * 分页查询模式
     * @return  {@link InterceptMode}
     */
    InterceptMode getInterceptMode();

    /**
     * 拦截查询的后置处理器
     * 与spring中的全局的后置处理器设计思想不同，这里的后置处理器是QueryHelper所私有的
     * 也就是支持没一个QueryHelper都可以设置一个自己的后置处理器
     * 除了使用默认的后置处理器{@link me.learnjava.queryhelper.core.processor.PaginationQueryPostProcessor}外
     * 使用自定义的后置处理器需要将该处理器注册到ioc容器中
     *
     * @return  {@link InterceptionPostProcessor}
     */
    InterceptionPostProcessor getInterceptionPostProcessor();

    /**
     * 封装处理分页查询的查询方法所在的接口一些数据的接口对象
     * @return  {@link Mapper}
     */
    Mapper getMapper();

}
