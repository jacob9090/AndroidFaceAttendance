package com.fruitoftek.androidfaceattendance.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.fruitoftek.androidfaceattendance.data.model.SurfingTimeCommand;

@Dao
public interface SurfingTimeCommandsDao {

    @Query("SELECT * FROM SurfingTimeCommand WHERE id = :id")
    SurfingTimeCommand findById(long id);

    @Query("SELECT * FROM SurfingTimeCommand WHERE isExecuted = 0")
    List<SurfingTimeCommand> getAllPendingExecute();

    @Query("SELECT COUNT(*) FROM SurfingTimeCommand WHERE isSync = 0")
    int countPendingSync();

    @Query("SELECT * FROM SurfingTimeCommand WHERE isExecuted = 1 AND isSync = 0")
    List<SurfingTimeCommand> getAllPendingSync();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(SurfingTimeCommand surfingTimeCommand);

    @Update(entity = SurfingTimeCommand.class)
    void update(SurfingTimeCommand surfingTimeCommand);
}
