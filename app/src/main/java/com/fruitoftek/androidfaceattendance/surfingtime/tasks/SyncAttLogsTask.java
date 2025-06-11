package com.fruitoftek.androidfaceattendance.surfingtime.tasks;

import android.app.Application;
import java.util.TimerTask;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SurfingTimeService;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SyncAttLogsService;

public class SyncAttLogsTask extends TimerTask {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SyncAttLogsTask";
    private SurfingTimeService surfingTimeService;
    private Application application;
    private SyncAttLogsService syncAttLogsService;

    public SyncAttLogsTask(SurfingTimeService surfingTimeService, SyncAttLogsService syncAttLogsService, Application application) {
        this.surfingTimeService = surfingTimeService;
        this.syncAttLogsService = syncAttLogsService;
        this.application = application;
    }

    @Override
    public void run() {
        if (!surfingTimeService.isEnabled()) {
            LOGGER.i(TAG, "SurfingTime Sync is not enabled");
            return;
        }

        try {
            LOGGER.i(TAG, "SyncAttLogsTask is running...");
            syncAttLogsService.syncPending();
        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error occurred while trying to sync Attendance Records with SurfingTime");
        }
    }
}
