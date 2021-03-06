package com.sakura.study.controller.adminController;

import com.google.common.cache.LoadingCache;
import com.sakura.study.dto.ChangePassword;
import com.sakura.study.dto.EmployeeDto;
import com.sakura.study.dto.EmployeePageRequest;
import com.sakura.study.model.Employee;
import com.sakura.study.model.Function;
import com.sakura.study.service.EmployeeService;
import com.sakura.study.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/employee")
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    @Autowired
    @Qualifier("employeeCache")
    LoadingCache<String, Optional<Employee>> employeeCache;

    /**
     * 管理员登录
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public ResponseResult login(@RequestBody Employee employee){
        String message = "用户名或密码不能为空";
        Assert.notEmpty(employee.getUsername(),message);
        Assert.notEmpty(employee.getPassword(),message);
        Employee record = employeeService.login(employee);
        String token = UUID.randomUUID().toString();
        employeeCache.put(token,Optional.ofNullable(record));
        Map<String,String> map = new HashMap<>();
        map.put("token",token);
        return ResponseResult.success(map);
    }

    /**
     * 获取用户信息
     * @param token
     * @return
     */
    @RequestMapping(value = "/info",method = RequestMethod.GET)
    public ResponseResult getEmployeeInfo(@RequestHeader("Token")String token){
        Employee employee = employeeCache.getUnchecked(token).orElse(null);
        Assert.notNull(employee,"用户信息不存在");
        List<Function> functions;
        if(employee.getDepartmentId() == 0)
            functions = employeeService.getAllFunction();
        else
            functions = employeeService.getFunctionByUser(employee.getId());
        EmployeeDto dto = new EmployeeDto(employee);
        dto.setFunctions(functions);
        return ResponseResult.success(dto);
    }

    /**
     * 新增工作人员
     * @param token
     * @param employee
     * @return
     */
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    public ResponseResult add(@RequestHeader("Token")String token,@RequestBody Employee employee){
        String defaultPassword = "123456";
        if(employee.getPassword() == null) employee.setPassword(MD5Util.md5Encode(defaultPassword));
        if(employee.getPassword().trim().isEmpty()) throw new BusinessException(400,"密码格式不合法");
        employeeService.add(employee,token);
        return ResponseResult.success("添加成功",null);
    }

    /**
     * 编辑工作人员
     * @param token
     * @param employee
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.PUT)
    public ResponseResult edit(@RequestHeader("Token") String token, @RequestBody Employee employee,
                              @PathVariable("id") Integer id){
        employeeService.edit(employee,id,token);
        return ResponseResult.success("修改成功",null);
    }

    /**
     * 删除工作人员
     * @param token
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}",method = RequestMethod.DELETE)
    public ResponseResult delete(@RequestHeader("Token") String token, @PathVariable("id") Integer id){
        employeeService.deleted(id,token);
        return ResponseResult.success("删除成功",null);
    }

    /**
     * 工作人员修改自己的密码
     * @param token
     * @param data
     * @return
     */
    @RequestMapping(value = "/editPwd",method = RequestMethod.PUT)
    public ResponseResult editPwd(@RequestHeader("Token") String token, @RequestBody ChangePassword data){
        employeeService.editPwd(data,token);
        return ResponseResult.success("密码已修改",null);
    }

    /**
     * 工作人员列表
     * @param token
     * @return
     */
    @RequestMapping(value = "/employees",method = RequestMethod.GET)
    public ResponseResult getPageEmployees(@RequestHeader("Token") String token, EmployeePageRequest page){
        Employee employee = employeeCache.getUnchecked(token).orElse(null);
        if(StringUtils.isEmpty(page.getRealName())){
            page.setRealName(null);
        }
        page.initSkip();
        return employeeService.getPageEmployee(employee.getId(),page);
    }

    /**
     * 管理员退出登录
     * @return
     */
    @RequestMapping(value = "/session",method = RequestMethod.DELETE)
    public ResponseResult login(@RequestHeader("Token") String token){
    	employeeCache.invalidate(token);//invaliadate()注销方法
        return ResponseResult.success("退出登录成功",null);
    }

}
