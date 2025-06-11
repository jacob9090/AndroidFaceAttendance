package com.fruitoftek.androidfaceattendance.surfingtime.tasks;

import android.app.Application;
import java.util.TimerTask;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SurfingTimeService;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SyncInfoService;

public class InfoTask extends TimerTask {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "InfoTask";
    private final SurfingTimeService surfingTimeService;
    private final Application application;
    private final SyncInfoService syncInfoService;

    public InfoTask(SurfingTimeService surfingTimeService, SyncInfoService syncInfoService, Application application) {
        this.surfingTimeService = surfingTimeService;
        this.application = application;
        this.syncInfoService = syncInfoService;
    }

    @Override
    public void run() {
        // Execute INFO so that this device gets registered against SurfingTime (first time)
        // And the device info data on SurfingTime gets updated
        if (!surfingTimeService.isEnabled()) {
            LOGGER.i(TAG, "SurfingTime Sync is not enabled");
            return;
        }

        try {
            LOGGER.i(TAG, "Syncing INFO with SurfingTime");
            syncInfoService.syncInfo();
            LOGGER.i(TAG, "Syncing INFO with SurfingTime success!");
        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error occurred while trying to sync INFO with SurfingTime");
        }
    }
}
