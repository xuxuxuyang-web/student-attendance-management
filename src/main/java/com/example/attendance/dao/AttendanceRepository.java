package com.example.attendance.dao;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    // ========== 基础查询 ==========

    // 根据学生ID分页查询
    Page<Attendance> findByStudentId(String studentId, Pageable pageable);

    // 根据学生ID和时间范围查询
    List<Attendance> findByStudentIdAndCheckInTimeBetween(String studentId, LocalDateTime start, LocalDateTime end);

    // 查询今日是否已打卡
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId AND a.courseId = :courseId AND a.checkInTime >= :startOfDay AND a.checkInTime < :endOfDay")
    Attendance findTodayAttendance(@Param("studentId") String studentId,
                                   @Param("courseId") Long courseId,
                                   @Param("startOfDay") LocalDateTime startOfDay,
                                   @Param("endOfDay") LocalDateTime endOfDay);

    // ========== 统计查询（JPA方法名） ==========

    // 统计某学生的总考勤次数
    long countByStudentId(String studentId);

    // 统计某学生的某状态数量
    long countByStudentIdAndStatus(String studentId, String status);

    // 检查某学生某课程是否已有记录
    boolean existsByStudentIdAndCourseId(String studentId, Long courseId);

    // ========== 统计查询（@Query注解 - JPQL） ==========

    // 统计某学生在指定日期范围内的考勤次数
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId AND a.checkInTime BETWEEN :startDate AND :endDate")
    long countByStudentIdAndDateRange(@Param("studentId") String studentId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // 按状态分组统计某学生的考勤数量
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.studentId = :studentId GROUP BY a.status")
    List<Object[]> countGroupByStatus(@Param("studentId") String studentId);

    // ========== 周/月统计（PostgreSQL 原生SQL） ==========

    // 统计本周打卡次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE student_id = :studentId AND " +
            "EXTRACT(YEAR FROM check_in_time) = EXTRACT(YEAR FROM CURRENT_DATE) AND " +
            "EXTRACT(WEEK FROM check_in_time) = EXTRACT(WEEK FROM CURRENT_DATE)",
            nativeQuery = true)
    long countThisWeek(@Param("studentId") String studentId);

    // 统计本月打卡次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE student_id = :studentId AND " +
            "EXTRACT(YEAR FROM check_in_time) = EXTRACT(YEAR FROM CURRENT_DATE) AND " +
            "EXTRACT(MONTH FROM check_in_time) = EXTRACT(MONTH FROM CURRENT_DATE)",
            nativeQuery = true)
    long countThisMonth(@Param("studentId") String studentId);

    // 统计本月正常打卡次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE student_id = :studentId AND status = 'NORMAL' AND " +
            "EXTRACT(YEAR FROM check_in_time) = EXTRACT(YEAR FROM CURRENT_DATE) AND " +
            "EXTRACT(MONTH FROM check_in_time) = EXTRACT(MONTH FROM CURRENT_DATE)",
            nativeQuery = true)
    long countNormalThisMonth(@Param("studentId") String studentId);

    // 统计本月迟到次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE student_id = :studentId AND status = 'LATE' AND " +
            "EXTRACT(YEAR FROM check_in_time) = EXTRACT(YEAR FROM CURRENT_DATE) AND " +
            "EXTRACT(MONTH FROM check_in_time) = EXTRACT(MONTH FROM CURRENT_DATE)",
            nativeQuery = true)
    long countLateThisMonth(@Param("studentId") String studentId);

    // 统计本月缺勤次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE student_id = :studentId AND status = 'ABSENT' AND " +
            "EXTRACT(YEAR FROM check_in_time) = EXTRACT(YEAR FROM CURRENT_DATE) AND " +
            "EXTRACT(MONTH FROM check_in_time) = EXTRACT(MONTH FROM CURRENT_DATE)",
            nativeQuery = true)
    long countAbsentThisMonth(@Param("studentId") String studentId);

    // ========== 今日统计（PostgreSQL 原生SQL） ==========

    // 统计今日总打卡次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE DATE(check_in_time) = CURRENT_DATE", nativeQuery = true)
    long countTodayCheckIn();

    // 统计今日迟到次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE DATE(check_in_time) = CURRENT_DATE AND status = 'LATE'", nativeQuery = true)
    long countTodayLate();

    // 统计今日正常打卡次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE DATE(check_in_time) = CURRENT_DATE AND status = 'NORMAL'", nativeQuery = true)
    long countTodayNormal();

    // 统计某学生今日打卡次数
    @Query(value = "SELECT COUNT(*) FROM attendance WHERE student_id = :studentId AND DATE(check_in_time) = CURRENT_DATE", nativeQuery = true)
    long countTodayByStudentId(@Param("studentId") String studentId);

    // ========== 班级统计（PostgreSQL 原生SQL） ==========

    // 统计所有学生的考勤情况（班级整体统计）
    @Query(value = "SELECT student_name, COUNT(*) as total, " +
            "SUM(CASE WHEN status = 'NORMAL' THEN 1 ELSE 0 END) as normal_count, " +
            "SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late_count, " +
            "SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count " +
            "FROM attendance GROUP BY student_name ORDER BY total DESC",
            nativeQuery = true)
    List<Object[]> getClassStatistics();

    // 按课程统计考勤情况
    @Query(value = "SELECT course_name, COUNT(*) as total, " +
            "SUM(CASE WHEN status = 'NORMAL' THEN 1 ELSE 0 END) as normal_count, " +
            "SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late_count " +
            "FROM attendance GROUP BY course_name ORDER BY total DESC",
            nativeQuery = true)
    List<Object[]> getCourseStatistics();

    // ========== 高级统计查询（PostgreSQL 原生SQL） ==========

    // 查询某学生每月的考勤统计
    @Query(value = "SELECT TO_CHAR(check_in_time, 'YYYY-MM') as month, " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 'NORMAL' THEN 1 ELSE 0 END) as normal_count, " +
            "SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late_count, " +
            "SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent_count " +
            "FROM attendance WHERE student_id = :studentId " +
            "GROUP BY TO_CHAR(check_in_time, 'YYYY-MM') " +
            "ORDER BY month DESC",
            nativeQuery = true)
    List<Object[]> getMonthlyStatistics(@Param("studentId") String studentId);

    // 查询某学生每天的考勤统计（最近7天）
    @Query(value = "SELECT DATE(check_in_time) as date, " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN status = 'NORMAL' THEN 1 ELSE 0 END) as normal_count, " +
            "SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) as late_count " +
            "FROM attendance WHERE student_id = :studentId " +
            "AND check_in_time >= CURRENT_DATE - INTERVAL ':days days' " +
            "GROUP BY DATE(check_in_time) " +
            "ORDER BY date DESC",
            nativeQuery = true)
    List<Object[]> getDailyStatistics(@Param("studentId") String studentId,
                                      @Param("days") int days);

    // 查询某学生最近 N 条打卡记录
    @Query(value = "SELECT * FROM attendance WHERE student_id = :studentId ORDER BY check_in_time DESC LIMIT :limit",
            nativeQuery = true)
    List<Attendance> findRecentRecords(@Param("studentId") String studentId,
                                       @Param("limit") int limit);

    // ========== 出勤率统计 ==========

    // 统计某学生的出勤率（正常打卡数 / 总打卡数）
    @Query(value = "SELECT " +
            "COALESCE(ROUND(100.0 * SUM(CASE WHEN status = 'NORMAL' THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 2), 0) as attendance_rate " +
            "FROM attendance WHERE student_id = :studentId",
            nativeQuery = true)
    Double getAttendanceRate(@Param("studentId") String studentId);

    // 统计本月出勤率
    @Query(value = "SELECT " +
            "COALESCE(ROUND(100.0 * SUM(CASE WHEN status = 'NORMAL' THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 2), 0) as attendance_rate " +
            "FROM attendance WHERE student_id = :studentId " +
            "AND EXTRACT(YEAR FROM check_in_time) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "AND EXTRACT(MONTH FROM check_in_time) = EXTRACT(MONTH FROM CURRENT_DATE)",
            nativeQuery = true)
    Double getMonthlyAttendanceRate(@Param("studentId") String studentId);
}