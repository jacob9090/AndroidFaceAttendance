package com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers;

import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;
import com.fruitoftek.androidfaceattendance.data.AttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.ui.facedetectionwrappers.viewmodels.DetectorViewModel;

public class DetectorActivity extends com.fruitoftek.androidfaceattendance.detection.DetectorActivity {

    protected List<Integer> userIdsToSkip = new ArrayList<>();

    @Override
    protected List<BioPhotos> initializeFaceRegistry() {
        // Build Face Registry from DB
        DetectorViewModel detectorViewModel = new ViewModelProvider(this).get(DetectorViewModel.class);
        AttendanceDatabase.databaseWriteExecutor.execute(() -> {
            // Recover all faces from DB and register for detection
            List<BioPhotos> faceRegistry = detectorViewModel.getFaceRegistry();
            for(BioPhotos bioPhoto: faceRegistry) {
                if (!userIdsToSkip.contains(bioPhoto.user)) {
                    registerNewFace(bioPhoto);
                }
            }
        });

        // Return an empty array for now, while faces are fetched from DB
        return new ArrayList<>();
    }
}
