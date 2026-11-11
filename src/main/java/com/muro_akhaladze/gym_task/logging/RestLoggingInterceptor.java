package com.muro_akhaladze.gym_task.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String transactionId = MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY);

        log.info("Request [{}] {} | transactionId: {}", method, uri, transactionId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        int status = response.getStatus();
        String uri = request.getRequestURI();
        String transactionId = MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY);

        if (ex != null) {
            log.error("Error during [{}] {} | Status: {} | transactionId: {} | Message: {}",
                    request.getMethod(), uri, status, transactionId, ex.getMessage());
        } else {
            log.info("Completed [{}] {} | Status: {} | transactionId: {}",
                    request.getMethod(), uri, status, transactionId);
        }
    }
}