package com.fruitoftek.androidfaceattendance.data.repositories;

import android.app.Application;
import java.util.List;
import com.fruitoftek.androidfaceattendance.data.SurfingAttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.dao.SurfingTimeCommandsDao;
import com.fruitoftek.androidfaceattendance.data.model.SurfingTimeCommand;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;

public class SurfingTimeCommandsRepository {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SurfingTimeCommandsRepository";

    private SurfingTimeCommandsDao surfingTimeCommandsDao;

    public SurfingTimeCommandsRepository(Application application) {
        SurfingAttendanceDatabase surfingAttendanceDatabase = SurfingAttendanceDatabase.getDatabase(application);
        surfingTimeCommandsDao = surfingAttendanceDatabase.surfingTimeCommandsDao();
    }

    public List<SurfingTimeCommand> getAllPendingSync() {
        return surfingTimeCommandsDao.getAllPendingSync();
    }

    public List<SurfingTimeCommand> getAllPendingExecute() {
        return surfingTimeCommandsDao.getAllPendingExecute();
    }

    public int countPendingSync() {
        // A command is considered in Sync with SurfingTime when it has been executed
        // successful or unsuccessful and when that update is synced to SurfingTime
        return surfingTimeCommandsDao.countPendingSync();
    }

    public void markCommandsAsSynced(List<SurfingTimeCommand> surfingTimeCommands) {
        for (SurfingTimeCommand surfingTimeCommand : surfingTimeCommands) {
            surfingTimeCommand.isSync = 1;
            update(surfingTimeCommand);
        }
    }

    public void upsertCommands(List<SurfingTimeCommand> commands) {
        for (SurfingTimeCommand surfingTimeCommand : commands) {
            SurfingTimeCommand surfingTimeCommandDb = surfingTimeCommandsDao.findById(surfingTimeCommand.id);
            if (surfingTimeCommandDb == null) {
                surfingTimeCommandsDao.insert(surfingTimeCommand);
            } else {
                surfingTimeCommandDb.command = surfingTimeCommand.command;
                surfingTimeCommandDb.receivedOn = surfingTimeCommand.receivedOn;
                surfingTimeCommandDb.executedOn = surfingTimeCommand.executedOn;
                surfingTimeCommandDb.isSync = surfingTimeCommand.isSync;
                surfingTimeCommandDb.isExecuted = surfingTimeCommand.isExecuted;
                surfingTimeCommandsDao.update(surfingTimeCommandDb);
            }
        }
    }

    public void insertIgnore(List<SurfingTimeCommand> commands) {
        for (SurfingTimeCommand surfingTimeCommand : commands) {
            SurfingTimeCommand surfingTimeCommandDb = surfingTimeCommandsDao.findById(surfingTimeCommand.id);
            if (surfingTimeCommandDb == null) {
                surfingTimeCommandsDao.insert(surfingTimeCommand);
            } else {
                LOGGER.i(TAG, "Ignoring incoming command which already exist in db: %s", surfingTimeCommandDb);
            }
        }
    }

    public void insert(SurfingTimeCommand surfingTimeCommand) {
        surfingTimeCommandsDao.insert(surfingTimeCommand);
    }

    public void update(SurfingTimeCommand surfingTimeCommand) {
        surfingTimeCommandsDao.update(surfingTimeCommand);
    }

}
