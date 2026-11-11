package com.muro_akhaladze.gym_task.config;

import com.muro_akhaladze.gym_task.logging.RestLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RestLoggingInterceptor restLoggingInterceptor;

    public WebConfig(RestLoggingInterceptor restLoggingInterceptor) {
        this.restLoggingInterceptor = restLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(restLoggingInterceptor);
    }
}
