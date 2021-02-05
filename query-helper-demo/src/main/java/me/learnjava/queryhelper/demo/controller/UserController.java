package me.learnjava.queryhelper.demo.controller;


import lombok.extern.slf4j.Slf4j;

import me.learnjava.queryhelper.demo.dto.UserCreateDto;
import me.learnjava.queryhelper.demo.entity.UserEntity;
import me.learnjava.queryhelper.demo.service.IUserService;
import me.learnjava.queryhelper.demo.service.impl.IUserServiceImpl;
import me.learnjava.queryhelper.demo.util.AssertUtil;
import me.learnjava.queryhelper.demo.vo.PageQueryInput;
import me.learnjava.queryhelper.exception.PaginationQueryException;
import me.learnjava.queryhelper.support.PaginationQueryDataPack;
import me.learnjava.queryhelper.support.PaginationQueryDataTransporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;


/**
 * @author Vinfer
 * @date 2021-02-01    22:37
 **/
@RestController
@RequestMapping(value = "/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;


    @GetMapping(value = "/query/all")
    public Object queryAll(){
        List<UserEntity> userEntities = userService.queryAll();
        System.out.println("collection size："+userEntities.size());
        return userEntities;
    }

    @PostMapping(value = "/query/page/modifySql")
    public Object pageQuery1(@RequestBody PageQueryInput input){
        return doPageQuery(input,"MODIFY_SQL");
    }

    @PostMapping(value = "/query/page/modifyResult")
    public Object pageQuery2(@RequestBody PageQueryInput input){
        return doPageQuery(input, "MODIFY_RESULT");
    }

    private Object doPageQuery(PageQueryInput input,String mode){
        String errorMsg = "";
        int pageNum = input.getPageNum();
        int pageSize = input.getPageSize();
        //页码检查
        if (pageNum < 1){
            errorMsg = "Page number must be bigger then 1";
        }else if(pageSize < 0){
            errorMsg = "Page size must be bigger then 0";
        }
        String throwableErrorMsg = errorMsg;
        AssertUtil.isTrue(pageNum >= 1 && pageSize > 0, () -> {throw new PaginationQueryException(throwableErrorMsg);});

        Object queryResult;
        switch (mode){
            case "MODIFY_SQL":
                queryResult = userService.queryByPageUseModifySqlMode(pageNum,pageSize);
                break;
            case "MODIFY_RESULT":
                //本地实测，当查询总数为1w时，使用该模式平均耗时为300ms，而使用MODIFY_SQL在30ms内（本地测试的网络环境较好）
                //数据量越大，使用该模式查询耗费性能越大，如果服务器性能差，查询全部数据的响应是非常慢的
                queryResult = userService.queryByPageUseModifyResultMode(pageNum,pageSize);
                break;
            default:
                queryResult = userService.queryAll();
        }
        checkThreadLocal();
        return queryResult;
    }

    @PostMapping("/auto/gen/{total}")
    public String batchAutoInsert(@PathVariable String total){
        System.out.println("正在批量创建用户....");
        for (int i = 1; i <= Integer.parseInt(total); i++) {
            UserCreateDto createDto = new UserCreateDto();
            long timeMillis = System.currentTimeMillis();
            //批量自动创建需要在这里把id设置好
            createDto.setId((timeMillis / 10000) + i);
            createDto.setUsername("test_user_"+i);
            createDto.setRealname("test_realname_"+i);
            createDto.setPassword("test_password_"+i);
            Random random = new Random(timeMillis);
            int randomNum = random.nextInt(10000000);
            createDto.setEmail("test"+randomNum + "@gmail.com");
            int r1 = random.nextInt(9);
            int r2 = random.nextInt(6);
            int r3 = random.nextInt(3);
            createDto.setPhone("189"+r1+"903"+r2+"51"+r3);
            int age = random.nextInt(60);
            if (age <= 20){
                age += 20;
            }
            createDto.setAge(age);
            int gender = random.nextInt(2);
            createDto.setGender(gender);
            UserEntity user = userService.createUser(createDto);
            System.out.println("已成功创建第"+i+"个用户，id为：["+user.getId()+"]");
        }
        return "success";
    }

    @GetMapping(value = "/byGender/{gender}")
    public Object queryUserWithCustomInterceptor(@PathVariable String gender){
        AssertUtil.isTrue(gender.equals("1") || gender.equals("0"), () -> new RuntimeException("No such gender like:"+gender));
        List<UserEntity> userEntities = userService.queryUserByGender(Integer.parseInt(gender));
        //拦截处理器中追加了limit 100，因此这里的size应该是小于等于100,
        System.out.println("data size: "+userEntities.size());
        checkThreadLocal();
        return userEntities;
    }

    @GetMapping(value = "/byGender/modifyResult/{gender}")
    public Object queryWithCustomInterceptorInModifyResultMode(@PathVariable String gender){
        AssertUtil.isTrue(gender.equals("1") || gender.equals("0"), () -> new RuntimeException("No such gender like:"+gender));
        List<UserEntity> userEntities = ((IUserServiceImpl)userService).queryUserByGenderInterceptWithMyProcessor(Integer.parseInt(gender));
        //通过修改结果，只添加了10条记录，因此这里的size应该是10
        System.out.println("data size: "+userEntities.size());
        checkThreadLocal();
        return userEntities;
    }

    private void checkThreadLocal(){
        //这里只是对ThreadLocal做一个检查，可以直观地看到query-helper在对该变量的使用是安全的
        //保证每一次查询方法结束都会对其进行remove
        PaginationQueryDataPack queryDataPack = PaginationQueryDataTransporter.get();
        System.out.println("ThreadLocal check,value is: "+ queryDataPack);
        if (queryDataPack != null){
            log.warn("ThreadLocal value should be clear when finish service");
        }
    }

}
