package me.learnjava.queryhelper.support;


/**
 * 用于在并发访问的情况下，以更细的粒度去封装锁对象/锁资源
 *
 * @author Vinfer
 * @date 2021-02-02    14:33
 **/
public class Holder<T> {

    /**Holder对象持有的值对象，使用volatile保证变量的可见性*/
    private volatile T value;

    public void set(T value){
        this.value = value;
    }

    public T get(){
        return value;
    }

}
