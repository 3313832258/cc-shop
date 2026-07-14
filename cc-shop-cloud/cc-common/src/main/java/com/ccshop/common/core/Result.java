package com.ccshop.common.core;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结构
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public boolean ok() {
        return this.code != null && this.code == 200;
    }
}
