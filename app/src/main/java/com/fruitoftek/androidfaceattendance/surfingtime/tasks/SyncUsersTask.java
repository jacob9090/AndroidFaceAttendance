package com.fruitoftek.androidfaceattendance.surfingtime.tasks;

import android.app.Application;
import java.util.TimerTask;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SurfingTimeService;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SyncUsersService;

public class SyncUsersTask extends TimerTask {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SyncUsersTask";
    private final SurfingTimeService surfingTimeService;
    private final SyncUsersService syncUsersService;
    private final Application application;

    public SyncUsersTask(SurfingTimeService surfingTimeService,
                         SyncUsersService syncUsersService,
                         Application application) {
        this.surfingTimeService = surfingTimeService;
        this.syncUsersService = syncUsersService;
        this.application = application;
    }


    @Override
    public void run() {
        if (!surfingTimeService.isEnabled()) {
            LOGGER.i(TAG, "SurfingTime Sync is not enabled");
            return;
        }

        try {
            // Sync Users, Profile Pictures and BioPhotos
            LOGGER.i(TAG, "SyncUsersTask is running...");
            syncUsersService.syncPendingUserInfo();
        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error occurred while trying to sync Users and BioPhotos with SurfingTime");
        }
    }
}
