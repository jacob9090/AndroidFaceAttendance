package com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import java.time.Instant;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import com.fruitoftek.androidfaceattendance.R;
import com.fruitoftek.androidfaceattendance.data.SurfingAttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers.viewmodels.AttendanceRecordsViewModel;
import com.fruitoftek.androidfaceattendance.util.Literals;
import com.fruitoftek.androidfaceattendance.util.Util;
import com.fruitoftek.androidfaceattendance.util.VerifyType;

public class AttendanceRecordsByFaceDetectionActivity extends SurfingDetectorActivity {

    private static final Logger LOGGER = new Logger();
    private static String TAG = "AttendanceRecordsByFaceDetectionActivity";
    private long millisAttRecordThreshold = 5000;
    private long millisLastMultiFaceWarnToast = Instant.now().toEpochMilli();
    private Timer timer = new Timer();

    //          UserId, Millis
    private Map<Integer, Long> mapOfUsersLastPunch = new Hashtable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide controls that won't be needed
        showAddButton(false);
        showBottomSheet(false);
        setShowConfidence(false);
        setShowFaceLabel(false);

        // Check Settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        int secondsBetweenValidAttRecords = Integer.parseInt(sharedPreferences.getString("faceAttInterval", "5"));
        millisAttRecordThreshold = secondsBetweenValidAttRecords * 1000;

        // Set the timer task to dequeue recent punches
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Dequeue all the punches below the limit threshold
                List<Integer> punchesToRemove = userPunchesBelowThreshold();
                for (Integer punchToRemove: punchesToRemove) {
                    mapOfUsersLastPunch.remove(punchToRemove);
                }
            }
        };

        // Schedule the timer to pop recent attendance records from the queue
        timer.scheduleAtFixedRate(timerTask, 100, 100);
    }

    @Override
    protected void onFacesRecognized(Bitmap fullPhoto, List<BioPhotos> recognitions) {
        Date date = new Date();
        long now = Instant.now().toEpochMilli();
        if (recognitions.size() > 1) {
            // Shouldn't let register an attendance record if more than one face is recognized
            if ( (now - millisLastMultiFaceWarnToast) >= 2000 ) {// Has 2 seconds passed already
                Toast.makeText(getApplicationContext(), R.string.activity_attendance_record_by_face_multiple_faces, Toast.LENGTH_SHORT).show();
                millisLastMultiFaceWarnToast = now;
            }

            // More than one face is on camera, can't allow to punch
            return;
        }

        BioPhotos bioPhoto = recognitions.get(0);
        int surfingUserId = bioPhoto.user;
        if (isPunchAllowedForUser(surfingUserId)) {
            mapOfUsersLastPunch.put(surfingUserId, now);
            SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
                AttendanceRecord attendanceRecord = mapFaceRecordToAttendanceRecord(fullPhoto, surfingUserId, date);
                AttendanceRecordsViewModel attendanceRecordsViewModel = new ViewModelProvider(this).get(AttendanceRecordsViewModel.class);
                attendanceRecordsViewModel.persistAttendanceRecordWhilePunching(attendanceRecord);

                Users user = attendanceRecordsViewModel.findUserById(surfingUserId);
                if (user != null) {
                    Util.showAttendanceRecordOkAlertDialog(user, AttendanceRecordsByFaceDetectionActivity.this, this);
                }
                LOGGER.i(TAG, "Attendance record persisted for user "+ surfingUserId);
            });
        } else {
            LOGGER.i(TAG, "Attendance record SKIPPED for user "+ surfingUserId);
        }
    }

    /**
     * Check if this punch is valid or should be ruled out as duplicate by comparing the epoch milliseconds
     */
    private boolean isPunchAllowedForUser(int surfingUserId) {
        // If the user is still in the map then he's not allowed to punch
        // to avoid duplicate attendance records
        return !mapOfUsersLastPunch.containsKey(surfingUserId);
    }

    private AttendanceRecord mapFaceRecordToAttendanceRecord(Bitmap fullPhoto, int surfingUserId, Date date) {
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.user = surfingUserId;
        attendanceRecord.verifyTime = Util.getFormatterDateTime(date);
        attendanceRecord.verifyTimeEpochMilliSeconds = date.getTime();
        attendanceRecord.verifyType = VerifyType.FACE.getType();
        attendanceRecord.isSync = Literals.FALSE;

        // Resize fullPhoto and set it to attendance record
        Bitmap attendancePicture = Util.getResizedBitmap(fullPhoto, 120, 120);
        attendanceRecord.setBioPhotoContent(attendancePicture);
        return attendanceRecord;
    }

    private List<Integer> userPunchesBelowThreshold() {
        long now = Instant.now().toEpochMilli();
        long millisBelowThreshold = now - millisAttRecordThreshold;
        return mapOfUsersLastPunch.entrySet().stream()
                // Find all entries which value (millis epoch) is below the threshold
                .filter(entry -> entry.getValue() <= millisBelowThreshold)
                // Recover only the userId part of the entry, which is the key
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }
}
