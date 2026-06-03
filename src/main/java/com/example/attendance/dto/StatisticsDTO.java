package com.example.attendance.dto;

public class StatisticsDTO {
    private long totalCount;
    private long normalCount;
    private long lateCount;
    private long absentCount;
    private double attendanceRate;
    private long weekCount;
    private long monthCount;
    private long monthNormalCount;
    private long monthLateCount;
    private long monthAbsentCount;
    private double monthAttendanceRate;

    public StatisticsDTO() {}

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private StatisticsDTO dto = new StatisticsDTO();

        public Builder totalCount(long totalCount) {
            dto.totalCount = totalCount;
            return this;
        }

        public Builder normalCount(long normalCount) {
            dto.normalCount = normalCount;
            return this;
        }

        public Builder lateCount(long lateCount) {
            dto.lateCount = lateCount;
            return this;
        }

        public Builder absentCount(long absentCount) {
            dto.absentCount = absentCount;
            return this;
        }

        public Builder attendanceRate(double attendanceRate) {
            dto.attendanceRate = attendanceRate;
            return this;
        }

        public Builder weekCount(long weekCount) {
            dto.weekCount = weekCount;
            return this;
        }

        public Builder monthCount(long monthCount) {
            dto.monthCount = monthCount;
            return this;
        }

        public Builder monthNormalCount(long monthNormalCount) {
            dto.monthNormalCount = monthNormalCount;
            return this;
        }

        public Builder monthLateCount(long monthLateCount) {
            dto.monthLateCount = monthLateCount;
            return this;
        }

        public Builder monthAbsentCount(long monthAbsentCount) {
            dto.monthAbsentCount = monthAbsentCount;
            return this;
        }

        public Builder monthAttendanceRate(double monthAttendanceRate) {
            dto.monthAttendanceRate = monthAttendanceRate;
            return this;
        }

        public StatisticsDTO build() {
            return dto;
        }
    }

    // Getters and Setters
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public long getNormalCount() { return normalCount; }
    public void setNormalCount(long normalCount) { this.normalCount = normalCount; }

    public long getLateCount() { return lateCount; }
    public void setLateCount(long lateCount) { this.lateCount = lateCount; }

    public long getAbsentCount() { return absentCount; }
    public void setAbsentCount(long absentCount) { this.absentCount = absentCount; }

    public double getAttendanceRate() { return attendanceRate; }
    public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }

    public long getWeekCount() { return weekCount; }
    public void setWeekCount(long weekCount) { this.weekCount = weekCount; }

    public long getMonthCount() { return monthCount; }
    public void setMonthCount(long monthCount) { this.monthCount = monthCount; }

    public long getMonthNormalCount() { return monthNormalCount; }
    public void setMonthNormalCount(long monthNormalCount) { this.monthNormalCount = monthNormalCount; }

    public long getMonthLateCount() { return monthLateCount; }
    public void setMonthLateCount(long monthLateCount) { this.monthLateCount = monthLateCount; }

    public long getMonthAbsentCount() { return monthAbsentCount; }
    public void setMonthAbsentCount(long monthAbsentCount) { this.monthAbsentCount = monthAbsentCount; }

    public double getMonthAttendanceRate() { return monthAttendanceRate; }
    public void setMonthAttendanceRate(double monthAttendanceRate) { this.monthAttendanceRate = monthAttendanceRate; }
}