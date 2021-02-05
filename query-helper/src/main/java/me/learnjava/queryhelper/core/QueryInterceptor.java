package me.learnjava.queryhelper.core;

import me.learnjava.queryhelper.annotation.QueryHelper;
import me.learnjava.queryhelper.constant.InterceptMode;
import me.learnjava.queryhelper.core.processor.InterceptionPostProcessor;
import me.learnjava.queryhelper.support.PaginationQueryDataTransporter;
import me.learnjava.queryhelper.support.Mapper;
import me.learnjava.queryhelper.support.PaginationQueryDataPack;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过拦截StatementHandler的prepare方法进行sql劫持，从而对sql进行修实现动态分页查询
 * 又或者是通过对查询得到的数据集进行数据分片实现分页查询
 *
 * Mybatis的拦截器支持拦截四类Signature：
 *  1.Executor:
 *      Mybatis的内部执行器，它负责调用StatementHandler操作数据库，并把结果集通过ResultSetHandler进行自动映射，
 *      另外，它还处理了二级缓存的操作。同时Executor创建StatementHandler对象,而且创建ParameterHandler和ResultSetHandler对象,
 *      而ParameterHandler和ResultSetHandler都依赖TypeHandler
 *  2.StatementHandler:
 *      是Mybatis直接和数据库执行sql脚本的对象，另外，该对象还实现了Mybatis的一级缓存
 *  3.ParameterHandler:
 *      Mybatis实现sql入参设置的对象
 *  4.ResultSetHandler:
 *      是Mybatis把ResultSet集合映射成POJO的接口对象，处理查询结果集
 *
 * 拦截对象所支持拦截的方法需要去看Mybatis这4个类对应的源码，而下面注解中的method和args的设置就是对应源码中的方法和参数列表
 * Invocation对象：
 *  这是Mybatis进行代理执行封装的调用对象，拦截不同的对象，Invocation中的各个参数的实例都不一样，
 *  这需要通过调试或者阅读源码来查看具体的实例
 *
 *
 * @author Vinfer
 * @date 2021-02-04    09:35
 **/
@Intercepts({
        @Signature(type = StatementHandler.class,method = "prepare",args = {Connection.class,Integer.class})
})
public class QueryInterceptor implements Interceptor, BeanFactoryAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryInterceptor.class);

    private final QueryHelperDefinitionsMapHolder definitionsMapHolder;

    private BeanFactory beanFactory;

    public QueryInterceptor(QueryHelperDefinitionsMapHolder definitionsMapHolder){
        this.definitionsMapHolder = definitionsMapHolder;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取ThreadLocal中传递的数据
        PaginationQueryDataPack queryDataPack = PaginationQueryDataTransporter.get();
        if (queryDataPack == null){
            //如果该dataPack为空，说明调用该被拦截的查询方法的业务逻辑方法（service层）上没有标注@QueryHelper注解
            //也就是说没有标注@QueryHelper注解的方法调用的查询方法在这里就会直接返回，查询的性能几乎不会受影响
            return invocation.proceed();
        }

        //拿到dao层被拦截的查询方法对应的mapper接口类对象，用于生成Mapper对象
        Class<?> mapperClass = queryDataPack.getMapperClass();
        //查询赋值注解，用于获取或创建qhd
        QueryHelper queryHelper = queryDataPack.getQueryHelper();
        QueryHelperDefinition qhd = definitionsMapHolder.getDefinition(mapperClass, queryHelper,beanFactory, queryDataPack.getAdviceMethodName());
        InterceptMode queryMode = qhd.getInterceptMode();
        //判断拦截模式，因为查询拦截时分两种模式的，这里只对MODIFY_SQL模式的查询进行处理
        if (queryMode.equals(InterceptMode.MODIFY_SQL)){
            Mapper mapper = qhd.getMapper();
            //通过该statementHandler对象获取到sql，然后通过反射来进行动态修改要执行的sql
            StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
            //通过statementHandler获取MetaObject封装类
            MetaObject metaObject = MetaObject.forObject(statementHandler,
                    SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                    SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                    new DefaultReflectorFactory());
            //拿到MappedStatement，该对象中存储着sql的类型以及mapper接口中执行的方法
            //getValue中的值需要通过断点调试来确定
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            //当前被拦截的查询方法名称
            String queryMethodName = mappedStatement.getId();
            //截取最后部分的方法名称
            String simpleMethodName = queryMethodName.substring(queryMethodName.lastIndexOf(".") + 1);
            //查询当前方法是否开启了拦截
            if (
                //判断是否在接口上使用@EnableInterception注解
                    mapper.isEnableAllMethodIntercept() ||
                            //判断当前被拦截的查询方法是否使用了@EnableInterception注解
                            mapper.getInterceptionMethods().containsKey(simpleMethodName)
            ) {
                doIntercept(qhd, statementHandler, queryDataPack);
            }
        }
        //执行方法推进，proceed之后会生成一个prepareStatement对象，下一步会交由ParameterHandler进行参数注入
        return invocation.proceed();
    }

    private void doIntercept(QueryHelperDefinition qhd,StatementHandler statementHandler,PaginationQueryDataPack queryDataPack) throws Throwable{
        InterceptionPostProcessor ipp = qhd.getInterceptionPostProcessor();
        //绑定了执行sql的对象
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();
        Map<String,Object> parametersMap = new HashMap<>(8);
        if (qhd.isPaginationQuery()){
            //如果用于分页查询，那么传递分页查询专用的特定参数
            parametersMap.put("pageNum",queryDataPack.getPageNum());
            parametersMap.put("pageSize", queryDataPack.getPageSize());
            parametersMap.put("lastPage", queryDataPack.getTotalPage());
        }else {
            //如果不用于分页查询，那么默认传递在aop处理器中封装的advice方法里的所有参数
            parametersMap = queryDataPack.getAnnotatedMethodArgs();
        }
        //执行sql提交前的后置处理
        //并且该拦截器拦截的是prepare方法，因此只执行查询前的后置处理
        String execSql = ipp.postProcessBeforeProceeding(originalSql, parametersMap);
        Field sql = boundSql.getClass().getDeclaredField("sql");
        sql.setAccessible(true);
        //通过反射修改最终要执行的sql，实现真实的分页查询
        sql.set(boundSql, execSql);
        LOGGER.info("exec modified sql: " + boundSql.getSql());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
