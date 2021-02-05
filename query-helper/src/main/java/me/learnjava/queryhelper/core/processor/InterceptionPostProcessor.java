package me.learnjava.queryhelper.core.processor;

import java.util.Map;

/**
 * 拦截后置处理器，对sql查询进行拦截时，通过后置处理器提交处理
 * query-helper使用与spring中类似的PostProcessor机制，通过定义具体的后置处理器
 * 对拦截的sql进行不同的处理，以达到类似插件的可插拔的配置形式，同时也符合开闭原则的设计思想
 * 如果用户要定义自己的后置处理器进行拦截逻辑处理，只需要实现该接口并且将该实现类注册成为Bean加载到ioc中去即可
 * 在任何情况下，query-helper都会优先使用用户自定义的后置处理器
 *
 * @author Vinfer
 * @date 2021-02-04    02:08
 **/
public interface InterceptionPostProcessor {

    /**
     * 在执行sql查询前提交后置处理
     * @param interceptedSql    被拦截的sql
     * @param parametersMap     对sql进行后置处理所需参数的Map，对于自定义且不是用于分页查询的的拦截处理，
     *                          该Map传递的是{@link me.learnjava.queryhelper.annotation.QueryHelper}注解标注的方法中所有的参数
     * @throws Exception        处理中会触发的异常
     * @return                  处理后的sql
     */
    default String postProcessBeforeProceeding(String interceptedSql, Map<String,Object> parametersMap) throws Exception{
        return interceptedSql;
    }

    /**
     * 在执行sql查询之后对查询结果提交后置处理
     * @param queryResult       查询得到的结果集，Mybatis对查询的结果都使用List来保存和传递
     *                          例如，dao接口的查询方法返回值是是long，假设查到的是1，那么这里拦截的结果应该是[1]
     *                          如果查询方法返回值的是一个User对象，拦截到的是[User{...}]
     *                          如果查询方法返回值是List<User>,那么这里拦截的就是List<User>,即[0=User{...},1=User{...},...]
     * @return                  处理后的数据对象
     * @throws Exception        处理中会触发的异常
     */
    default Object postProcessAfterProceeding(Object queryResult)throws Exception{
        return queryResult;
    }

}
