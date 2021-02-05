package me.learnjava.queryhelper.constant;

/**
 * 查询拦截模式：
 *  1.拦截修改SQL，拦截的是sql在生成prepareStatement对象的节端
 *  2.拦截修改结果，对查询返回的数据进行拦截
 *
 * @author Vinfer
 * @date 2021-02-04    03:21
 **/
public enum InterceptMode {

    /**修改sql*/
    MODIFY_SQL,

    /**修改结果集*/
    MODIFY_RESULT;

}
