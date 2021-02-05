package me.learnjava.queryhelper.support;

import lombok.Getter;
import me.learnjava.queryhelper.annotation.EnableInterception;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vinfer
 * @date 2021-02-02    14:43
 **/
@Getter
public class CommonMapper implements Mapper {

    private final String mapperName;

    private final Class<?> mapperClass;

    private final Map<String, Method> interceptionMethods = new HashMap<>(8);

    private final Map<String, Annotation[]> interceptionMethodAnnotations = new HashMap<>(8);

    private final boolean enableAllMethodIntercept;

    public CommonMapper(Class<?> mapperClass){
        this.mapperClass = mapperClass;
        this.mapperName = mapperClass.getName();
        this.enableAllMethodIntercept = mapperClass.isAnnotationPresent(EnableInterception.class);
        ReflectionUtils.doWithMethods(mapperClass, method -> {
            String methodName = method.getName();
            if (method.isAnnotationPresent(EnableInterception.class)){
                interceptionMethods.put(methodName,method);
                interceptionMethodAnnotations.put(methodName, method.getDeclaredAnnotations());
            }
        });
    }

}
