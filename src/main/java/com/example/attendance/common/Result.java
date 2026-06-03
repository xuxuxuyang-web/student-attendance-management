package com.example.attendance.common;

// 1. 按照课程要求：定义统一响应格式
public class Result<T> {
    // 状态码：200成功，500失败
    private Integer code;
    // 提示信息
    private String msg;
    // 数据体
    private T data;

    // 2. 必须提供：无参构造器（符合 Java Bean 规范）
    public Result() {
    }

    // 3. 提供：成功/失败 静态快速构建方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        result.data = null;
        return result;
    }

    // 4. 必须提供：getter 和 setter 方法（符合 Java Bean 规范）
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
