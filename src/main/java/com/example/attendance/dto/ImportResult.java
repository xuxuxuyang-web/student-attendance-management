package com.example.attendance.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int successCount = 0;
    private int failCount = 0;
    private List<String> errors = new ArrayList<>();

    public void incrementSuccess() {
        this.successCount++;
    }

    public void incrementFail() {
        this.failCount++;
    }

    public void addError(String error) {
        this.errors.add(error);
        this.failCount++;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}