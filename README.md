# QueryHelper



## 简介

一个基于mybatis+springboot实现的基于注解，低业务侵入的用于查询拦截的mybatis拦截器插件

该插件内置了分页查询功能的实现，可以轻松使用该查询来进行数据分页查询，与常用的github的PageHelper插件不一样，query-helper基于注解开发和使用，不需要在业务逻辑中编写分页查询的逻辑代码，实现了业务的低侵入性，并且不需要在Mybatis配置文件中进行任何对插件相关的配置。

query-helper内部使用了仿照spring的XXXPostProcessor的设计，因此除了使用内置的默认的拦截后置处理器来实现分页查询功能，还可以支持用户自定义的拦截后置处理器，可以用于实现用户自己想要的拦截处理逻辑，比如配合业务逻辑通过修改sql来实现一定程度的数据权限拦截，query-helper可以让您更简单实现对sql查询的拦截



## 如何使用

该项目没有发布到maven的公服仓库中，因此只能通过下载到本地进行maven依赖安装的方式进行使用

依赖安装好后，在项目中将starter依赖包导入即可：

```xml

<dependency>
	<groupId>me.learnjava</groupId>
	<artifactId>query-helper-spring-boot-starter</artifactId>
	<version>1.0.0</version>
</dependency>

```



query-helper的使用非常简单，只需要两个注解：

- @EnableQueryHelper：在启动类上使用，开启查询辅助插件
- @EnableInterception：在dao接口上或者接口中方法上使用，允许接口级别或方法级别的拦截

更详细的使用介绍请查看该项目中的**query-helper-demo**模块



作者渣渣开发该插件的目的是为了对Mybatis拦截器的工作原理以及springboot自动装配进行学习...所以欢迎大家一起来学习交流，对这个项目感兴趣或者是有疑问的，可以加我的QQ（937020117）联系哦

如果该项目对你有帮助或者绝对还不错的，别忘了给作者渣渣一个star哦...哈哈哈哈



