package me.learnjava.queryhelper.support;


import me.learnjava.queryhelper.support.PaginationQueryDataPack;

/**
 * @author Vinfer
 * @date 2021-02-02    09:31
 **/
public class PaginationQueryDataTransporter {

    /**
     * 使用ThreadLocal将分页查询所需的参数传递给Mybatis的拦截器
     */
    public static final ThreadLocal<PaginationQueryDataPack> THREAD_LOCAL_HOLDER = new ThreadLocal<>();

    public static void set(PaginationQueryDataPack input){
        THREAD_LOCAL_HOLDER.set(input);
    }

    public static PaginationQueryDataPack get(){
        return THREAD_LOCAL_HOLDER.get();
    }

    public static void remove(){
        THREAD_LOCAL_HOLDER.remove();
    }

}
