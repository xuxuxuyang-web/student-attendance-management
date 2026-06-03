package com.example.attendance.controller;

import com.example.attendance.dao.CourseRepository;
import com.example.attendance.dto.ImportResult;
import com.example.attendance.dto.StatisticsDTO;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.StudentService;
import com.example.attendance.service.UserService;
import jakarta.annotation.PostConstruct;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private static final Logger log = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    // ========== 辅助方法 ==========

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        return userService.findByUsername(username);
    }

    /**
     * 判断当前用户是否是教师或管理员
     */
    private boolean isTeacherOrAdmin() {
        User user = getCurrentUser();
        if (user == null) return false;
        String role = user.getRole();
        return "TEACHER".equals(role) || "ADMIN".equals(role);
    }

    /**
     * 获取当前登录学生（仅学生角色使用）
     */
    private Student getCurrentStudent() {
        User user = getCurrentUser();
        if (user == null) return null;

        // 如果是教师/管理员，不返回学生
        if (isTeacherOrAdmin()) return null;

        return studentService.findByUserId(user.getId());
    }

    // ========== 考勤打卡（仅学生可用） ==========

    /**
     * 考勤打卡页面
     */
    @GetMapping("/checkIn")
    public String checkInPage(Model model) {
        Student student = getCurrentStudent();
        if (student == null) {
            // 如果是教师，跳转到列表页
            if (isTeacherOrAdmin()) {
                return "redirect:/attendance/list";
            }
            return "redirect:/login";
        }

        List<Course> courses = courseRepository.findAll();
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        model.addAttribute("courses", courses);
        model.addAttribute("currentTime", currentTime);
        model.addAttribute("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("studentName", student.getName());

        return "attendance/check-in";
    }

    /**
     * 处理打卡（仅学生可用）
     */
    @PostMapping("/checkIn")
    public String checkIn(@RequestParam Long courseId,
                          @RequestParam(required = false) String remark,
                          RedirectAttributes redirectAttributes) {
        try {
            Student student = getCurrentStudent();
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "学生才能进行打卡操作");
                return "redirect:/login";
            }

            String studentIdStr = String.valueOf(student.getId());

            // 检查今日是否已打卡
            Attendance todayAttendance = attendanceService.findTodayAttendance(studentIdStr, courseId);
            if (todayAttendance != null) {
                redirectAttributes.addFlashAttribute("error", "今日已打卡，无需重复打卡！");
                return "redirect:/attendance/checkIn";
            }

            // 获取课程信息
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                redirectAttributes.addFlashAttribute("error", "课程不存在！");
                return "redirect:/attendance/checkIn";
            }

            // 创建考勤记录
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentIdStr);
            attendance.setStudentName(student.getName());
            attendance.setCourseId(courseId);
            attendance.setCourseName(course.getName());
            attendance.setCheckInTime(LocalDateTime.now());
            attendance.setRemark(remark);
            attendance.setCreateTime(LocalDateTime.now());

            // 判断是否迟到
            LocalTime now = LocalTime.now();
            LocalTime startTime = course.getStartTime();

            if (now.isBefore(startTime)) {
                attendance.setStatus("NORMAL");
                redirectAttributes.addFlashAttribute("success", "打卡成功！");
            } else if (now.isBefore(startTime.plusMinutes(30))) {
                attendance.setStatus("NORMAL");
                redirectAttributes.addFlashAttribute("success", "打卡成功！");
            } else {
                attendance.setStatus("LATE");
                redirectAttributes.addFlashAttribute("warning", "打卡成功，但已迟到！下次请准时。");
            }

            attendanceService.save(attendance);

        } catch (Exception e) {
            log.error("打卡失败", e);
            redirectAttributes.addFlashAttribute("error", "打卡失败：" + e.getMessage());
        }

        return "redirect:/attendance/list";
    }

    // ========== 考勤记录列表（教师可见所有学生） ==========

    /**
     * 考勤记录列表页面
     * - 教师/管理员：查看所有学生的考勤记录
     * - 学生：只查看自己的考勤记录
     */
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) Long studentId,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        boolean isTeacher = isTeacherOrAdmin();

        // 构建查询条件
        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 学生只能看自己的记录
            if (!isTeacher) {
                Student student = getCurrentStudent();
                if (student != null) {
                    predicates.add(cb.equal(root.get("studentId"), String.valueOf(student.getId())));
                }
            } else {
                // 教师可以选择查看特定学生的记录
                if (studentId != null && studentId > 0) {
                    predicates.add(cb.equal(root.get("studentId"), String.valueOf(studentId)));
                }
            }

            // 日期范围筛选
            if (startDate != null && !startDate.isEmpty()) {
                LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), startDateTime));
            }

            if (endDate != null && !endDate.isEmpty()) {
                LocalDateTime endDateTime = LocalDate.parse(endDate).plusDays(1).atStartOfDay();
                predicates.add(cb.lessThan(root.get("checkInTime"), endDateTime));
            }

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 课程筛选
            if (courseId != null && courseId > 0) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("checkInTime").descending());
        Page<Attendance> attendancePage = attendanceService.findAll(spec, pageable);

        List<Course> courses = courseRepository.findAll();
        List<Student> students = studentService.findAll();  // 用于教师选择学生

        // 统计信息
        long normalCount = 0, lateCount = 0, absentCount = 0, totalCount = 0;

        if (!isTeacher) {
            // 学生个人统计
            Student student = getCurrentStudent();
            if (student != null) {
                String sid = String.valueOf(student.getId());
                normalCount = attendanceService.countByStudentIdAndStatus(sid, "NORMAL");
                lateCount = attendanceService.countByStudentIdAndStatus(sid, "LATE");
                absentCount = attendanceService.countByStudentIdAndStatus(sid, "ABSENT");
                totalCount = normalCount + lateCount + absentCount;
                model.addAttribute("studentName", student.getName());
            }
        } else {
            // 教师统计（全部）
            totalCount = attendancePage.getTotalElements();
            // 简化统计，实际可做全量统计
            normalCount = attendanceService.countTodayNormal();
            lateCount = attendanceService.countTodayLate();
            model.addAttribute("students", students);
            model.addAttribute("selectedStudentId", studentId);
            model.addAttribute("isTeacher", true);
        }

        model.addAttribute("records", attendancePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", attendancePage.getTotalPages());
        model.addAttribute("totalElements", attendancePage.getTotalElements());
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        model.addAttribute("courseId", courseId);
        model.addAttribute("courses", courses);
        model.addAttribute("normalCount", normalCount);
        model.addAttribute("lateCount", lateCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("totalCount", totalCount);

        return "attendance/list";
    }

    // ========== 批量导入 ==========

    /**
     * 批量导入页面
     */
    @GetMapping("/import")
    public String importPage(Model model) {
        return "attendance/import";
    }

    /**
     * 处理文件上传和导入
     */
    @PostMapping("/import")
    public String importFile(@RequestParam("file") MultipartFile file,
                             RedirectAttributes redirectAttributes) {

        // 验证文件大小
        if (file.getSize() > 10 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "文件大小不能超过10MB");
            return "redirect:/attendance/import";
        }

        try {
            ImportResult result = attendanceService.importFromExcel(file);

            String message = String.format("导入完成！成功：%d条，失败：%d条",
                    result.getSuccessCount(), result.getFailCount());

            if (!result.getErrors().isEmpty()) {
                message += "，错误详情：" + String.join("; ", result.getErrors());
            }

            if (result.getFailCount() > 0) {
                redirectAttributes.addFlashAttribute("warning", message);
            } else {
                redirectAttributes.addFlashAttribute("success", message);
            }

        } catch (Exception e) {
            log.error("导入失败", e);
            redirectAttributes.addFlashAttribute("error", "导入失败：" + e.getMessage());
        }

        return "redirect:/attendance/import";
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        // 创建 Excel 模板
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("考勤记录模板");

            // 创建标题行
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"学号", "课程名称", "打卡时间", "状态", "备注"};

            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                // 设置标题行样式
                org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // 添加示例数据行
            org.apache.poi.ss.usermodel.Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("42411122");
            exampleRow.createCell(1).setCellValue("Java程序设计");
            exampleRow.createCell(2).setCellValue("2026-06-03 08:30:00");
            exampleRow.createCell(3).setCellValue("NORMAL");
            exampleRow.createCell(4).setCellValue("正常打卡");

            // 添加状态说明行
            org.apache.poi.ss.usermodel.Row noteRow = sheet.createRow(3);
            org.apache.poi.ss.usermodel.Cell noteCell = noteRow.createCell(0);
            noteCell.setCellValue("状态说明：NORMAL=正常，LATE=迟到，ABSENT=缺勤");
            org.apache.poi.ss.usermodel.CellStyle noteStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font noteFont = workbook.createFont();
            noteFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.RED.getIndex());
            noteStyle.setFont(noteFont);
            noteCell.setCellStyle(noteStyle);

            // 设置列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 5000);
            }

            // 写入响应
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            HttpHeaders headers_resp = new HttpHeaders();
            headers_resp.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers_resp.setContentDispositionFormData("attachment", "attendance_template.xlsx");

            return new ResponseEntity<>(out.toByteArray(), headers_resp, HttpStatus.OK);
        }
    }

    // ========== 考勤统计 ==========

    /**
     * 考勤统计页面
     */
    @GetMapping("/statistics")
    public String statisticsPage(Model model) {
        boolean isTeacher = isTeacherOrAdmin();

        if (isTeacher) {
            // 教师查看全班统计
            List<Object[]> classStats = attendanceService.getClassStatistics();
            List<Object[]> courseStats = attendanceService.getCourseStatistics();
            model.addAttribute("classStats", classStats);
            model.addAttribute("courseStats", courseStats);
            model.addAttribute("isTeacher", true);
        } else {
            // 学生个人统计
            Student student = getCurrentStudent();
            if (student != null) {
                StatisticsDTO statistics = attendanceService.getStudentStatistics(String.valueOf(student.getId()));
                model.addAttribute("statistics", statistics);
                model.addAttribute("studentName", student.getName());
                model.addAttribute("isTeacher", false);
            }
        }

        // 今日统计
        model.addAttribute("todayCount", attendanceService.countTodayCheckIn());
        model.addAttribute("todayLate", attendanceService.countTodayLate());
        model.addAttribute("todayNormal", attendanceService.countTodayNormal());

        return "attendance/statistics";
    }

    /**
     * 获取学生统计API（JSON格式，用于前端图表）
     */
    @GetMapping("/api/statistics/{studentId}")
    @ResponseBody
    public StatisticsDTO getStudentStatisticsApi(@PathVariable String studentId) {
        return attendanceService.getStudentStatistics(studentId);
    }

    /**
     * 获取班级统计API
     */
    @GetMapping("/api/class-statistics")
    @ResponseBody
    public List<Object[]> getClassStatisticsApi() {
        return attendanceService.getClassStatistics();
    }

    // ========== 文件上传目录管理 ==========

    /**
     * 确保上传目录存在
     */
    @PostConstruct
    public void init() {
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("上传目录创建成功: {}", uploadPath);
            } else {
                log.warn("上传目录创建失败: {}", uploadPath);
            }
        }
    }
}