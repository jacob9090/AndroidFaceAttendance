package com.fruitoftek.androidfaceattendance.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.fruitoftek.androidfaceattendance.data.model.AttendanceRecord;

@Dao
public interface AttendanceRecordDao {

    @Query("SELECT COUNT(*) FROM AttendanceRecord")
    int getAttendanceRecordCount();

    @Query("SELECT * FROM AttendanceRecord WHERE user = :userId ORDER BY verifyTimeEpochMilliSeconds DESC LIMIT 1")
    AttendanceRecord findLastOneByUserId(int userId);

    @Query("SELECT * FROM AttendanceRecord")
    List<AttendanceRecord> getAll();

    @Query("SELECT * FROM AttendanceRecord WHERE isSync = 0")
    List<AttendanceRecord> getAllPendingSync();

    @Query("SELECT * FROM AttendanceRecord WHERE user = :userId")
    List<AttendanceRecord> findByUserId(int userId);

    @Query("SELECT * FROM AttendanceRecord ORDER BY verifyTimeEpochMilliSeconds DESC")
    LiveData<List<AttendanceRecord>> getAllAttendanceRecordsLive();

    @Query("SELECT * FROM AttendanceRecord ORDER BY verifyTimeEpochMilliSeconds DESC LIMIT :rowCount OFFSET :offset")
    List<AttendanceRecord> queryAttendanceRecordsPaginated(int offset, int rowCount);

    @Query("SELECT * FROM AttendanceRecord WHERE verifyTimeEpochMilliSeconds >= :epochStart AND verifyTimeEpochMilliSeconds <= :epochEnd")
    List<AttendanceRecord> queryAttendanceRecordsByVerifyEpoch(long epochStart, long epochEnd);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(AttendanceRecord attendanceRecord);

    @Update(entity = AttendanceRecord.class)
    void update(AttendanceRecord attendanceRecord);

    @Query("DELETE FROM AttendanceRecord")
    void deleteAll();

}
