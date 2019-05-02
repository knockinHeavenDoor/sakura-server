package com.sakura.study.adminController;

import com.sakura.study.dto.PageRequest;
import com.sakura.study.service.UserService;
import com.sakura.study.utils.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class ConsumerController {

    @Autowired
    UserService userService;

    /**
     * 获取用户分页列表
     * @param page
     * @return
     */
    @RequestMapping(value = "/users",method = RequestMethod.GET)
    public ResponseResult getPageUsers(PageRequest page){
        return userService.getPageUsers(page);
    }
}