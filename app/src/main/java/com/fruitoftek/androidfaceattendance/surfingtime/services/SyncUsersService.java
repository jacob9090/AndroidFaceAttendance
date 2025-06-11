package com.fruitoftek.androidfaceattendance.surfingtime.services;

import android.app.Application;
import org.apache.commons.collections4.CollectionUtils;
import java.util.Arrays;
import java.util.List;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.data.repositories.BioPhotosRepository;
import com.fruitoftek.androidfaceattendance.data.repositories.UsersRepository;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiBioPhoto;
import com.fruitoftek.androidfaceattendance.surfingtime.dto.ApiUser;

public class SyncUsersService {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "InfoService";
    private final Application application;
    private final SurfingTimeService surfingTimeService;
    private final UsersRepository usersRepository;
    private final BioPhotosRepository bioPhotosRepository;

    public SyncUsersService(SurfingTimeService surfingTimeService, Application application) {
        this.application = application;
        this.surfingTimeService = surfingTimeService;
        usersRepository = new UsersRepository(application);
        bioPhotosRepository = new BioPhotosRepository(application);
    }

    public void syncUserInfo(int userId) throws Exception {
        // Get Users and BioPhotos Pending to Sync
        Users user = usersRepository.findFullById(userId);
        if (user == null) {
            throw new Exception("User not found");
        }
        syncUsers(Arrays.asList(user));
        BioPhotos bioPhoto = user.findFirstBioPhoto();
        if (bioPhoto != null) {
            syncBioPhotos(Arrays.asList(bioPhoto));
        }
    }

    public void syncPendingUserInfo() {
        // Get Users and BioPhotos Pending to Sync
        List<Users> users = usersRepository.getAllUsersPendingSync();
        List<BioPhotos> bioPhotos = bioPhotosRepository.getAllBioPhotosPendingSync();
        syncUsers(users);
        syncBioPhotos(bioPhotos);
    }

    public void syncAllUserInfo() {
        List<Users> users = usersRepository.getAllUsers();
        List<BioPhotos> bioPhotos = bioPhotosRepository.getAllBioPhotosForAttendance();
        syncUsers(users);
        syncBioPhotos(bioPhotos);
    }

    private void syncUsers(List<Users> users) {
        // Syncing Users
        if (CollectionUtils.isNotEmpty(users)) {
            LOGGER.i(TAG, "Syncing user info...");
            for (Users userToSync : users) {
                try {
                    ApiUser apiUser = userToSync.ToApiUser();
                    surfingTimeService.syncUser(apiUser.getUser(), apiUser);
                    usersRepository.markUserAsSynced(userToSync);
                } catch (Exception ex) {
                    LOGGER.e(TAG, ex, "Error occurred while trying to sync User to SurfingTime: %s", userToSync.toString());
                }
            }
        } else {
            LOGGER.i(TAG, "No user info to sync...");
        }
    }

    private void syncBioPhotos(List<BioPhotos> bioPhotos) {
        // Syncing BioPhotos
        if (CollectionUtils.isNotEmpty(bioPhotos)) {
            LOGGER.i(TAG, "Syncing biophotos...");
            for (BioPhotos bioPhotoToSync : bioPhotos) {
                try {
                    ApiBioPhoto apiBioPhoto = bioPhotoToSync.ToApiBioPhoto();
                    surfingTimeService.syncUserBioPhoto(apiBioPhoto.getUser(), apiBioPhoto);
                    bioPhotosRepository.markBioPhotoAsSynced(bioPhotoToSync);
                } catch (Exception ex) {
                    LOGGER.e(TAG, ex, "Error occurred while trying to sync biophoto to SurfingTime: %s", bioPhotoToSync.toString());
                }
            }
        } else {
            LOGGER.i(TAG, "No biophotos to sync...");
        }
    }

}
