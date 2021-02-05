package me.learnjava.queryhelper.demo.dto;

import lombok.Data;

/**
 * @author Vinfer
 * @date 2021-02-05    14:59
 **/
@Data
public class UserCreateDto {

    private Long id;
    private String username;
    private String realname;
    private String password;
    private Integer gender;
    private Integer age;
    private String email;
    private String phone;

}
