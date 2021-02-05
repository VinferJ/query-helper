package me.learnjava.queryhelper.exception;

/**
 * @author Vinfer
 * @date 2021-02-02    14:26
 **/
public class PaginationQueryException extends RuntimeException{

    public PaginationQueryException(String msg, Throwable e){
        super(msg,e);
    }

    public PaginationQueryException(String msg){
        super(msg);
    }

}
