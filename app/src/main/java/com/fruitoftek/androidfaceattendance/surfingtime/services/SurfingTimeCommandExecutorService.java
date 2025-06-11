package com.fruitoftek.androidfaceattendance.surfingtime.services;

import android.app.Application;

import com.fruitoftek.androidfaceattendance.data.model.BioPhotoFeatures;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.data.model.SurfingTimeCommand;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.data.repositories.AttendanceRecordsRepository;
import com.fruitoftek.androidfaceattendance.data.repositories.BioPhotoFeaturesRepository;
import com.fruitoftek.androidfaceattendance.data.repositories.BioPhotosRepository;
import com.fruitoftek.androidfaceattendance.data.repositories.SurfingTimeCommandsRepository;
import com.fruitoftek.androidfaceattendance.data.repositories.UsersRepository;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.facerecognition.FaceDetectionAndRecognitionService;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiBioPhoto;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiUser;
import com.fruitoftek.androidfaceattendance.util.Util;

import static com.fruitoftek.androidfaceattendance.surfingtime.util.CommandLiterals.*;
import static com.fruitoftek.androidfaceattendance.util.Literals.TRUE;

import com.jakewharton.processphoenix.ProcessPhoenix;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SurfingTimeCommandExecutorService {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SurfingTimeCommandExecutorService";
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final SurfingTimeService surfingTimeService;
    private final SyncInfoService syncInfoService;
    private final SyncAttLogsService syncAttLogsService;
    private final SyncUsersService syncUsersService;
    private final FaceDetectionAndRecognitionService faceDetectionAndRecognitionService;
    private final Application application;
    private final SurfingTimeCommandsRepository surfingTimeCommandsRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final BioPhotosRepository bioPhotosRepository;
    private final BioPhotoFeaturesRepository bioPhotoFeaturesRepository;
    private final UsersRepository usersRepository;

    public SurfingTimeCommandExecutorService(SurfingTimeService surfingTimeService,
                                             SyncInfoService syncInfoService,
                                             SyncAttLogsService syncAttLogsService,
                                             SyncUsersService syncUsersService,
                                             Application application) {
        this.surfingTimeService = surfingTimeService;
        this.syncInfoService = syncInfoService;
        this.syncAttLogsService = syncAttLogsService;
        this.syncUsersService = syncUsersService;
        this.application = application;
        faceDetectionAndRecognitionService = new FaceDetectionAndRecognitionService(application.getApplicationContext());
        surfingTimeCommandsRepository = new SurfingTimeCommandsRepository(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
        bioPhotosRepository = new BioPhotosRepository(application);
        bioPhotoFeaturesRepository = new BioPhotoFeaturesRepository(application);
        usersRepository = new UsersRepository(application);
    }

    /**
     * Tries to execute a command and stores the result in the command record
     */
    public void executeCommand(SurfingTimeCommand surfingTimeCommand) {
        CommandResult result;
        if (surfingTimeCommand.command.startsWith(DEV_CMD_CLEAR_LOG)) {
            result = executeClearLog();
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_CLEAR_DATA)) {
            result = executeClearData();
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_INFO)) {
            result = executeInfo();
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_REBOOT)) {
            result = executeReboot();
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_DATA_UPDATE_USERINFO)) {
            result = executeUpdateUser(surfingTimeCommand.command);
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_DATA_UPDATE_BIOPHOTO_URL)) {
            result = executeUpdateBioPhoto(surfingTimeCommand.command);
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_DATA_DELETE_USERINFO)) {
            result = executeDeleteUser(surfingTimeCommand.command);
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_DATA_CLEAR_USERINFO)) {
            result = executeDeleteAllUserInfo(surfingTimeCommand.command);
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_DATA_QUERY_ATTLOG)) {
            result = queryAttendanceLog(surfingTimeCommand.command);
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_DATA_QUERY_USERINFO_BY_PIN)) {
            result = queryUserInfo(surfingTimeCommand.command);
        } else if (surfingTimeCommand.command.startsWith(DEV_CMD_DATA_QUERY_USERINFO)) {
            result = queryAllUserInfo(surfingTimeCommand.command);
        } else {
            result = new CommandResult();
            String errrMsg = String.format(UNKNOWN_COMMAND_ERROR_MSG, surfingTimeCommand.command);
            LOGGER.e(TAG, errrMsg);
            result.cmdReturn = FAILED_UNKNOWN;
            result.cmdReturnInfo = errrMsg;
        }

        surfingTimeCommand.executedOn = Util.getDateTimeNow();
        surfingTimeCommand.cmdReturn = result.cmdReturn;
        surfingTimeCommand.cmdReturnInfo = result.cmdReturnInfo;
        surfingTimeCommand.isExecuted = TRUE;
        surfingTimeCommandsRepository.update(surfingTimeCommand);
    }

    private CommandResult executeClearLog() {
        return catchableCommandExecutor(() -> {
            attendanceRecordsRepository.deleteAll();
        }, DEV_CMD_CLEAR_LOG, DEV_CMD_CLEAR_LOG_SUCCESS_MSG, DEV_CMD_CLEAR_LOG_ERROR_MSG);
    }

    private CommandResult executeClearData() {
        return catchableCommandExecutor(() -> {
            attendanceRecordsRepository.deleteAll();
            usersRepository.deleteAll();
            bioPhotosRepository.deleteAll();
        }, DEV_CMD_CLEAR_DATA, DEV_CMD_CLEAR_DATA_SUCCESS_MSG, DEV_CMD_CLEAR_DATA_ERROR_MSG);
    }

    private CommandResult executeInfo() {
        return catchableCommandExecutor(() -> {
            syncInfoService.syncInfo();
        }, DEV_CMD_INFO, DEV_CMD_INFO_SUCCESS_MSG, DEV_CMD_INFO_ERROR_MSG);
    }

    private CommandResult executeReboot() {
        return catchableCommandExecutor(() -> {
            // Not gonna actually reboot the Device, instead gonna just restart the app
            executorService.schedule(() -> {
                // Wait 5 seconds and then restart the whole SurfingAttendance app
                ProcessPhoenix.triggerRebirth(application);
            }, 5, TimeUnit.SECONDS);
        }, DEV_CMD_REBOOT, DEV_CMD_REBOOT_SUCCESS_MSG, DEV_CMD_REBOOT_ERROR_MSG);
    }

    private CommandResult executeUpdateUser(String cmd) {
        return catchableCommandExecutor(() -> {
            int userId = extractUserFromCmd(cmd);
            // Fetch user from SurfingTime and upsert into internal DB
            ApiUser apiUser = surfingTimeService.getUserById(userId);
            Users user = Users.fromApiUser(apiUser);
            usersRepository.upsert(user);
        }, cmd, DEV_CMD_DATA_UPDATE_USERINFO_SUCCESS_MSG, DEV_CMD_DATA_UPDATE_USERINFO_ERROR_MSG);
    }

    private CommandResult executeUpdateBioPhoto(String cmd) {
        return catchableCommandExecutor(() -> {
            int userId = extractUserFromCmd(cmd);
            // Fetch BioPhoto from SurfingTime and from internal DB
            ApiBioPhoto apiBioPhoto = surfingTimeService.getBioPhotoForUser(userId);
            BioPhotos bioPhoto = BioPhotos.fromApiBioPhoto(apiBioPhoto);

            // Extracts features and thumbnail from the BioPhoto and then sets them inside the object
            faceDetectionAndRecognitionService.setBioPhotoFeaturesToBioPhoto(bioPhoto);
            BioPhotoFeatures bioPhotoFeatures = bioPhoto.Features;
            BioPhotos thumbNailBioPhoto = bioPhoto.Thumbnail;
            thumbNailBioPhoto.isSync = TRUE;

            // Persist into internal DB
            bioPhotosRepository.upsertBioPhoto(bioPhoto);
            bioPhotoFeaturesRepository.upsert(bioPhotoFeatures);
            bioPhotosRepository.upsertBioPhoto(thumbNailBioPhoto);
        }, cmd, DEV_CMD_DATA_UPDATE_BIOPHOTO_URL_SUCCESS_MSG, DEV_CMD_DATA_UPDATE_BIOPHOTO_URL_ERROR_MSG);
    }

    private CommandResult executeDeleteUser(String cmd) {
        return catchableCommandExecutor(() -> {
            int userId = extractUserFromCmd(cmd);
            bioPhotosRepository.deleteForUser(userId);
            usersRepository.deleteById(userId);
        }, cmd, DEV_CMD_DATA_DELETE_USERINFO_SUCCESS_MSG, DEV_CMD_DATA_DELETE_USERINFO_ERROR_MSG);
    }

    private CommandResult executeDeleteAllUserInfo(String cmd) {
        return catchableCommandExecutor(() -> {
            bioPhotosRepository.deleteAll();
            usersRepository.deleteAll();
        }, cmd, DEV_CMD_DATA_CLEAR_USERINFO_SUCCESS_MSG, DEV_CMD_DATA_CLEAR_USERINFO_ERROR_MSG);
    }

    private CommandResult queryAttendanceLog(String cmd) {
        return catchableCommandExecutor(() -> {
            String startTime = extractFieldFromCommand(cmd, STARTTIME_CMD_FIELD);
            String endTime = extractFieldFromCommand(cmd, ENDTIME_CMD_FIELD);
            syncAttLogsService.syncByDates(startTime, endTime);
        }, cmd, DEV_CMD_DATA_QUERY_ATTLOG_SUCCESS_MSG, DEV_CMD_DATA_QUERY_ATTLOG_ERROR_MSG);
    }

    private CommandResult queryUserInfo(String cmd) {
        return catchableCommandExecutor(() -> {
            int userId = extractUserFromCmd(cmd);
            syncUsersService.syncUserInfo(userId);
        }, cmd, DEV_CMD_DATA_QUERY_USERINFO_BY_PIN_SUCCESS_MSG, DEV_CMD_DATA_QUERY_USERINFO_BY_PIN_ERROR_MSG);
    }

    private CommandResult queryAllUserInfo(String cmd) {
        return catchableCommandExecutor(() -> {
            syncUsersService.syncAllUserInfo();
        }, cmd, DEV_CMD_DATA_QUERY_USERINFO_SUCCESS_MSG, DEV_CMD_DATA_QUERY_USERINFO_ERROR_MSG);
    }

    private CommandResult catchableCommandExecutor(Executable executable, String cmdInfo, String successMsg, String errorMsgTemplate) {
        CommandResult result = new CommandResult();
        try {
            String logMsg = String.format(INFO_LOG_TEMPLATE, cmdInfo);
            LOGGER.i(TAG, logMsg);
            executable.execute();
            result.cmdReturn = SUCCESS;
            result.cmdReturnInfo = successMsg;
        } catch (Exception ex) {
            String errrMsg = String.format(errorMsgTemplate, ex.getMessage());
            LOGGER.e(TAG, ex, errrMsg);
            result.cmdReturn = FAILED;
            result.cmdReturnInfo = errrMsg;
        }
        return result;
    }

    private int extractUserFromCmd(String cmd) throws Exception {
        String pin = extractFieldFromCommand(cmd, USER_CMD_FIELD);
        if (!NumberUtils.isParsable(pin)) {
            String errMsg = String.format("PIN value is not a number inside command %s", cmd);
            throw new Exception(errMsg);
        }

        // Try to parse the String as an Integer
        return Integer.parseInt(pin);
    }

    private String extractFieldFromCommand(String cmd, String field) throws Exception {
        String[] cmdSegments = cmd.trim().split("\t");
        String pinCmdSegment = Arrays.stream(cmdSegments)
                .filter(segment -> segment.contains(field))
                .findFirst().orElse(null);
        if (StringUtils.isEmpty(pinCmdSegment)) {
            String errMsg = String.format("No valid field %s inside command %s", field, cmd);
            throw new Exception(errMsg);
        }

        String fieldVal = StringUtils.substringAfter(pinCmdSegment, field).trim();
        if (StringUtils.isEmpty(fieldVal) ) {
            String errMsg = String.format("Field value is empty for command %s", cmd);
            throw new Exception(errMsg);
        }

        return fieldVal;
    }

    private interface Executable {
        void execute() throws Exception;
    }

    private class CommandResult {
        // For Example:
        // "0"  - Means the command executed successfully
        // "-1" - Means Unknown Command
        private String cmdReturn;

        // The description of the command return code
        // Like
        // "Command executed successfully"
        // "Unknown Command"
        private String cmdReturnInfo;

        public String getCmdReturn() {
            return cmdReturn;
        }

        public void setCmdReturn(String cmdReturn) {
            this.cmdReturn = cmdReturn;
        }

        public String getCmdReturnInfo() {
            return cmdReturnInfo;
        }

        public void setCmdReturnInfo(String cmdReturnInfo) {
            this.cmdReturnInfo = cmdReturnInfo;
        }
    }

}
