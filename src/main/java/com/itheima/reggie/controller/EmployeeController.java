package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @Description:
 * @Author: 海马在路上
 * @Date: 2022/6/22 9:40
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    // 登录逻辑
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {

        //①. 将页面提交的密码password进行md5加密处理, 得到加密后的字符串
        String password = employee.getPassword();// 补全 ctrl+alt+v
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //②. 根据页面提交的用户名username查询数据库中员工数据信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();//包装查询对象
        queryWrapper.eq(Employee::getUsername, employee.getUsername()); // 添加条件
        Employee emp = employeeService.getOne(queryWrapper);// 数据库中name unique唯一，所以用 getone

        //③. 如果没有查询到, 则返回登录失败结果
        if (emp == null) {
            return R.error("登录失败"); // 静态方法
        }
        //④. 密码比对，如果不一致, 则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败"); // 静态方法
        }
        //⑤. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已禁用");
        }
        //⑥. 登录成功，将员工id存入Session, 并返回登录成功结果
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);
//        登录添加一个过滤器或拦截器，判断用户是否已经完成登录，如果没有登录则返回提示信息，跳转到登录页面。

    }

    // 退出逻辑
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 1、 清理session中的用户id【跳转登录页面在前端代码中】
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //新增员工
    //因为前端传来json格式，函数参数需要@RequestBody
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());

        // 新增员工设置初始密码,不要用明文密码，需MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

/* 【公共字段自动填充（MP提供的功能）】
1、在实体类（entity或domain层）的公共属性上加入@TableField注解，指定自动填充的策略。
2、按照MP框架要求编写元数据对象处理器，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口。
        //status 数据库会设置默认值
        //设置createTime,updateTime,createUser,updateUser
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

// 获得当前登录用户的id 需要request对象，所以函数参数中引入
        //⑥. 登录成功，将员工id存入Session, 并返回登录成功结果
//        request.getSession().setAttribute("employee",emp.getId());
//        return  R.success(emp);
        Long empId = (Long) request.getSession().getAttribute("employee");//不转返回object
        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);
*/
        // 传到service层，save方法来自service继承的IService接口
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    //员工信息分页查询方法 【函数参数为所接受的前端传来的请求信息】
    //1). 页面发送ajax请求，将分页查询参数(page、pageSize、name)提交到服务端
    // 先要编写MybatisPlusConfig 配置类
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);

        //1、构造分页构造器（告诉MP page  pageSize）
        Page pageInfo = new Page(page, pageSize);

        //2、构造条件构造器【动态封装过滤条件】MP中
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件【注意：StringUtils 为common.lang包中】
        //第一个参数作用：当name不为空时，填入参数name
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //3、执行查询【传入page对象 ，条件构造器 queryWrapper】
        //内部会自动能够进行封装，所以不需要返回值
        employeeService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    //根据id修改员工信息 【前端中res.code 所以 此处R<String> 填string】
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("employee:{}", employee);
/*【公共字段自动填充（MP提供的功能）】
1、在实体类（entity或domain层）的公共属性上加入@TableField注解，指定自动填充的策略。
2、按照MP框架要求编写元数据对象处理器，在此类中统一为公共字段赋值，此类需要实现MetaObjectHandler接口。
        //获得当前登录用户的id
        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(empId);
        employee.setUpdateTime(LocalDateTime.now());
*/
        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    //根据id查询员工信息
    //@PathVariable 表示id变量 位于请求路径url中
    @GetMapping("/{id}")
    public  R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工信息。。。。");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return  R.error("没有查询到对应员工信息");
    }
}

