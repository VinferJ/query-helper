package me.learnjava.queryhelper.demo.dao;




import me.learnjava.queryhelper.annotation.EnableInterception;

import me.learnjava.queryhelper.demo.entity.UserEntity;
import org.apache.catalina.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Vinfer
 * @date 2021-02-01    17:42
 **/
@Repository
@Mapper
public interface UserDao {

    /**
     * 查询所有用户
     * @return      {@link UserEntity}
     */
    List<UserEntity> queryAll();

    /**
     * 分页查询，通过自定义的Mybatis拦截器实现
     * 只有在方法或接口上使用@EnableInterception注解才会对当前方法进行查询拦截
     * 而且这里要写的sql应该是查询要进行分页的所有数据
     *
     * @return      查询所有用户数据
     */
    @EnableInterception
    List<UserEntity> pageQuery();


    /**
     * 保存一行用户记录
     * @param userEntity    {@link UserEntity}
     */
    void saveOne(UserEntity userEntity);


    /**
     * 查询用户表总行数
     * @return      用户表总行数
     */
    @Select("select count(*) from user")
    Long countUserTable();

    /**
     * 查询指定性别的全部用户
     * @param gender    用户性别
     * @return          {@link UserEntity}
     */
    @EnableInterception
    @Select("select id,username,realname,password,gender,age,email,phone,avatar,status,created_by,created_at from user where gender = #{gender}")
    List<UserEntity> queryByGender(int gender);

}
