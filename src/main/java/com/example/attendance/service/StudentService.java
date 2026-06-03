package com.example.attendance.service;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    Student save(Student student);

    Student update(Student student);

    void deleteById(Long id);

    void batchDelete(List<Long> ids);

    Student findById(Long id);

    List<Student> findAll();

    Page<Student> findAll(Pageable pageable);

    Student findByStudentNo(String studentNo);

    List<Student> findByNameContaining(String name);

    List<Student> findByClassName(String className);

    long countByGender(String gender);

    long countDistinctClass();

    Page<Student> searchStudents(String keyword, String gender, Pageable pageable);

    boolean existsByStudentNo(String studentNo);

    boolean existsByStudentNoAndIdNot(String studentNo, Long excludeId);
    // 新增：通过用户名查询学生
    Student findByUsername(String username);
    Student findByUserId(Long userId);
}