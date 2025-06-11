package com.fruitoftek.androidfaceattendance.surfingtime.tasks;

import android.app.Application;
import org.apache.commons.collections4.CollectionUtils;
import java.util.List;
import java.util.TimerTask;
import com.fruitoftek.androidfaceattendance.data.model.SurfingTimeCommand;
import com.fruitoftek.androidfaceattendance.data.repositories.SurfingTimeCommandsRepository;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SurfingTimeCommandExecutorService;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SurfingTimeService;

public class ExecuteCommandsTask extends TimerTask {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "ExecuteCommandsTask";
    private final SurfingTimeService surfingTimeService;
    private final Application application;
    private final SurfingTimeCommandsRepository surfingTimeCommandsRepository;
    private final SurfingTimeCommandExecutorService surfingTimeCommandExecutorService;

    public ExecuteCommandsTask(SurfingTimeService surfingTimeService, SurfingTimeCommandExecutorService surfingTimeCommandExecutorService, Application application) {
        this.surfingTimeService = surfingTimeService;
        this.application = application;
        surfingTimeCommandsRepository = new SurfingTimeCommandsRepository(application);
        this.surfingTimeCommandExecutorService = surfingTimeCommandExecutorService;
    }

    @Override
    public void run() {
        if (!surfingTimeService.isEnabled()) {
            LOGGER.i(TAG, "SurfingTime Sync is not enabled");
            return;
        }

        try {
            LOGGER.i(TAG, "Executing Commands from SurfingTime");
            List<SurfingTimeCommand> pendingExecuteCommands = surfingTimeCommandsRepository.getAllPendingExecute();
            if (CollectionUtils.isEmpty(pendingExecuteCommands)) {
                LOGGER.i(TAG, "No commands pending to be executed");
                return;
            }

            // Execute commands one at a time
            for (SurfingTimeCommand command : pendingExecuteCommands) {
                try {
                    surfingTimeCommandExecutorService.executeCommand(command);
                } catch (Exception ex) {
                    LOGGER.e(TAG, ex, "Error occurred while trying to execute command from SurfingTime: %s", command.toString());
                }
            }
        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error occurred while trying to execute commands from SurfingTime");
        }
    }

}
