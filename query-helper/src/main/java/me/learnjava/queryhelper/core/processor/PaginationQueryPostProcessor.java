package me.learnjava.queryhelper.core.processor;

import me.learnjava.queryhelper.exception.QueryResultProcessException;
import me.learnjava.queryhelper.support.PaginationQueryDataPack;
import me.learnjava.queryhelper.support.PaginationQueryDataTransporter;

import java.util.*;

/**
 * 分页查询后置处理器，query-helper自带的一个默认的InterceptionPostProcessor
 * 
 * @author Vinfer
 * @date 2021-02-04    02:29
 **/
public class PaginationQueryPostProcessor implements InterceptionPostProcessor{

    @Override
    public String postProcessBeforeProceeding(String interceptedSql, Map<String, Object> parametersMap) throws Exception{

        //提交sql查询前的后置处理：修改原始sql，为sql添加limit和offset关键字进行分页查询

        //mybatis执行查询的sql绑定对象
        StringBuilder originalSql = new StringBuilder(interceptedSql);
        int pageNum = (int) parametersMap.get("pageNum");
        int pageSize = (int) parametersMap.get("pageSize");
        int lastPage = (int) parametersMap.get("lastPage");
        //表查询的起始行数
        int realOffset;
        if (pageNum >= 1 && pageNum < lastPage){
            realOffset = pageSize * (pageNum - 1);
        } else {
            //查询的页数大于或等于总页数时，使用最后一页
            realOffset = pageSize * (lastPage - 1);
        }
        //拼接sql，添加limit和offset实现分页查询
        return originalSql.append(" limit ").append(pageSize).append(" offset ").append(realOffset).toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterProceeding(Object queryResult) {

        //提交sql后的后置处理：根据查询得到的全部数据集进行数据分片

        PaginationQueryDataPack queryDataPack = PaginationQueryDataTransporter.get();
        //Mybatis对查询结果的保存都是使用List
        if (List.class.isAssignableFrom(queryResult.getClass())){
            List<Object> dataCollection = (List<Object>) queryResult;
            long total = dataCollection.size();
            queryDataPack.setTotal(total);
            //使用MODIFY_RESULT查询模式时，所有查询数据的属性字段会被赋值
            queryDataPack.setDataCollection(dataCollection);
            int pageSize = queryDataPack.getPageSize();
            int currentPage = queryDataPack.getPageNum();
            //由于是对结果集做数据分片，因此lastPage需要计算
            int alonePageSize = (int) (total % pageSize);
            int plusPage = alonePageSize > 0 ? 1 : 0;
            int lastPage = (int)(total / pageSize) + plusPage;
            //将lastPage保存到queryDataPack中
            queryDataPack.setTotalPage(lastPage);
            //计算offset和limit
            //offset：数据分片的起始下标  limit：数据分片的截止下标
            int offset = pageSize * (currentPage - 1);
            int limit = pageSize;
            Collection<Object> pageData = new ArrayList<>(pageSize);
            //如果当前页码大于等于最后一页，那么使用最后一页
            if (currentPage >= lastPage){
                //将最后一页设置为当前页码
                queryDataPack.setPageNum(lastPage);
                offset = pageSize * (lastPage - 1);
                if (alonePageSize > 0){
                    limit = alonePageSize;
                }
            }
            for (int i = offset; i < limit + offset; i++) {
                pageData.add(dataCollection.get(i));
            }
            return pageData;
        }

        throw new QueryResultProcessException("Can't handle this query returning type: ["+queryResult.getClass()+"], " +
                "Only support [java.util.List] now");
    }
}
