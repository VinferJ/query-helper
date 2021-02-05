package me.learnjava.queryhelper.demo;

import me.learnjava.queryhelper.autoconfigure.EnableQueryHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 使用@EnableQueryHelper开启查询辅助插件的使用，只要开启该注解即可
 * 不需要在Mybatis的配置文件中在进行插件的配置，如果开启了该注解，又在
 * 配置文件中配置了相同的插件，那么这两个插件会同时被Mybatis识别加载
 * 因为开启了该注解，query-helper会将插件注册到spring的ioc中，Mybatis与spring
 * 整合时支持识别ioc中实现了Mybatis的Interceptor接口的Bean
 *
 * @author Vinfer
 */
@EnableQueryHelper
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
