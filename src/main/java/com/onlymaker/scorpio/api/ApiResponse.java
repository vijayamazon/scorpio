package com.onlymaker.scorpio.api;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

class ApiResponse {
    private int code;
    private Map data;

    static ApiResponse success() {
        return new ApiResponse(ErrorCode.NONE_ERROR);
    }

    static ApiResponse failure() {
        return new ApiResponse(ErrorCode.UNKNOWN);
    }

    static ApiResponse failure(ErrorCode errorCode) {
        Assert.isTrue(errorCode != ErrorCode.NONE_ERROR, "errorCode mustn't be zero");
        return new ApiResponse(errorCode);
    }

    @SuppressWarnings("unchecked")
    Map build() {
        return new HashMap() {{
            put("code", code);
            put("data", data);
        }};
    }

    ApiResponse setData(Map data) {
        this.data = data;
        return this;
    }

    private ApiResponse(ErrorCode errorCode) {
        this.code = errorCode.value();
    }
}
