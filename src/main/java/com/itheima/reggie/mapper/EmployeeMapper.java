package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description:
 * @Author: 海马在路上
 * @Date: 2022/6/22 9:10
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
