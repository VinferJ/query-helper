package me.learnjava.queryhelper.support;



import me.learnjava.queryhelper.annotation.QueryHelper;

import java.util.Collection;

/**
 * @author Vinfer
 * @date 2021-02-04    00:40
 **/
public interface PaginationQueryResultAdapter {

    /**
     * 获取分页查询所查的数据集，当{@link QueryHelper}
     * 中没开启整表查询统计行数时，该值为null
     * @return  数据集的全部数据的集合
     */
    Object getAllData();

    /**
     * 获取分页查询的单页数据
     * @return  单页的数据集合
     */
    Object getPaginationData();

    /**
     * 获取当前页码
     * @return  页码值（大于等于1）
     */
    int getCurrentPage();

    /**
     * 获取最后一页页码
     * @return  页码值（大于等于1）
     */
    int getLastPage();

    /**
     * 获取整表数据的总数（总行数）
     * @return  数据集数据总行数
     */
    long getTotal();

    /**
     * 获取分页大小/数据分片大小
     * @return  分页大小
     */
    int getPageSize();

}
