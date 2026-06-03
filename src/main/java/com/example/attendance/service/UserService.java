package com.example.attendance.service;

import com.example.attendance.entity.User;

public interface UserService {

    User findByUsername(String username);

    void save(User user);

    User authenticate(String username, String password);
    void updateUserPasswords();
}