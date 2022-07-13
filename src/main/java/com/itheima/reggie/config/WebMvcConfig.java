package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @Description:
 * @Author: 海马在路上
 * @Date: 2022/6/15 12:54
 */
@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射");
       registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/") ;
       registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/") ;
    }

    //扩展mvc框架的消息转化器
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象，作用：将controller返回结果R对象【 R。success（pageInfo）】转成json，再通过输出流形式响应给页面
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
       //设置对象转换器，底层使用Jackson 将 java对象转为 json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0,messageConverter);
    }
}
