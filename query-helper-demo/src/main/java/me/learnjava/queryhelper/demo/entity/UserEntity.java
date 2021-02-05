package me.learnjava.queryhelper.demo.entity;


import lombok.Data;

/**
 * @author Vinfer
 * @date 2021-02-01    17:16
 **/
@Data
public class UserEntity {

    private Long id;

    private String username;

    private String realname;

    private String password;

    private Integer gender;

    private Integer age;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private Long createdBy;

    private Long createdAt;

}
