package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.service.StudentService;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    // 获取当前登录用户
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        return userService.findByUsername(username);
    }

    // 判断是否是教师或管理员
    private boolean isTeacherOrAdmin() {
        User user = getCurrentUser();
        if (user == null) return false;
        String role = user.getRole();
        return "TEACHER".equals(role) || "ADMIN".equals(role);
    }

    // 获取当前登录学生（如果是学生）
    private Student getCurrentStudent() {
        User user = getCurrentUser();
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return null;
        }
        return studentService.findByUserId(user.getId());
    }

    // 学生列表页面（带统计数据）
    @GetMapping("/list")
    public String list(Model model) {
        List<Student> students;
        boolean isTeacher = isTeacherOrAdmin();

        if (isTeacher) {
            // 教师/管理员：查看所有学生
            students = studentService.findAll();
        } else {
            // 学生：只能查看自己的信息
            Student currentStudent = getCurrentStudent();
            if (currentStudent != null) {
                students = List.of(currentStudent);
            } else {
                students = List.of();
            }
        }

        // 统计数据
        long maleCount = studentService.countByGender("男");
        long femaleCount = studentService.countByGender("女");
        long classCount = studentService.countDistinctClass();

        model.addAttribute("students", students);
        model.addAttribute("totalElements", students.size());
        model.addAttribute("maleCount", maleCount);
        model.addAttribute("femaleCount", femaleCount);
        model.addAttribute("classCount", classCount);
        model.addAttribute("isTeacher", isTeacher);

        return "student/student-list";
    }

    // 新增学生页面（仅教师/管理员）
    @GetMapping("/add")
    public String addForm(Model model, RedirectAttributes redirectAttributes) {
        if (!isTeacherOrAdmin()) {
            redirectAttributes.addFlashAttribute("error", "无权限操作");
            return "redirect:/student/list";
        }
        model.addAttribute("student", new Student());
        model.addAttribute("pageTitle", "新增学生");
        return "student/student-form";
    }

    // 编辑学生页面（仅教师/管理员，或学生编辑自己）
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        // 检查权限
        if (!isTeacherOrAdmin()) {
            // 学生只能编辑自己的信息
            Student currentStudent = getCurrentStudent();
            if (currentStudent == null || !currentStudent.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "无权限编辑他人信息");
                return "redirect:/student/list";
            }
        }

        Student student = studentService.findById(id);
        if (student == null) {
            redirectAttributes.addFlashAttribute("error", "学生不存在");
            return "redirect:/student/list";
        }

        model.addAttribute("student", student);
        model.addAttribute("pageTitle", "修改学生");
        return "student/student-form";
    }

    // 保存学生（新增或更新）- 仅教师/管理员
    @PostMapping("/save")
    public String save(@ModelAttribute Student student, RedirectAttributes redirectAttributes) {
        if (!isTeacherOrAdmin()) {
            redirectAttributes.addFlashAttribute("error", "无权限操作");
            return "redirect:/student/list";
        }
        System.out.println("保存学生: " + student.getName());
        studentService.save(student);
        return "redirect:/student/list";
    }

    // 删除学生 - 仅教师/管理员
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!isTeacherOrAdmin()) {
            redirectAttributes.addFlashAttribute("error", "无权限删除");
            return "redirect:/student/list";
        }
        studentService.deleteById(id);
        return "redirect:/student/list";
    }
}