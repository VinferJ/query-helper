package me.learnjava.queryhelper.demo.util;


import java.util.function.Supplier;

/**
 * @author Vinfer
 * @date 2021-02-02    16:08
 **/
public class AssertUtil {
    
    public static void isTrue(boolean expression, Supplier<RuntimeException> exceptionSupplier){
        if (!expression){
            throw safeGet(exceptionSupplier);
        }
    }

    private static RuntimeException safeGet(Supplier<RuntimeException> exceptionSupplier) {
        return exceptionSupplier != null ? exceptionSupplier.get() : new IllegalArgumentException();
    }
    
}
