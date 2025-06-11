package com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import com.fruitoftek.androidfaceattendance.data.AttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.facerecognition.dto.RecognitionResult;
import com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers.viewmodels.UpdateBioPhotoViewModel;

public class UpdateBioPhotoFaceDetectionActivity extends DetectorActivity {
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide controls that won't be needed
        showAddButton(false);
        showBottomSheet(false);
        setShowConfidence(false);

        // Activate to Add New
        activateFaceFeaturesDetection(true);

        // Capture safe args
        userId = com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers.UpdateBioPhotoFaceDetectionActivityArgs.fromBundle(getIntent().getExtras()).getUserId();

        // Add this User Id to the skip list so that it doesn't interfere with update
        userIdsToSkip.add(userId);
    }

    @Override
    protected void onFaceFeaturesDetected(Bitmap fullPhoto, RecognitionResult recognitionResult) {
        if (userId > -1) {// Upsert BioPhoto in database
            UpdateBioPhotoViewModel updateBioPhotoViewModel = new ViewModelProvider(this).get(UpdateBioPhotoViewModel.class);
            AttendanceDatabase.databaseWriteExecutor.execute(() -> {
                BioPhotos bioPhoto = updateBioPhotoViewModel.upsertBioPhotos(userId, fullPhoto, recognitionResult);
                registerNewFace(bioPhoto);
            });
        }

        // NAVIGATE BACK To Users Upsert Fragment
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            runOnUiThread(() -> {
                super.onBackPressed();
            });
        }
    }

}
