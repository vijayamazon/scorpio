package com.onlymaker.scorpio.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestControllerAdvice
public class ExceptionAdvice {
    private final static Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @Autowired
    ErrorAttributes errorAttributes;

    @ExceptionHandler
    public Map response(HttpServletRequest request, Throwable t) {
        logger.info("request {} exception", request.getRequestURI(), t);
        return ApiResponse
                .failure()
                .setData(errorAttributes.getErrorAttributes(new ServletWebRequest(request), logger.isDebugEnabled()))
                .build();
    }
}
