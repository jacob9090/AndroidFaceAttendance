package com.fruitoftek.androidfaceattendance.data.repositories;

import android.Manifest;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import com.fruitoftek.androidfaceattendance.data.SurfingAttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.dao.AttendanceRecordDao;
import com.fruitoftek.androidfaceattendance.data.dao.UsersDao;
import com.fruitoftek.androidfaceattendance.data.dto.SearchAttendanceRecordsQuery;
import com.fruitoftek.androidfaceattendance.data.dto.SearchAttendanceRecordsResponse;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.util.Util;

public class AttendanceRecordsRepository {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "AttendanceRecordsRepository";
    private Application application;
    private AttendanceRecordDao attendanceRecordDao;
    private UsersDao usersDao;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private int milliSecondsBetweenValidAttRecords;

    public AttendanceRecordsRepository(Application application) {
        this.application = application;
        SurfingAttendanceDatabase surfingAttendanceDatabase = SurfingAttendanceDatabase.getDatabase(application);
        attendanceRecordDao = surfingAttendanceDatabase.attendanceRecordDao();
        usersDao = surfingAttendanceDatabase.usersDao();

        // Check Settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());
        milliSecondsBetweenValidAttRecords = Integer.parseInt(sharedPreferences.getString("faceAttInterval", "5")) * 1000;

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application);
    }

    public int getAttendanceRecordCount() {
        return attendanceRecordDao.getAttendanceRecordCount();
    }

    // Saves attendance record into DB if the verify time of the last record has passed the
    // minimum time between valid attendance records.
    // This avoids duplicate face records while doing Face Recognition attendance/punching
    public void persistAttendanceRecordWhilePunching(AttendanceRecord attendanceRecord) {
        AttendanceRecord attendanceRecordDb = attendanceRecordDao.findLastOneByUserId(attendanceRecord.user);
        if (attendanceRecordDb == null ||
                (Math.abs(attendanceRecord.verifyTimeEpochMilliSeconds - attendanceRecordDb.verifyTimeEpochMilliSeconds) > milliSecondsBetweenValidAttRecords)) {
            insertWithLocation(attendanceRecord);
        } else {
            LOGGER.d(TAG, "Ruling out duplicate Attendance Record for user " + attendanceRecord.user);
        }
    }

    public List<AttendanceRecord> getAll() {
        List<AttendanceRecord> attendanceRecords = attendanceRecordDao.getAll();
        for (AttendanceRecord attendanceRecord: attendanceRecords) {
            attendanceRecord.userObj = usersDao.findById(attendanceRecord.user);
            attendanceRecord.verifyTypeStr = Util.getVerifyTypeString(attendanceRecord.verifyType, application);
        }
        return attendanceRecords;
    }

    public List<AttendanceRecord> getAllPendingSync() {
        List<AttendanceRecord> attendanceRecords = attendanceRecordDao.getAllPendingSync();
        return attendanceRecords;
    }

    /**
     * Get attendance records between start and end date time
     * @param startTimeStr Date format: "yyyy-MM-dd HH:mm:ss"
     * @param endTimeStr Date format: "yyyy-MM-dd HH:mm:ss"
     */
    public List<AttendanceRecord> queryAttendanceRecordsByVerifyTime(String startTimeStr, String endTimeStr) throws ParseException {
        Date startTime = Util.parseSurfingFormattedDateString(startTimeStr);
        Date endTime = Util.parseSurfingFormattedDateString(endTimeStr);
        long epochStart = startTime.getTime();
        long epochEnd = endTime.getTime();
        List<AttendanceRecord> attendanceRecords = attendanceRecordDao.queryAttendanceRecordsByVerifyEpoch(epochStart, epochEnd);
        return attendanceRecords;
    }

    public void markAttLogsAsSynced(List<AttendanceRecord> attendanceRecords) {
        for (AttendanceRecord attendanceRecord : attendanceRecords) {
            attendanceRecord.isSync = 1;
            update(attendanceRecord);
        }
    }

    public LiveData<List<AttendanceRecord>> getAllAttendanceRecordsLive() {
        return attendanceRecordDao.getAllAttendanceRecordsLive();
    }

    public void insertWithLocation(AttendanceRecord attendanceRecord) {
        if (ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                String geoLocation = null;
                if (location != null) {
                    geoLocation = String.format("%.6f,%.6f", location.getLongitude(), location.getLatitude());
                }
                attendanceRecord.geoLocation = geoLocation;
                SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
                    insert(attendanceRecord);
                });
            });
        } else {
            insert(attendanceRecord);
        }
    }

    public void insert(AttendanceRecord attendanceRecord) {
        attendanceRecordDao.insert(attendanceRecord);
    }

    public void update(AttendanceRecord attendanceRecord) {
        attendanceRecordDao.update(attendanceRecord);
    }

    public void deleteAll() {
        attendanceRecordDao.deleteAll();
    }

    /**
     *
     * @param query Not used for now
     * @param pageNumber Starting from 1
     * @param pageSize Number of records returned
     * @return
     */
    public ListenableFuture<SearchAttendanceRecordsResponse> searchAttendanceRecords(
            SearchAttendanceRecordsQuery query, int pageNumber, int pageSize) {
        return Futures.submit(() -> {
            int rowCount = pageSize;
            int offset = rowCount * (pageNumber-1);
            List<AttendanceRecord> attendanceRecords = attendanceRecordDao.queryAttendanceRecordsPaginated(offset, rowCount);
            int returnedCount = attendanceRecords.size();
            Integer nextPage = null;
            if (returnedCount >= pageSize) {
                nextPage = pageNumber+1;
            }
            LOGGER.i(TAG, String.format("searchAttendanceRecords rowCount %d, offset %d, returned %d", rowCount, offset, returnedCount));
            return new SearchAttendanceRecordsResponse(attendanceRecords, nextPage);
        }, SurfingAttendanceDatabase.databaseWriteExecutor);
    }
}
