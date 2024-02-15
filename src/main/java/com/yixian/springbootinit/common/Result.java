package com.yixian.springbootinit.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 全局统一返回
 *
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;  //状态码：1 成功 0 失败
    private String message; // 错误信息
    private T data; // 数据


    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.message = msg;
        return result;
    }


}
