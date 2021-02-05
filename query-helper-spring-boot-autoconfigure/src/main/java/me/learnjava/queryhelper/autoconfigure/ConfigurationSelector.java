package me.learnjava.queryhelper.autoconfigure;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * springboot自动装配的类导入选择器，当在springboot启动类上使用{@link EnableQueryHelper}注解时
 * 将会通过springboot的自动装配机制将下面配置的类加载到spring-ioc中
 *
 * @author Vinfer
 * @date 2021-02-03    22:39
 **/
public class ConfigurationSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{
                //自动配置类，需要springboot自动生效必须要把配置类导入到ioc中
                //如果不使用javaConfig进行配置，也可以通过spring.factories文件进行配置，但是这种方式一定会被加载
                //而通过@Import注解配置则可以实现@EnableXXX的功能，可以更加优雅地使用自定义组件
                "me.learnjava.queryhelper.autoconfigure.QueryHelperAutoConfiguration",
        };
    }
}
