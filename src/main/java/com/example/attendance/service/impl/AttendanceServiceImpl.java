package com.example.attendance.service.impl;

import com.example.attendance.dao.AttendanceRepository;
import com.example.attendance.dao.CourseRepository;
import com.example.attendance.dao.StudentRepository;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.dto.StatisticsDTO;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ========== 基础 CRUD ==========

    @Override
    public Attendance save(Attendance attendance) {
        if (attendance.getCreateTime() == null) {
            attendance.setCreateTime(LocalDateTime.now());
        }
        return attendanceRepository.save(attendance);
    }

    @Override
    public Attendance findById(Long id) {
        return attendanceRepository.findById(id).orElse(null);
    }

    @Override
    public Page<Attendance> findAll(Specification<Attendance> spec, Pageable pageable) {
        return attendanceRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Attendance> findByStudentId(String studentId, Pageable pageable) {
        return attendanceRepository.findByStudentId(studentId, pageable);
    }

    @Override
    public List<Attendance> findByStudentIdAndDateRange(String studentId, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByStudentIdAndCheckInTimeBetween(studentId, start, end);
    }

    @Override
    public Attendance findTodayAttendance(String studentId, Long courseId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        return attendanceRepository.findTodayAttendance(studentId, courseId, startOfDay, endOfDay);
    }

    // ========== 统计查询 ==========

    @Override
    public long countByStudentIdAndStatus(String studentId, String status) {
        return attendanceRepository.countByStudentIdAndStatus(studentId, status);
    }

    @Override
    public long countByStudentId(String studentId) {
        return attendanceRepository.countByStudentId(studentId);
    }

    @Override
    public long countByStudentIdAndDateRange(String studentId, LocalDateTime startDate, LocalDateTime endDate) {
        return attendanceRepository.countByStudentIdAndDateRange(studentId, startDate, endDate);
    }

    @Override
    public List<Object[]> countGroupByStatus(String studentId) {
        return attendanceRepository.countGroupByStatus(studentId);
    }

    // ========== 周/月统计 ==========

    @Override
    public long countThisWeek(String studentId) {
        return attendanceRepository.countThisWeek(studentId);
    }

    @Override
    public long countThisMonth(String studentId) {
        return attendanceRepository.countThisMonth(studentId);
    }

    @Override
    public long countNormalThisMonth(String studentId) {
        return attendanceRepository.countNormalThisMonth(studentId);
    }

    @Override
    public long countLateThisMonth(String studentId) {
        return attendanceRepository.countLateThisMonth(studentId);
    }

    @Override
    public long countAbsentThisMonth(String studentId) {
        return attendanceRepository.countAbsentThisMonth(studentId);
    }

    // ========== 今日统计 ==========

    @Override
    public long countTodayCheckIn() {
        return attendanceRepository.countTodayCheckIn();
    }

    @Override
    public long countTodayLate() {
        return attendanceRepository.countTodayLate();
    }

    @Override
    public long countTodayNormal() {
        return attendanceRepository.countTodayNormal();
    }

    @Override
    public long countTodayByStudentId(String studentId) {
        return attendanceRepository.countTodayByStudentId(studentId);
    }

    // ========== 班级统计 ==========

    @Override
    public List<Object[]> getClassStatistics() {
        return attendanceRepository.getClassStatistics();
    }

    @Override
    public List<Object[]> getCourseStatistics() {
        return attendanceRepository.getCourseStatistics();
    }

    // ========== 高级统计 ==========

    @Override
    public List<Object[]> getMonthlyStatistics(String studentId) {
        return attendanceRepository.getMonthlyStatistics(studentId);
    }

    @Override
    public List<Object[]> getDailyStatistics(String studentId, int days) {
        return attendanceRepository.getDailyStatistics(studentId, days);
    }

    @Override
    public List<Attendance> findRecentRecords(String studentId, int limit) {
        return attendanceRepository.findRecentRecords(studentId, limit);
    }

    @Override
    public Double getAttendanceRate(String studentId) {
        Double rate = attendanceRepository.getAttendanceRate(studentId);
        return rate != null ? rate : 0.0;
    }

    @Override
    public Double getMonthlyAttendanceRate(String studentId) {
        Double rate = attendanceRepository.getMonthlyAttendanceRate(studentId);
        return rate != null ? rate : 0.0;
    }

    @Override
    public StatisticsDTO getStudentStatistics(String studentId) {
        long totalCount = attendanceRepository.countByStudentId(studentId);
        long normalCount = attendanceRepository.countByStudentIdAndStatus(studentId, "NORMAL");
        long lateCount = attendanceRepository.countByStudentIdAndStatus(studentId, "LATE");
        long absentCount = attendanceRepository.countByStudentIdAndStatus(studentId, "ABSENT");

        double attendanceRate = totalCount > 0 ? (double) normalCount / totalCount * 100 : 0;

        long weekCount = attendanceRepository.countThisWeek(studentId);
        long monthCount = attendanceRepository.countThisMonth(studentId);
        long monthNormalCount = attendanceRepository.countNormalThisMonth(studentId);
        long monthLateCount = attendanceRepository.countLateThisMonth(studentId);
        long monthAbsentCount = attendanceRepository.countAbsentThisMonth(studentId);

        double monthAttendanceRate = monthCount > 0 ? (double) monthNormalCount / monthCount * 100 : 0;

        return StatisticsDTO.builder()
                .totalCount(totalCount)
                .normalCount(normalCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .attendanceRate(Math.round(attendanceRate * 100) / 100.0)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .monthNormalCount(monthNormalCount)
                .monthLateCount(monthLateCount)
                .monthAbsentCount(monthAbsentCount)
                .monthAttendanceRate(Math.round(monthAttendanceRate * 100) / 100.0)
                .build();
    }

    // ========== 文件导入 ==========

    @Override
    public ImportResult importFromExcel(MultipartFile file) {
        ImportResult result = new ImportResult();

        // 1. 验证文件
        if (file.isEmpty()) {
            result.addError("文件为空");
            return result;
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            result.addError("文件格式不正确，请上传 .xlsx 或 .xls 文件");
            return result;
        }

        // 2. 解析 Excel
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            parseWorkbook(workbook, result);
        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            result.addError("解析Excel文件失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public ImportResult importFromExcelByPath(String filePath) {
        ImportResult result = new ImportResult();

        try (Workbook workbook = WorkbookFactory.create(new java.io.File(filePath))) {
            parseWorkbook(workbook, result);
        } catch (Exception e) {
            log.error("解析Excel文件失败", e);
            result.addError("解析Excel文件失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 解析 Workbook
     */
    private void parseWorkbook(Workbook workbook, ImportResult result) {
        Sheet sheet = workbook.getSheetAt(0);

        // 验证标题行
        Row headerRow = sheet.getRow(0);
        if (headerRow == null || headerRow.getLastCellNum() < 5) {
            result.addError("Excel格式错误，至少需要5列：学号、课程名称、打卡时间、状态、备注");
            return;
        }

        // 从第二行开始读取
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                // 读取单元格数据
                String studentNo = getCellValue(row.getCell(0));
                String courseName = getCellValue(row.getCell(1));
                String checkInTimeStr = getCellValue(row.getCell(2));
                String status = getCellValue(row.getCell(3));
                String remark = getCellValue(row.getCell(4));

                // 数据验证
                if (studentNo.isEmpty()) {
                    result.addError("第" + (i + 1) + "行：学号不能为空");
                    continue;
                }
                if (courseName.isEmpty()) {
                    result.addError("第" + (i + 1) + "行：课程名称不能为空");
                    continue;
                }
                if (checkInTimeStr.isEmpty()) {
                    result.addError("第" + (i + 1) + "行：打卡时间不能为空");
                    continue;
                }

                // 查找学生
                Student student = studentRepository.findByStudentNo(studentNo).orElse(null);
                if (student == null) {
                    result.addError("第" + (i + 1) + "行：学号 " + studentNo + " 不存在");
                    continue;
                }

                // 查找课程
                Course course = null;
                List<Course> courses = courseRepository.findAll();
                for (Course c : courses) {
                    if (c.getName().equals(courseName)) {
                        course = c;
                        break;
                    }
                }
                if (course == null) {
                    result.addError("第" + (i + 1) + "行：课程 " + courseName + " 不存在");
                    continue;
                }

                // 解析打卡时间
                LocalDateTime checkInTime = parseDateTime(checkInTimeStr);
                if (checkInTime == null) {
                    result.addError("第" + (i + 1) + "行：打卡时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
                    continue;
                }

                // 验证状态
                if (!isValidStatus(status)) {
                    result.addError("第" + (i + 1) + "行：状态无效，请使用 NORMAL/LATE/ABSENT");
                    continue;
                }

                // 创建考勤记录
                Attendance attendance = new Attendance();
                attendance.setStudentId(String.valueOf(student.getId()));
                attendance.setStudentName(student.getName());
                attendance.setCourseId(course.getId());
                attendance.setCourseName(course.getName());
                attendance.setCheckInTime(checkInTime);
                attendance.setStatus(status);
                attendance.setRemark(remark);
                attendance.setCreateTime(LocalDateTime.now());

                attendanceRepository.save(attendance);
                result.incrementSuccess();

            } catch (Exception e) {
                log.error("解析第{}行失败", i + 1, e);
                result.addError("第" + (i + 1) + "行：" + e.getMessage());
            }
        }
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;

        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                LocalDate date = LocalDate.parse(dateTimeStr, DATE_FORMATTER);
                return date.atStartOfDay();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    /**
     * 验证状态是否有效
     */
    private boolean isValidStatus(String status) {
        return "NORMAL".equals(status) || "LATE".equals(status) || "ABSENT".equals(status);
    }
}