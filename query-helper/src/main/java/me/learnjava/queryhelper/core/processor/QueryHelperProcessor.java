package me.learnjava.queryhelper.core.processor;




import me.learnjava.queryhelper.annotation.QueryHelper;
import me.learnjava.queryhelper.constant.InterceptMode;
import me.learnjava.queryhelper.exception.PaginationQueryException;
import me.learnjava.queryhelper.support.*;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 分页查询的切面处理器，主要是负责分页查询时的参数封装和传递
 *
 * @author Vinfer
 * @date 2021-02-02    09:45
 **/
@Aspect
public class QueryHelperProcessor implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**Mapper接口中计算表总行数的方法，key为Mapper接口的类名*/
    private final ConcurrentMap<String, Method> counterMethodMaps = new ConcurrentHashMap<>();

    @Before(value = "@annotation(me.learnjava.queryhelper.annotation.QueryHelper)")
    public void packingPaginationQueryData(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        QueryHelper queryHelper = signature.getMethod().getAnnotation(QueryHelper.class);
        Class<?> mapperClass = queryHelper.mapperClass();
        //通过反射获取@QueryHelper标注的方法中的参数并封装成一个queryDataPack
        PaginationQueryDataPack queryDataPack = buildPaginationQueryDataPack(signature, joinPoint.getArgs(),queryHelper);
        queryDataPack.setAdviceMethodName(signature.getMethod().getName());
        //将mapperClass通过该transporter设置并传递给拦截器
        queryDataPack.setMapperClass(mapperClass);
        queryDataPack.setQueryHelper(queryHelper);

        //当使用拦截模式为MODIFY_SQL时，需要在该aop处理中就要执行设置的行数查询的方法来获取数据集的总行数以及计算lastPage
        //此时要求QueryHelper中必须设置了rowCounterMethodName
        if (queryHelper.isForPaginationQuery() && queryHelper.interceptMode().equals(InterceptMode.MODIFY_SQL)){
            //从ioc中获取到mapper接口对应的Bean，即mybatis为mapper接口生成的FactoryBean（代理类）
            Object mapperBean = applicationContext.getBean(mapperClass);
            //@QueryHelper注解配置的查询表行数的方法
            String configuredCounterMethodName = queryHelper.rowCounterMethodName();
            //先从缓存中查找counter方法
            Method counterMethod = counterMethodMaps.get(configuredCounterMethodName);
            if (counterMethod == null) {
                //缓存中没有那么从mapperClass中寻找并将结果缓存起来
                counterMethod = findCounterMethod(mapperClass,configuredCounterMethodName);
            }
            doCounterQuery(counterMethod, queryDataPack, mapperBean);
        }
        PaginationQueryDataTransporter.set(queryDataPack);
        //当使用查询模式MODIFY_RESULT时，不需要调用查询行数的方法，该模式是通过将查到的所有数据进行数据分片而实现的分页
        //该模式不需要借口设置@RowCounter方法，并且会通过拦截返回结果来进行数据分片，aop中只需要将queryDataPack封装设置好即可
    }


    @NonNull
    private PaginationQueryDataPack buildPaginationQueryDataPack(MethodSignature methodSignature,Object[] args,QueryHelper queryHelper){
        PaginationQueryDataPack queryDataPack = new CommonPaginationQueryDataPack();
        String pageNumParamName = queryHelper.pageNumParamName();
        String pageSizeParamName = queryHelper.pageSizeParamName();
        //参数名称检查
        Assert.isTrue(StringUtils.hasLength(pageNumParamName), "PageNumParamName must has a value!");
        Assert.isTrue(StringUtils.hasLength(pageSizeParamName),"PageSizeParamName must has a value!");
        Method businessMethod = methodSignature.getMethod();
        Parameter[] parameters = businessMethod.getParameters();
        Map<String,Object> parametersMap = new HashMap<>(args.length);
        for (int i = 0; i < parameters.length; i++) {
            parametersMap.put(parameters[i].getName(),args[i]);
        }
        queryDataPack.setAnnotatedMethodArgs(parametersMap);
        //如果是用于分页查询的，那么单独将分页查询传入的参数从Map中取出
        if (queryHelper.isForPaginationQuery()){
            if (parametersMap.containsKey(pageNumParamName)){
                queryDataPack.setPageNum((Integer) parametersMap.get(pageNumParamName));
            }else {
                throw new PaginationQueryException("Requires query parameters when doing pagination query, but can't found a parameter " +
                        "naming ["+pageNumParamName+"] in method ["+businessMethod.getName()+"]");
            }
            if (parametersMap.containsKey(pageSizeParamName)){
                queryDataPack.setPageSize((Integer) parametersMap.get(pageSizeParamName));
            }else {
                throw new PaginationQueryException("Requires query parameters when doing pagination query, but can't found a parameter " +
                        "named ["+pageSizeParamName+"] in method ["+businessMethod.getName()+"]");
            }
        }
        return queryDataPack;
    }

    private void doCounterQuery(Method counterMethod, PaginationQueryDataPack queryDataPack, Object mapperBean){
        try {
            //通过反射执行表行数统计的方法
            long rowNum = (long) counterMethod.invoke(mapperBean);
            //将查询到的总行数设置到pageArgHolder中
            queryDataPack.setTotal(rowNum);
            int pageSize = queryDataPack.getPageSize();
            //如果总页数对分页大小取余不为0，说明最后一页是孤立页，需要加1
            int plusPage = rowNum % pageSize != 0 ? 1 : 0;
            //最后一页的页码，同时也是总的页数
            int lastPage = (int) (rowNum / pageSize) + plusPage;
            queryDataPack.setTotalPage(lastPage);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new PaginationQueryException("Error occurred when invoke row counter method of mapper", e);
        }
    }

    private Method findCounterMethod(Class<?> mapperClass,String methodName){
        //找到mapper接口中由@RowCounter标注的方法进行当前表行数查询
        List<Method> methodList = Arrays.stream(mapperClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .collect(Collectors.toList());
        if (methodList.size() > 0){
            Method counterMethod = methodList.get(0);
            counterMethodMaps.putIfAbsent(methodName, counterMethod);
            return counterMethod;
        }else {
            throw new PaginationQueryException("Can not find a method named ["+methodName+"] in Class ["+mapperClass.getName()+"]");
        }
    }

    @Around(value = "@annotation(me.learnjava.queryhelper.annotation.QueryHelper)")
    public Object afterPageQuery(ProceedingJoinPoint joinPoint){
        Object queryResult;
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        QueryHelper queryHelper = signature.getMethod().getDeclaredAnnotation(QueryHelper.class);
        try {
            queryResult = joinPoint.proceed();
            //当且仅当advice方法的返回值类型是PaginationQueryResultAdapter或者其实现类时，为返回值结果做数据包装
            boolean wrapFlag =
                    //用于分页查询
                    queryHelper.isForPaginationQuery()
                            //返回值是Object或者是List类型
                            && (returnType.isAssignableFrom(Object.class) || returnType.isAssignableFrom(List.class));
            if (wrapFlag){
                queryResult = CommonPaginationQueryResult.of(queryResult, PaginationQueryDataTransporter.get());
            }
            return queryResult;
        } catch (Throwable throwable) {
            throw new PaginationQueryException("Exception Caused by proceeding in pagination query",throwable);
        } finally {
            //清除ThreadLocal的缓存
            PaginationQueryDataTransporter.remove();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
