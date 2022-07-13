package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @Description:全局异常处理器
 * @Author: 海马在路上
 * @Date: 2022/6/23 13:27
 * @ControllerAdvice配置需要拦截的controller
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody // 处理返回的json数据
@Slf4j
public class GlobalExceptionHandler {
    // 异常处理方法
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public  R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.info(ex.getMessage());
        //首先判断异常信息是否包含 Duplicate entry 关键字
        if(ex.getMessage().contains("Duplicate entry")){
            // ex.getMessage() 异常信息 根据空格 分割为 字符串数组 ,取出zhangsan
            String[] split = ex.getMessage().split(" ");
            String msg= split[2]+ "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");// 返回给前端页面
    }

    // 异常处理方法【处理自定义的异常】
    @ExceptionHandler(CustomException.class)
    public  R<String> exceptionHandler(CustomException ex){
        log.info(ex.getMessage());

        return R.error(ex.getMessage());// 返回给前端页面
    }
}
