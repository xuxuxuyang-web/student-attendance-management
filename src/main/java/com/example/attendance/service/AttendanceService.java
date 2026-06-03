package com.example.attendance.service;

import com.example.attendance.dto.ImportResult;
import com.example.attendance.dto.StatisticsDTO;
import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {

    // ========== 基础 CRUD ==========

    Attendance save(Attendance attendance);

    Attendance findById(Long id);

    Page<Attendance> findAll(Specification<Attendance> spec, Pageable pageable);

    Page<Attendance> findByStudentId(String studentId, Pageable pageable);

    List<Attendance> findByStudentIdAndDateRange(String studentId, LocalDateTime start, LocalDateTime end);

    Attendance findTodayAttendance(String studentId, Long courseId);

    // ========== 统计查询 ==========

    long countByStudentIdAndStatus(String studentId, String status);

    // 统计某学生的总考勤次数
    long countByStudentId(String studentId);

    // 统计学生在指定日期范围内的考勤次数
    long countByStudentIdAndDateRange(String studentId, LocalDateTime startDate, LocalDateTime endDate);

    // 按状态分组统计
    List<Object[]> countGroupByStatus(String studentId);

    // ========== 周/月统计 ==========

    // 本周打卡次数
    long countThisWeek(String studentId);

    // 本月打卡次数
    long countThisMonth(String studentId);

    // 本月正常打卡次数
    long countNormalThisMonth(String studentId);

    // 本月迟到次数
    long countLateThisMonth(String studentId);

    // 本月缺勤次数
    long countAbsentThisMonth(String studentId);

    // ========== 今日统计 ==========

    // 今日总打卡次数
    long countTodayCheckIn();

    // 今日迟到次数
    long countTodayLate();

    // 今日正常打卡次数
    long countTodayNormal();

    // 某学生今日打卡次数
    long countTodayByStudentId(String studentId);

    // ========== 班级统计 ==========

    // 班级整体统计
    List<Object[]> getClassStatistics();

    // 按课程统计
    List<Object[]> getCourseStatistics();

    // ========== 高级统计 ==========

    // 每月统计
    List<Object[]> getMonthlyStatistics(String studentId);

    // 每日统计（最近N天）
    List<Object[]> getDailyStatistics(String studentId, int days);

    // 最近N条记录
    List<Attendance> findRecentRecords(String studentId, int limit);

    // 出勤率
    Double getAttendanceRate(String studentId);

    // 本月出勤率
    Double getMonthlyAttendanceRate(String studentId);

    // 获取学生完整统计信息
    StatisticsDTO getStudentStatistics(String studentId);

    // ========== 文件导入 ==========

    // 从 MultipartFile 导入 Excel
    ImportResult importFromExcel(MultipartFile file);

    // 从文件路径导入 Excel
    ImportResult importFromExcelByPath(String filePath);
}