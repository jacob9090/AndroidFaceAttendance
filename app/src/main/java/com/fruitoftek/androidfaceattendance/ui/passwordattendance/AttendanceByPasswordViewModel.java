package com.fruitoftek.androidfaceattendance.ui.passwordattendance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.data.repositories.AttendanceRecordsRepository;
import com.fruitoftek.androidfaceattendance.data.repositories.UsersRepository;
import com.fruitoftek.androidfaceattendance.util.Literals;
import com.fruitoftek.androidfaceattendance.util.Util;
import com.fruitoftek.androidfaceattendance.util.VerifyType;

public class AttendanceByPasswordViewModel extends AndroidViewModel {
    private static String TAG = "AttendanceByPasswordViewModel";
    private AttendanceRecordsRepository attendanceRecordsRepository;
    private UsersRepository usersRepository;

    public AttendanceByPasswordViewModel(@NonNull Application application) {
        super(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
        usersRepository = new UsersRepository(application);
    }

    public boolean validateUserPassword(Users user, String password) {
        if (user == null) {
            return false;
        }

        return StringUtils.equals(user.password, password);
    }

    public Users findUserById(int userId) {
        return usersRepository.findFullById(userId);
    }

    public void persistAttendanceRecordForUserNow(Users user) {
        Date now = new Date();
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.user = user.user;
        attendanceRecord.verifyTime = Util.getFormatterDateTime(now);
        attendanceRecord.verifyTimeEpochMilliSeconds = now.getTime();
        attendanceRecord.verifyType = VerifyType.PASSWORD.getType();
        attendanceRecord.isSync = Literals.FALSE;
        attendanceRecordsRepository.insertWithLocation(attendanceRecord);
    }

}