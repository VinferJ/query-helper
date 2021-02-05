package me.learnjava.queryhelper.support;

import lombok.Data;

import java.util.Collection;

/**
 * @author Vinfer
 * @date 2021-02-04    00:57
 **/
@Data
public class CommonPaginationQueryResult implements PaginationQueryResultAdapter{

    private Object allData = null;
    private Object paginationData;
    private int currentPage;
    private int lastPage;
    private long total;
    private int pageSize;

    public static CommonPaginationQueryResult of(Object result, PaginationQueryDataPack queryDataPack){
        CommonPaginationQueryResult queryResult = new CommonPaginationQueryResult();
        queryResult.paginationData = result;
        queryResult.currentPage = queryDataPack.getPageNum();
        queryResult.lastPage = queryDataPack.getTotalPage();
        queryResult.pageSize = queryDataPack.getPageSize();
        queryResult.total = queryDataPack.getTotal();
        queryResult.allData = queryDataPack.getDataCollection();
        return queryResult;
    }

}
