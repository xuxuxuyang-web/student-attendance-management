package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 显示登录页面
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误！");
        }
        if (logout != null) {
            model.addAttribute("message", "您已成功退出登录！");
        }
        return "login";
    }

    /**
     * 显示注册页面
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * 处理注册请求（表单提交）
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam(defaultValue = "STUDENT") String role,
                           Model model) {
        // 验证用户名是否已存在
        if (userService.findByUsername(username) != null) {
            model.addAttribute("error", "用户名已存在！");
            return "register";
        }

        // 验证密码是否一致
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致！");
            return "register";
        }

        // 验证密码长度
        if (password.length() < 6) {
            model.addAttribute("error", "密码长度不能少于6位！");
            return "register";
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        userService.save(user);

        model.addAttribute("success", "注册成功，请登录！");
        return "login";
    }
    @GetMapping("/gen")
    @ResponseBody
    public String generatePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        return "原始密码: " + password +
                "<br>加密后: " + encoded +
                "<br>密码长度: " + encoded.length();
    }

}