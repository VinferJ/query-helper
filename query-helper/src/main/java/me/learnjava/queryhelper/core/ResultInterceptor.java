package me.learnjava.queryhelper.core;

import me.learnjava.queryhelper.annotation.QueryHelper;
import me.learnjava.queryhelper.constant.InterceptMode;
import me.learnjava.queryhelper.core.processor.InterceptionPostProcessor;
import me.learnjava.queryhelper.support.Mapper;
import me.learnjava.queryhelper.support.PaginationQueryDataPack;
import me.learnjava.queryhelper.support.PaginationQueryDataTransporter;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.sql.Statement;

/**
 * 对于查询结果的拦截是比较简单的，只需要拦截ResultSetHandler.handleOutputParameters方法即可
 * 并且一般拦截查询结果都是拦截有Mybatis做好映射封装之后的结果，所以拦截的是Invocation.proceed返回的对象
 * 因为在proceed之后返回的才是被映射为POJO的结果
 *
 * @author Vinfer
 * @date 2021-02-05    09:45
 **/
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class ResultInterceptor implements Interceptor, BeanFactoryAware {

    private final QueryHelperDefinitionsMapHolder definitionsMapHolder;

    private BeanFactory beanFactory;

    public ResultInterceptor(QueryHelperDefinitionsMapHolder definitionsMapHolder){
        this.definitionsMapHolder = definitionsMapHolder;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取已经被映射成POJO后查询结果，Mybatis对查询结果都统一使用List做外层封装
        Object queryResult = invocation.proceed();
        //获取ThreadLocal中传递的数据
        PaginationQueryDataPack queryDataPack = PaginationQueryDataTransporter.get();
        if (queryDataPack == null){
            //queryDataPack为空，说明业务方法没有标注@QueryHelper注解，直接返回
            return queryResult;
        }
        Class<?> mapperClass = queryDataPack.getMapperClass();
        QueryHelper queryHelper = queryDataPack.getQueryHelper();
        QueryHelperDefinition qhd = definitionsMapHolder.getDefinition(mapperClass, queryHelper, beanFactory, queryDataPack.getAdviceMethodName());
        InterceptMode interceptMode = qhd.getInterceptMode();
        //在该sql结果拦截器中只处理MODIFY_RESULT模式下的分页处理，实质就是对查询结果进行数据分片
        if (interceptMode.equals(InterceptMode.MODIFY_RESULT)){
            Mapper mapper = qhd.getMapper();
            ResultSetHandler resultSetHandler = (ResultSetHandler) invocation.getTarget();
            //需要拿到resultSetHandler中的mappedStatement，同样需要封装为metaObject
            MetaObject metaObject = MetaObject.forObject(resultSetHandler,
                    SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                    SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                    new DefaultReflectorFactory());
            //通过metaObject获取mappedStatement
            MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");
            String queryMethodName = mappedStatement.getId();
            String simpleMethodName = queryMethodName.substring(queryMethodName.lastIndexOf(".") + 1);
            //查询当前方法是否开启了拦截
            if (mapper.isEnableAllMethodIntercept() ||
                    mapper.getInterceptionMethods().containsKey(simpleMethodName)) {
                InterceptionPostProcessor interceptionPostProcessor = qhd.getInterceptionPostProcessor();
                //提交对sql查询后的后置处理
                queryResult = interceptionPostProcessor.postProcessAfterProceeding(queryResult);
            }
        }
        return queryResult;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
