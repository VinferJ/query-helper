package me.learnjava.queryhelper.annotation;



import me.learnjava.queryhelper.constant.InterceptMode;
import me.learnjava.queryhelper.core.processor.InterceptionPostProcessor;
import me.learnjava.queryhelper.core.processor.PaginationQueryPostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询辅助注解，基于该注解，可以实现对业务低侵入的分页查询
 * 由于对QueryHelper中属性会使用一个Definition对象来保存该配置，更改后需要重新启动ioc容器才能生效
 * 因此注解属性的修改不支持热部署
 *
 * @author Vinfer
 * @date 2021-02-02    09:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QueryHelper {

    /**
     * 分页查询必要参数：当前查询的页码
     * 该方法用于获取标注方法中参数名称为pageNum的参数
     * 如果不使用默认值，请一定要将该属性值和和参数的实际名称设置一致
     * 例如：service类中的某个分页查询方法：
     *  \@QueryHelper
     *  public List<UserDto> userPaginationQuery(int pageNum,int pageSize){
     *      .....
     *  }
     *  该方法参数中的参数名称要和注解中的设置一致，这样注解才可以通过反射获取到正确的参数
     *
     * @return  当前页码的参数名称
     */
    String pageNumParamName() default "pageNum";

    /**
     * 分页查询必要参数：分页大小
     * 该方法用于获取标注方法中参数名称为pageSize的方法参数
     * 如果不使用默认值，请一定要将该属性值和和参数的实际名称设置一致
     * @return  分页大小的参数名称
     */
    String pageSizeParamName() default "pageSize";

    /**
     * Mapper接口中用于查询需要做分页查询的表的行数的方法名称，
     * 该方法必须是下面mapperClass设置的类中的方法
     * 当该注解用于分页查询功能时，并且查询模式为MODIFY_SQL
     * 需要设置该值，如果设置有误，会导致行数查询失败从而导致分页查询失败
     * @return  dao接口中查询表行数的方法名称
     */
    String rowCounterMethodName() default "";

    /**
     * 当前注解是否用于分页查询功能的实现
     * 如果该值为false，QueryHelper的默认切面处理器将不会进行表行数的查询和分页查询相关的具体参数设置
     * 但是用户可以通过定义具体的拦截处理器{@link me.learnjava.queryhelper.core.processor.InterceptionPostProcessor}
     * 来继续实现自定义的拦截逻辑
     * @return  注解用于分页查询功能时返回true，否则返回false
     */
    boolean isForPaginationQuery() default true;

    /**
     * 拦截模式，如果要修改sql，使用MODIFY_SQL模式
     * 如果要修改返回结果，使用MODIFY_RESULT模式
     * 查询拦截器{@link me.learnjava.queryhelper.core.QueryInterceptor}只在MODIFY_SQL模式下生效或工作
     * 而结果拦截器{@link me.learnjava.queryhelper.core.ResultInterceptor} 只在MODIFY_RESULT模式下生效或工作
     * @return  {@link InterceptMode}
     */
    InterceptMode interceptMode() default InterceptMode.MODIFY_SQL;

    /**
     * dao层mapper接口的class对象，用于反射寻找统计表总行数的方法
     * @return  mapper接口的class对象
     */
    Class<?> mapperClass();

    /**
     * 查询拦截后置处理器的配置，要求必须是{@link InterceptionPostProcessor}的实现类
     * 默认是内置的分页查询拦截后置处理器类
     * @return  处理器类
     */
    Class<? extends InterceptionPostProcessor> processor() default PaginationQueryPostProcessor.class;


}
