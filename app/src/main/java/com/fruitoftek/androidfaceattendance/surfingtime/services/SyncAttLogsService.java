package com.fruitoftek.androidfaceattendance.surfingtime.services;

import android.app.Application;
import android.text.TextUtils;
import org.apache.commons.collections4.ListUtils;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.data.repositories.AttendanceRecordsRepository;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiAttLog;

public class SyncAttLogsService {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SyncAttLogsService";
    private SurfingTimeService surfingTimeService;
    private Application application;
    private AttendanceRecordsRepository attendanceRecordsRepository;


    public SyncAttLogsService(SurfingTimeService surfingTimeService, Application application) {
        this.surfingTimeService = surfingTimeService;
        this.application = application;
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
    }

    public void syncPending() {
        List<AttendanceRecord> attendanceRecordsPendingSync = attendanceRecordsRepository.getAllPendingSync();
        if (attendanceRecordsPendingSync.isEmpty()) {
            LOGGER.i(TAG, "No Attendance Records to sync");
            return;
        }
        sync(attendanceRecordsPendingSync);
    }

    /**
     * Sync attendance records to SurfingTime between start and end date time
     * @param startTimeStr Date format: "yyyy-MM-dd HH:mm:ss"
     * @param endTimeStr Date format: "yyyy-MM-dd HH:mm:ss"
     */
    public void syncByDates(String startTimeStr, String endTimeStr) throws ParseException {
        List<AttendanceRecord> attendanceRecordsPendingSync = attendanceRecordsRepository.queryAttendanceRecordsByVerifyTime(startTimeStr, endTimeStr);
        if (attendanceRecordsPendingSync.isEmpty()) {
            LOGGER.i(TAG, "No Attendance Records to sync");
            return;
        }
        sync(attendanceRecordsPendingSync);
    }

    private void sync(List<AttendanceRecord> attendanceRecords) {
        List<List<AttendanceRecord>> attendanceRecordsBatches = ListUtils.partition(attendanceRecords, 10);
        // Sync to SurfingTime in batches of 10 attendance records
        for (List<AttendanceRecord> batch : attendanceRecordsBatches) {
            try {// Map records and sync to SurfingTime
                List<ApiAttLog> apiAttLogs = batch.stream()
                        .map(attendanceRecord -> attendanceRecord.toApiAttLog())
                        .collect(Collectors.toList());
                surfingTimeService.syncAttendanceLogs(apiAttLogs);
                attendanceRecordsRepository.markAttLogsAsSynced(batch);
            } catch (Exception ex) {
                LOGGER.e(TAG, ex, "Error occurred while trying to sync Attendance Record batch to SurfingTime: %s", TextUtils.join(", ", batch));
            }
        }
    }

}
