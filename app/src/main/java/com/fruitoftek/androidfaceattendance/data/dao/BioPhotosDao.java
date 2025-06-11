package com.fruitoftek.androidfaceattendance.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;

@Dao
public interface BioPhotosDao {

    @Query("SELECT COUNT(*) FROM BioPhotos")
    int bioPhotosCount();

    @Query("SELECT * FROM BioPhotos WHERE user = :userId AND type = :type")
    BioPhotos findById(int userId, int type);

    @Query("SELECT * FROM BioPhotos WHERE user = :userId")
    List<BioPhotos> getAllBioPhotosForUser(int userId);

    @Query("SELECT * FROM BioPhotos WHERE isSync = 0 AND type = 9")
    List<BioPhotos> getAllBioPhotosPendingSync();

    @Query("SELECT COUNT(*) FROM BioPhotos WHERE user = :userId")
    int countAllBioPhotosForUser(int userId);

    @Query("SELECT * FROM BioPhotos WHERE type = 9")
    List<BioPhotos> getAllBioPhotosForAttendance();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BioPhotos bioPhotos);

    @Update(entity = BioPhotos.class)
    void update(BioPhotos bioPhotos);

    @Query("DELETE FROM BioPhotos")
    void deleteAll();

    @Query("DELETE FROM BioPhotos WHERE user = :userId")
    void deleteForUser(int userId);
}
