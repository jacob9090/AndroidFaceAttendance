package com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.data.repositories.AttendanceRecordsRepository;
import com.fruitoftek.androidfaceattendance.data.repositories.UsersRepository;

public class AttendanceRecordsViewModel extends AndroidViewModel {
    private static String TAG = "AttendanceRecordsViewModel";
    private AttendanceRecordsRepository attendanceRecordsRepository;
    private UsersRepository usersRepository;

    public AttendanceRecordsViewModel(@NonNull Application application) {
        super(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
        usersRepository = new UsersRepository(application);
    }

    public Users findUserById(int userId) {
        return usersRepository.findFullById(userId);
    }

    public void persistAttendanceRecordWhilePunching(AttendanceRecord attendanceRecord) {
        attendanceRecordsRepository.persistAttendanceRecordWhilePunching(attendanceRecord);
    }
}