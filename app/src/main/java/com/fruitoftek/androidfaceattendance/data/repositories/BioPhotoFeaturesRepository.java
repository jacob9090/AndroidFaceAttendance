package com.fruitoftek.androidfaceattendance.data.repositories;

import android.app.Application;

import com.fruitoftek.androidfaceattendance.data.AttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.dao.BioPhotoFeaturesDao;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotoFeatures;

public class BioPhotoFeaturesRepository {

    private BioPhotoFeaturesDao bioPhotoFeaturesDao;

    public BioPhotoFeaturesRepository(Application application) {
        AttendanceDatabase attendanceDatabase = AttendanceDatabase.getDatabase(application);
        bioPhotoFeaturesDao = attendanceDatabase.bioPhotoFeaturesDao();
    }

    public BioPhotoFeatures findById(int userId, int type) {
        return bioPhotoFeaturesDao.findById(userId, type);
    }

    public void update(BioPhotoFeatures bioPhotoFeatures) {
        bioPhotoFeaturesDao.update(bioPhotoFeatures);
    }

    public void insert(BioPhotoFeatures bioPhotoFeatures) {
        bioPhotoFeaturesDao.insert(bioPhotoFeatures);
    }

    public void upsert(BioPhotoFeatures bioPhotoFeatures) {
        BioPhotoFeatures bioPhotoFeaturesBd = findById(bioPhotoFeatures.user, bioPhotoFeatures.type);
        if (bioPhotoFeaturesBd == null) {
            bioPhotoFeaturesDao.insert(bioPhotoFeatures);
        } else {
            // Update editable fields
            bioPhotoFeaturesBd.features = bioPhotoFeatures.features;
            bioPhotoFeaturesDao.update(bioPhotoFeaturesBd);
        }
    }

}
