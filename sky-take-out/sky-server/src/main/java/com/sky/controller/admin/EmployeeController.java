package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "Employee related interface")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "Employee login method")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("Employee logout method")
    public Result<String> logout() {
        return Result.success();
    }


    @PostMapping
    @ApiOperation("Add a new employee")
    public Result<String> addEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("Current Thread Id:{}", Thread.currentThread().getId());
        log.info("Add employee:{}", employeeDTO);
        employeeService.addEmployee(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("Employee paging query")
    public Result<PageResult<Employee>> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("Employee paging query, parameters are:{}", employeePageQueryDTO);
        PageResult<Employee> pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("Enable or Disable Employee Account")
    public Result toggleStatus(@PathVariable("status") Integer status,@RequestParam("id") Long id){
        log.info("Enable or Disable Employee Account: status:{}, id:{}", status, id);
        employeeService.toggleStatus(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("Query employee information by id")
    public Result<Employee> getEmployeeInfoById(@PathVariable Long id){
        log.info("Get Employee Info By id:{}", id);
        Employee employee = employeeService.selectById(id);
        if(employee != null)
            return Result.success(employee);
        else return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
    }

    @PutMapping
    @ApiOperation("Update Employee information")
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("Edit employee info:{}", employeeDTO);
        int update = employeeService.updateEmployee(employeeDTO);
        if(update > 0){
            return Result.success();
        }else
            return Result.error(MessageConstant.UNKNOWN_ERROR);
    }

}
