package com.xiaomi.mone.app.enums;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/10/29 13:22
 */
public enum CommonError {
    Success(0, "success"),

    UnknownError(-1, "unknown error"),

    ParamsError(400, "parameter error"),

    NOT_EXISTS_DATA(204, "数据不存在"),

    UNAUTHORIZED(403, "没有操作权限"),

    SERVER_ERROR(500, "服务器异常"),
    ;

    private int code;
    private String message;

    CommonError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
