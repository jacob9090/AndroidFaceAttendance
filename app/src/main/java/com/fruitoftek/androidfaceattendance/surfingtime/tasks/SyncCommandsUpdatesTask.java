package com.fruitoftek.androidfaceattendance.surfingtime.tasks;

import android.app.Application;
import android.text.TextUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;
import com.fruitoftek.androidfaceattendance.data.model.SurfingTimeCommand;
import com.fruitoftek.androidfaceattendance.data.repositories.SurfingTimeCommandsRepository;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SurfingTimeService;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiCommandUpdate;

public class SyncCommandsUpdatesTask extends TimerTask {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SyncCommandsUpdatesTask";
    private final SurfingTimeService surfingTimeService;
    private final Application application;
    private final SurfingTimeCommandsRepository surfingTimeCommandsRepository;

    public SyncCommandsUpdatesTask(SurfingTimeService surfingTimeService, Application application) {
        this.surfingTimeService = surfingTimeService;
        this.application = application;
        surfingTimeCommandsRepository = new SurfingTimeCommandsRepository(application);
    }

    @Override
    public void run() {
        if (!surfingTimeService.isEnabled()) {
            LOGGER.i(TAG, "SurfingTime Sync is not enabled");
            return;
        }

        try {
            LOGGER.i(TAG, "Syncing Command Updates to SurfingTime");
            List<SurfingTimeCommand> pendingSyncCommands = surfingTimeCommandsRepository.getAllPendingSync();
            if (CollectionUtils.isEmpty(pendingSyncCommands)) {
                LOGGER.i(TAG, "No commands updates pending Sync");
                return;
            }

            List<List<SurfingTimeCommand>> pendingSyncCommandsBatches = ListUtils.partition(pendingSyncCommands, 10);
            // Sync to SurfingTime in batches of 10 attendance records
            for (List<SurfingTimeCommand> batch: pendingSyncCommandsBatches) {
                try {// Map to update and sync with SurfingTime
                    List<ApiCommandUpdate> updates = batch.stream()
                            .map(surfingTimeCommand -> surfingTimeCommand.toApiCommandUpdate())
                            .collect(Collectors.toList());
                    surfingTimeService.pushCommandsUpdates(updates);
                    surfingTimeCommandsRepository.markCommandsAsSynced(batch);
                } catch (Exception ex) {
                    LOGGER.e(TAG, ex, "Error occurred while trying to sync Command Updates batch to SurfingTime: %s", TextUtils.join(", ", batch));
                }
            }

        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error occurred while trying to sync command updates to SurfingTime");
        }
    }

}
