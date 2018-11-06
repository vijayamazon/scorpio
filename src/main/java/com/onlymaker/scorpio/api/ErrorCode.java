package com.onlymaker.scorpio.api;

public enum ErrorCode {
    UNKNOWN(-1),
    NONE_ERROR(0);

    ErrorCode(int i) {
        this.code = i;
    }

    int value() {
        return this.code;
    }

    private final int code;
}
