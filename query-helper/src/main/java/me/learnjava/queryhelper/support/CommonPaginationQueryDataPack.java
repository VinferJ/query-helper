package me.learnjava.queryhelper.support;

import lombok.Data;
import me.learnjava.queryhelper.annotation.QueryHelper;

import java.util.Collection;
import java.util.Map;

/**
 * @author Vinfer
 * @date 2021-02-02    14:12
 **/
@Data
public class CommonPaginationQueryDataPack implements PaginationQueryDataPack {

    private int pageNum = 1;

    private int pageSize;

    private long total = 0L;

    private int totalPage = 1;

    private Class<?> mapperClass = null;

    private QueryHelper queryHelper;

    private Collection<Object> dataCollection = null;

    private Map<String,Object> annotatedMethodArgs;

    private String adviceMethodName;

}
