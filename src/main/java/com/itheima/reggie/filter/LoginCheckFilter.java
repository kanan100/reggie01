package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description: 检查用户是否已经完成登录
 * urlPatterns = "/*" 表示所有请求都拦截
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符  专门用来路径比较 处理/backend/**  与 /backend/index.html
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override // 重写过滤方法  【规范格式快捷键 ctrl+alt+L】
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // ①. 获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截请求：{}",requestURI);
        //定义不需要处理的请求路径(不用判登录）
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",//静态资源
                "/front/**",//静态资源
                "/user/sendMsg",//移动端发送短信
                "/user/login",//移动端登录
        };

        //②. 判断本次请求, 是否需要登录, 才可以访问
        boolean check = check(urls, requestURI);

        //③. 如果不需要，则直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request, response); //放行
            return;
        }
        //④.-1 判断后端员工系统 登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("employee"));

            //【公共字段自动填充（MP提供的功能）,获取用户id】 ;注意basecontext掉自己写
            Long empId= (Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response); //放行
            return;
        }
        //④.-2 判断移动端用户 登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            //【公共字段自动填充（MP提供的功能）,获取用户id】 ;注意basecontext掉自己写
            Long userId= (Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response); //放行
            return;
        }
        //⑤. 如果未登录, 则返回未登录结【需要结合前端代码】
        //通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
       // log.info("拦截到请求：{}", request.getRequestURI());//{}为占位符 填入第二个参数
    }

    //路径匹配方法，检查本次请求是否需要放行
    // 注意：【方法内不能定义方法】
    public boolean check(String[] urls , String requestURI){
        for (String url : urls) { // 快捷键 urls.for
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
