package me.learnjava.queryhelper.demo.service;







import me.learnjava.queryhelper.demo.dto.UserCreateDto;
import me.learnjava.queryhelper.demo.entity.UserEntity;

import java.util.List;

/**
 * @author Vinfer
 * @date 2021-02-01    17:24
 **/
public interface IUserService {

    /**
     * 创建用户
     * @param userCreateDto 用户创建数据传输对象
     * @return              {@link UserEntity}
     */
    UserEntity createUser(UserCreateDto userCreateDto);

    /**
     * 查询所有用户信息
     * @return          {@link UserEntity}
     */
    List<UserEntity> queryAll();

    /**
     * 使用MODIFY_SQL的模式进行分页查询
     * @param pageNum   当前页码
     * @param pageSize  分页大小
     * @return          分页查询结果
     */
    Object queryByPageUseModifySqlMode(int pageNum, int pageSize);

    /**
     * 使用MODIFY_RESULT的模式进行分页查询
     * @param currentPage   当前页码
     * @param segmentSize   分页大小
     * @return              分页查询结果
     */
    Object queryByPageUseModifyResultMode(int currentPage, int segmentSize);


    List<UserEntity> queryUserByGender(int gender);

    /**
     * 获取用户数据总数
     * @return      用户表总行数
     */
    Long count();


}
