package me.learnjava.queryhelper.demo.plugins;

import me.learnjava.queryhelper.core.processor.InterceptionPostProcessor;
import me.learnjava.queryhelper.demo.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vinfer
 * @date 2021-02-05    09:46
 **/
@Component
public class MyInterceptionPostProcessor implements InterceptionPostProcessor {


    /*
    * 目前版本中，一个拦截处理器的两个方法是在不同拦截模式下调用的
    * 因此，一种拦截模式只会有一个对应的方法被调用
    * */

    /**
     * 该后置处理只能在MODIFY_SQL模式下生效
     * @param interceptedSql    被拦截的sql
     * @param parametersMap     对sql进行后置处理所需参数的Map，对于自定义且不是用于分页查询的的拦截处理，
     *                          该Map传递的是{@link me.learnjava.queryhelper.annotation.QueryHelper}注解标注的方法中所有的参数
     *
     * @return                  处理后的sql
     * @throws Exception        异常
     */
    @Override
    public String postProcessBeforeProceeding(String interceptedSql, Map<String, Object> parametersMap) throws Exception {

        //实现自定义的不用于分页查询的拦截后置处理器可以通过结合业务逻辑用来实现数据权限控制的功能
        //可以通过给advice方法传入某些权限判断的参数，然后可以在该方法中通过parametersMap来获取
        //最终通过这些参数生成对应的sql，实现对数据访问的真实的权限拦截或过滤
        //使用自定义的拦截器时一定要将该拦截后置处理器注册到ioc中

        //拦截sql，追加where判断条件，对可查询数据进行限制
        //只允许看到年龄小于30的用户
        String modifiedSql = interceptedSql + " and age <= 30 ";

        //可以将字段通过map传递尽量，限制可查看的字段
        //以空格进行分割：select 字段部分 from 表名部分 连表操作部分(如果有) where 条件部分 ...其他..
        //具体分割的部分需要看自己写的sql来控制
        String[] split = modifiedSql.split(" ");
        String accessibleField = " id,username,gender,age,created_at ";
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < split.length; i++) {
            builder.append(split[i]).append(" ");
        }
        String execSql = split[0] + accessibleField + builder.toString();
        //添加limit，只给看范围内的数据
        execSql = execSql + " limit 100 ";
        System.out.println("parameters map: "+parametersMap);
        System.out.println("exec sql: "+execSql);
        return execSql;
    }


    /**
     * 该后置处理只能在MODIFY_RESULT模式下生效
     *
     * @param queryResult       查询得到的结果集，Mybatis对查询的结果都使用List来保存和传递
     *                          例如，dao接口的查询方法返回值是是long，假设查到的是1，那么这里拦截的结果应该是[1]
     *                          如果查询方法返回值的是一个User对象，拦截到的是[User{...}]
     *                          如果查询方法返回值是List<User>,那么这里拦截的就是List<User>,即[0=User{...},1=User{...},...]
     *
     * @return                  处理后的查询结果
     * @throws Exception        异常
     */
    @Override
    public Object postProcessAfterProceeding(Object queryResult) throws Exception {
        List<UserEntity> modifiedResult = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("modified_user_"+i);
            modifiedResult.add(userEntity);
        }
        return modifiedResult;
    }
}
