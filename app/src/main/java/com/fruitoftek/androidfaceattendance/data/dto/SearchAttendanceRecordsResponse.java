package com.fruitoftek.androidfaceattendance.data.dto;

import java.util.List;

import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;

public class SearchAttendanceRecordsResponse {
    private List<AttendanceRecord> attendanceRecords;
    private Integer nextPageNumber;

    public SearchAttendanceRecordsResponse(List<AttendanceRecord> attendanceRecords, Integer nextPageNumber) {
        this.attendanceRecords = attendanceRecords;
        this.nextPageNumber = nextPageNumber;
    }

    public List<AttendanceRecord> getAttendanceRecords() {
        return attendanceRecords;
    }

    public Integer getNextPageNumber() {
        return nextPageNumber;
    }
}
