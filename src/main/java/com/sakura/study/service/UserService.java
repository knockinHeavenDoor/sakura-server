package com.sakura.study.service;

import com.sakura.study.dto.PageRequest;
import com.sakura.study.utils.ResponseResult;

public interface UserService {
    /**
     * 获取分页的用户列表
     * @param page
     * @return
     */
    ResponseResult getPageUsers(PageRequest page);
}
