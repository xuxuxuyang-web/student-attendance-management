package com.example.attendance.dao;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNo(String studentNo);

    List<Student> findByNameContaining(String name);

    List<Student> findByClassName(String className);

    long countByGender(String gender);

    @Query("SELECT COUNT(DISTINCT s.className) FROM Student s")
    long countDistinctClass();

    boolean existsByStudentNo(String studentNo);

    boolean existsByStudentNoAndIdNot(String studentNo, Long id);

    Page<Student> findByStudentNoContainingOrNameContaining(String studentNo, String name, Pageable pageable);

    Page<Student> findByStudentNoContainingOrNameContainingAndGender(String studentNo, String name, String gender, Pageable pageable);

    Page<Student> findByGender(String gender, Pageable pageable);
    // 新增：通过用户名查询学生
    @Query("SELECT s FROM Student s JOIN s.user u WHERE u.username = :username")
    Student findByUsername(@Param("username") String username);

    // 通过用户ID查询学生
    Student findByUserId(Long userId);
}