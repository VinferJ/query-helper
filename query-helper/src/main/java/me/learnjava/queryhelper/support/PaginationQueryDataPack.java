package me.learnjava.queryhelper.support;

import me.learnjava.queryhelper.annotation.QueryHelper;

import java.util.Collection;
import java.util.Map;

/**
 * 分页查询的参数和结果集的数据封装接口(类似于上下文接口)
 *
 * @author Vinfer
 * @date 2021-02-02    14:05
 **/
public interface PaginationQueryDataPack {

    /**
     * 获取页码
     * @return  当前页码
     */
    int getPageNum();

    /**
     * 获取分页大小
     * @return  分页大小/数据分片大小
     */
    int getPageSize();

    /**
     * 数据总量数
     * @return  数据总量
     */
    long getTotal();

    /**
     * 获取总页数
     * @return  总页数
     */
    int getTotalPage();

    /**
     * 处理查询的Mapper接口的类对象
     * @return  Mapper接口的类对象
     */
    Class<?> getMapperClass();

    /**
     * 获取QueryHelper注解
     * @return  {@link QueryHelper}
     */
    QueryHelper getQueryHelper();

    /**
     * queryHelper注解标注的方法的方法名称，用于生成queryHelperDefinition对象保存的key值
     * @return  queryHelper注解标注的方法的方法名称
     */
    String getAdviceMethodName();

    /**
     * 由{@link QueryHelper}注解标记的方法中的所有方法参数
     * @return  方法参数的对象数组
     */
    Map<String,Object> getAnnotatedMethodArgs();

    /**
     * 获取用于统计总数和进行数据分片的数据集
     * @return 总的数据集
     */
    Collection<Object> getDataCollection();

    /**
     * 设置当前查询的页码
     * @param pageNum   当前查询页码
     */
    void setPageNum(int pageNum);

    /**
     * 设置分页大小
     * @param pageSize  分页大小
     */
    void setPageSize(int pageSize);

    /**
     * 设置数据总量
     * @param total 数据总量
     */
    void setTotal(long total);

    /**
     * 设置总页数
     * @param totalPage 总页数
     */
    void setTotalPage(int totalPage);

    /**
     * 设置Mapper接口类对象
     * @param mapperClass   Mapper接口的类对象
     */
    void setMapperClass(Class<?> mapperClass);

    /**
     * 设置QueryHelper
     * @param queryHelper   {@link QueryHelper}
     */
    void setQueryHelper(QueryHelper queryHelper);

    /**
     * 设置用于统计总数和进行数据分片的数据集
     * @param objectCollection   总的数据集
     */
    void setDataCollection(Collection<Object> objectCollection);

    /**
     * 设置{@link QueryHelper}标注的方法的方法参数
     * @param args  方法参数数组
     */
    void setAnnotatedMethodArgs(Map<String,Object> args);

    /**
     * 设置注解方法的名称
     * @param adviceMethodName  queryHelper注解方法的名称
     */
    void setAdviceMethodName(String adviceMethodName);

}
