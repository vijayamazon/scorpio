package com.onlymaker.scorpio.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ErrorHandler implements ErrorController {
    private final static Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    private final static String PATH = "/error";

    @Autowired
    ErrorAttributes errorAttributes;

    @RequestMapping(PATH)
    private Map response(HttpServletRequest request) {
        WebRequest webRequest = new ServletWebRequest(request);
        return ApiResponse
                .failure()
                .setData(errorAttributes.getErrorAttributes(webRequest, logger.isDebugEnabled()))
                .build();
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
