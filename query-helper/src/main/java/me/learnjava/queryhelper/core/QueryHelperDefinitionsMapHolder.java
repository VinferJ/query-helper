package me.learnjava.queryhelper.core;

import me.learnjava.queryhelper.annotation.QueryHelper;
import me.learnjava.queryhelper.support.Holder;
import org.springframework.beans.factory.BeanFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 需要在多个拦截器中共用同一个queryHelperDefinitionsMap
 * 因此需要一个holder对象来维护该Map，而该holder对象的维护则可以交给spring来管理
 *
 * @author Vinfer
 * @date 2021-02-05    10:18
 **/
public class QueryHelperDefinitionsMapHolder {

    private final ConcurrentMap<String, Holder<QueryHelperDefinition>> queryHelperDefinitionsMap = new ConcurrentHashMap<>(8);

    public QueryHelperDefinition getDefinition(Class<?> mapperClass, QueryHelper queryHelper, BeanFactory beanFactory,String adviceMethodName){
        String definitionName = mapperClass.getName() + "." + adviceMethodName;
        Holder<QueryHelperDefinition> definitionHolder = queryHelperDefinitionsMap.get(definitionName);
        if (definitionHolder == null){
            //Holder对象为空时，向map中放入一个空的Holder
            queryHelperDefinitionsMap.putIfAbsent(definitionName, new Holder<>());
            definitionHolder = queryHelperDefinitionsMap.get(definitionName);
        }
        QueryHelperDefinition qhd = definitionHolder.get();
        //DLC，保证qhd是单例对象
        if (qhd == null){
            synchronized (definitionHolder){
                qhd = definitionHolder.get();
                if (qhd == null){
                    //创建qhd
                    qhd = new GenericQueryHelperDefinition(mapperClass, queryHelper, beanFactory);
                    //保存到holder中
                    definitionHolder.set(qhd);
                    return qhd;
                }
            }
        }
        return qhd;
    }

}
