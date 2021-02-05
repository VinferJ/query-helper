package me.learnjava.queryhelper.demo.service.impl;




import me.learnjava.queryhelper.annotation.QueryHelper;
import me.learnjava.queryhelper.constant.InterceptMode;
import me.learnjava.queryhelper.demo.dao.UserDao;
import me.learnjava.queryhelper.demo.dto.UserCreateDto;
import me.learnjava.queryhelper.demo.entity.UserEntity;
import me.learnjava.queryhelper.demo.plugins.MyInterceptionPostProcessor;
import me.learnjava.queryhelper.demo.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Vinfer
 * @date 2021-02-01    17:41
 **/
@Service
public class IUserServiceImpl implements IUserService {


    @Autowired
    private UserDao userDao;

    @Override
    public UserEntity createUser(UserCreateDto userCreateDto) {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(userCreateDto, userEntity);
        long timeMillis = System.currentTimeMillis();
        userEntity.setAvatar("");
        userEntity.setCreatedBy(1000L);
        userEntity.setCreatedAt(timeMillis);
        userDao.saveOne(userEntity);
        return userEntity;
    }

    @Override
    public List<UserEntity> queryAll() {
        return userDao.queryAll();
    }


    @QueryHelper(
            //配置mapperClass，对应的是查询所在的接口
            mapperClass = UserDao.class,
            //如果使用默认的MODIFY_SQL模式来进行分页查询
            //由于该拦截模式是通过修改sql来添加limit和offset关键字进行查询的
            //因此需要先去查询结果集的总行数，因此需要配置一个查询行数的查询方法
            //那么这里的counterUserTable对应查询的结果集的行数就是
            //方法中userDao.pageQuery方法未分页时所查询的结果集的行数
            rowCounterMethodName = "countUserTable")
    @Override
    public List<UserEntity> queryByPageUseModifySqlMode(int pageNum, int pageSize) {
        return userDao.pageQuery();
    }

    @QueryHelper(
            mapperClass = UserDao.class,
            //使用MODIFY_RESULT进行分页查询时不需要再配置rowCounterMethodName
            //因为该拦截模式实现分页查询的原理是通过拦截查询结果进行数据分片
            //也就是所查询时会查询所有的数据，最终通过传入的参数进行数据分片
            //严格来说这种实现并不是真正的分页查询，只是做了数据分片罢了
            //在整体数据量非常大的时候，建议使用MODIFY_SQL模式，
            //因为查询表的行数要比查询所有数据整体要更快些（查询全部数据时数据传输的网络io耗时大）
            //并且Mybatis对结果集拦截的插件会对所有类型查询都进行拦截
            interceptMode = InterceptMode.MODIFY_RESULT,
            //这两个属性也可以不使用默认值，但必须和下面方法的参数名称进行对应，
            //因为query-helper是基于aop来处理参数传递的，因此会通过反射来获取参数数据
            pageNumParamName = "currentPage",
            pageSizeParamName = "segmentSize"
    )
    @Override
    public Object queryByPageUseModifyResultMode(int currentPage, int segmentSize) {
        return userDao.pageQuery();
    }

    @QueryHelper(
            mapperClass = UserDao.class,
            //设置为false，不用于实现分页查询
            isForPaginationQuery = false,
            //query-helper支持将后置处理更换为自定义的拦截后置处理器，用于实现自定义的拦截逻辑
            //使用自定义的拦截器时，如果不是用于实现分页查询，那么建议将isForPaginationQuery关闭，
            //如果还是用于实现分页查询功能，也可以开启
            //并且要注意，如果该处理器要拦截修改sql，请使用MODIFY_SQL为拦截模式
            //如果要拦截修改查询结果，请使用MODIFY_RESULT为拦截模式
            processor = MyInterceptionPostProcessor.class
    )
    @Override
    public List<UserEntity> queryUserByGender(int gender) {
        return userDao.queryByGender(gender);
    }

    @QueryHelper(
            mapperClass = UserDao.class,
            isForPaginationQuery = false,
            processor = MyInterceptionPostProcessor.class,
            //使用拦截结果的模式，让处理器中的postProcessAfterProceeding生效
            interceptMode = InterceptMode.MODIFY_RESULT
    )
    public List<UserEntity> queryUserByGenderInterceptWithMyProcessor(int gender){
        return userDao.queryByGender(gender);
    }

    @Override
    public Long count() {
        return userDao.countUserTable();
    }

}
