package com.example.attendance.controller;

import com.example.attendance.Result;
import com.example.attendance.Student;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class StudentController {

    @GetMapping("/student/{id}")
    public String getStudentById(@PathVariable String id) {
        return "查询学号为 " + id + " 的学生信息";
    }

    @GetMapping("/student/search")
    public String searchStudent(
            @RequestParam String name,
            @RequestParam(defaultValue = "1") Integer page
    ) {
        return "查询姓名：" + name + "，页码：" + page;
    }
    @PostMapping("/student/create")
    public String createStudent(@RequestBody Student student) {
        return "创建学生：" + student.getName() + "，学号：" + student.getStudentId();
    }
}
