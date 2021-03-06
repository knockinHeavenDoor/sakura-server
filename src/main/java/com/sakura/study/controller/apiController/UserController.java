package com.sakura.study.controller.apiController;

import com.google.common.cache.LoadingCache;
import com.sakura.study.dto.ChangePassword;
import com.sakura.study.dto.UserAgreementDto;
import com.sakura.study.dto.UserDto;
import com.sakura.study.model.Assessment;
import com.sakura.study.model.University;
import com.sakura.study.model.User;
import com.sakura.study.model.UserAgreement;
import com.sakura.study.service.UserService;
import com.sakura.study.utils.Assert;
import com.sakura.study.utils.BusinessException;
import com.sakura.study.utils.MD5Util;
import com.sakura.study.utils.ResponseResult;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Resource(name = "userCache")
    private LoadingCache<String, Optional<User>> userCache;

    /**
     * 用户详细信息
     * @param token
     * @return
     */
    @RequestMapping(value = "/user/info",method = RequestMethod.GET)
    @ApiOperation(value="获取用户详细信息", notes="根据token来获取用户详细信息")
    @ApiImplicitParam(name = "token", value = "用户token", required = true, dataType = "String", paramType = "header")
    public ResponseResult getUserById(@RequestHeader("Token")String token) {
        User user = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(user,"登录已失效");
        UserDto data = userService.getUserInfo(user.getId());
        return ResponseResult.success(data);
    }

    /**
     * 用户登录
     * @param user
     * @return
     */
    @RequestMapping(value = "/user/session",method = RequestMethod.POST)
    public ResponseResult login(@RequestBody User user){
        String message = "用户名或密码不能为空";
        Assert.notEmpty(user.getUsername(),message);
        Assert.notEmpty(user.getPassword(),message);
        UserDto record = userService.login(user);
        String token = UUID.randomUUID().toString();
        userCache.put(token,Optional.of(record));
        Map<String,Object> map = new HashMap<>();
        map.put("token",token);
        map.put("user",record);
        return ResponseResult.success(map);
    }

    /**
     * 用户登出
     * @param token
     * @return
     */
    @RequestMapping(value = "/user/session",method = RequestMethod.DELETE)
    public ResponseResult login_out(@RequestHeader("Token")String token){
        if(StringUtils.isEmpty(token))
            throw new BusinessException(404,"您尚未登录");
        userCache.invalidate(token);
        return ResponseResult.success("退出登录成功",null);
    }

    /**
     * 用户注册
     * @param userDto
     */
    @RequestMapping(value = "/user",method = RequestMethod.POST)
    public ResponseResult register(@RequestBody UserDto userDto){
        UserDto data = userService.register(userDto);
        return ResponseResult.success(data);
    }

    /**
     * 用户修改信息
     * @param user
     * @param token
     * @return
     */
    @RequestMapping(value = "/user",method = RequestMethod.PATCH)
    public ResponseResult editInfo(@RequestBody User user, @RequestHeader("Token") String token){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        user.setId(cacheUser.getId());
        userService.editInfo(user);
        return ResponseResult.success("修改成功",null);
    }

    /**
     * api
     * 用户修改密码
     * @param data
     * @return
     */
    @RequestMapping(value = "/user/password",method = RequestMethod.PUT)
    public ResponseResult editPassword(@RequestBody ChangePassword data, @RequestHeader("Token") String token){
        if(StringUtils.isEmpty(data.getNewPassword()) || StringUtils.isEmpty(data.getOldPassword())){
            throw new BusinessException(400,"参数错误");
        }
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        User userInfo = userService.getUserById(cacheUser.getId());
        if(!userInfo.getPassword().equals(MD5Util.md5Encode(data.getOldPassword()))){
            throw new BusinessException(403,"原密码不对");
        }
        userInfo.setPassword(MD5Util.md5Encode(data.getNewPassword()));
        userService.editInfo(userInfo);
        return ResponseResult.success("修改成功",null);
    }

    /**
     * 用户的评估
     * @param assessment
     * @param token
     * @return
     */
    @RequestMapping(value = "/user/assessment",method = RequestMethod.POST)
    public ResponseResult assessment(@RequestBody Assessment assessment, @RequestHeader("Token") String token){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        userService.assessment(assessment,cacheUser.getId());
        return ResponseResult.success("评估成功",null);
    }

    /**
     * 用户上传签署协议
     * @param data
     * @param token
     * @return
     */
    @RequestMapping(value = "/user/agreement",method = RequestMethod.POST)
    public ResponseResult uploadAgreement(@RequestBody UserAgreement data,@RequestHeader("Token") String token){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        userService.uploadAgreement(data,cacheUser);
        return ResponseResult.success("上传成功",null);
    }

    /**
     * 获取家长列表，可搜索
     * @param username
     * @return
     */
    @RequestMapping(value = "/user/parents",method = RequestMethod.GET)
    public ResponseResult getParents(String username){
        if(username != null && username.trim().isEmpty())
            username = null;
        List<User> parents = userService.getParents(username);
        return ResponseResult.success(parents);
    }

    /**
     * 获取家长列表，可搜索
     * @param token
     * @return
     */
    @RequestMapping(value = "/user/children",method = RequestMethod.GET)
    public ResponseResult getChildren(@RequestHeader("Token") String token){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        List<User> childs = userService.getChildren(cacheUser.getId());
        return ResponseResult.success(childs);
    }

    /**
     * 用户上传签署协议
     * @param token
     * @return
     */
    @RequestMapping(value = "/user/agreement",method = RequestMethod.GET)
    public ResponseResult getAgreement(@RequestHeader("Token") String token){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        UserAgreementDto data = userService.getUserAgreement(cacheUser.getId());
        return ResponseResult.success(data);
    }

    /**
     * get用户的评估
     * @param token
     * @return
     */
    @RequestMapping(value = "/user/assessment",method = RequestMethod.GET)
    public ResponseResult assessment(@RequestHeader("Token") String token){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        Assessment assessment = userService.getAssessment(cacheUser.getId());
        return ResponseResult.success(assessment);
    }

    /**
     * 申请院校
     * @param token
     * @return
     */
    @RequestMapping(value = "/apply/school",method = RequestMethod.POST)
    public ResponseResult apply(@RequestHeader("Token") String token,@RequestBody User user){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        userService.apply(cacheUser.getId(),user.getApplySchool());
        return ResponseResult.success("申请成功",null);
    }

    /**
     * 申请院校
     * @param token
     * @return
     */
    @RequestMapping(value = "/apply/school",method = RequestMethod.GET)
    public ResponseResult apply(@RequestHeader("Token") String token){
        User cacheUser = userCache.getUnchecked(token).orElse(null);
        Assert.notNull(cacheUser,"登录已失效");
        University university = userService.getApply(cacheUser.getId());
        return ResponseResult.success(university);
    }
}
