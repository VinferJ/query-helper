package me.learnjava.queryhelper.exception;

/**
 * @author Vinfer
 * @date 2021-02-04    10:26
 **/
public class ProcessorNotFoundException extends RuntimeException{

    public ProcessorNotFoundException(String msg,Exception exception){
        super(msg,exception);
    }

    public ProcessorNotFoundException(String msg){
        super(msg);
    }

}
