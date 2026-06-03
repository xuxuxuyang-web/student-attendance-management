package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.dao.StudentRepository;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student save(Student student) {
        if (student.getId() == null) {
            student.setCreateTime(LocalDateTime.now());
        }
        student.setUpdateTime(LocalDateTime.now());
        return studentRepository.save(student);
    }

    @Override
    public Student update(Student student) {
        student.setUpdateTime(LocalDateTime.now());
        return studentRepository.save(student);
    }

    @Override
    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        studentRepository.deleteAllById(ids);
    }

    @Override
    public Student findById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Override
    public Page<Student> findAll(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    public Student findByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo).orElse(null);
    }

    @Override
    public List<Student> findByNameContaining(String name) {
        return studentRepository.findByNameContaining(name);
    }

    @Override
    public List<Student> findByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    @Override
    public long countByGender(String gender) {
        return studentRepository.countByGender(gender);
    }

    @Override
    public long countDistinctClass() {
        return studentRepository.countDistinctClass();
    }

    @Override
    public Page<Student> searchStudents(String keyword, String gender, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty()) {
            return studentRepository.findByStudentNoContainingOrNameContaining(keyword, keyword, pageable);
        } else if (gender != null && !gender.isEmpty()) {
            return studentRepository.findByGender(gender, pageable);
        } else {
            return studentRepository.findAll(pageable);
        }
    }

    @Override
    public boolean existsByStudentNo(String studentNo) {
        return studentRepository.existsByStudentNo(studentNo);
    }

    @Override
    public boolean existsByStudentNoAndIdNot(String studentNo, Long excludeId) {
        return studentRepository.existsByStudentNoAndIdNot(studentNo, excludeId);
    }
    @Override
    public Student findByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    // 新增方法实现
    @Override
    public Student findByUserId(Long userId) {
        return studentRepository.findByUserId(userId);
    }



}