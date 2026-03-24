package com.example.attendance;

import lombok.Data;

@Data // 自动生成 getter/setter/toString 等方法
public class Student {
    // 学号（路径参数，推荐用 String）
    private String studentId;
    // 姓名
    private String name;
    // 年龄（包装类型 Integer，避免默认值 0 的歧义）
    private Integer age;
    // 班级
    private String className;
}
