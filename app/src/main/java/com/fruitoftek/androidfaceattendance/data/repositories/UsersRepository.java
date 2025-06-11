package com.fruitoftek.androidfaceattendance.data.repositories;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import com.fruitoftek.androidfaceattendance.data.SurfingAttendanceDatabase;
import com.fruitoftek.androidfaceattendance.data.dao.BioPhotosDao;
import com.fruitoftek.androidfaceattendance.data.dao.UsersDao;
import com.fruitoftek.androidfaceattendance.data.model.BioPhotos;
import com.fruitoftek.androidfaceattendance.data.model.Users;

public class UsersRepository {
    private UsersDao usersDao;
    private BioPhotosDao bioPhotosDao;

    public UsersRepository(Application application) {
        SurfingAttendanceDatabase surfingAttendanceDatabase = SurfingAttendanceDatabase.getDatabase(application);
        usersDao = surfingAttendanceDatabase.usersDao();
        bioPhotosDao = surfingAttendanceDatabase.bioPhotosDao();
    }

    public int getUsersCount() {
        return usersDao.getUsersCount();
    }

    public Users findFullById(int userId) {
        Users user = usersDao.findById(userId);
        if (user == null) {
            return null;
        }
        List<BioPhotos> bioPhotos = bioPhotosDao.getAllBioPhotosForUser(userId);
        user.BioPhotos = bioPhotos;
        return user;
    }

    public Users findById(int userId) {
        return usersDao.findById(userId);
    }

    public List<Users> getAllUsers() {
        return usersDao.getAllUsers();
    }

    public List<Users> getAllUsersPendingSync() {
        return usersDao.getAllUsersPendingSync();
    }

    public int nextId() {
        int userId = usersDao.nextId();
        userId = userId == 0 ? 1 : userId;
        return userId;
    }

    public LiveData<List<Users>> getAllUsersLive() {
        return usersDao.getAllUsersLive();
    }

    public void insert(Users user) {
        usersDao.insert(user);
    }

    public void markUserAsSynced(Users user) {
        user.isSync = 1;
        update(user);
    }

    public void update(Users user) {
        usersDao.update(user);
    }

    public void upsert(Users user) {
        Users userDb = findById(user.user);
        if (userDb == null) {
            insert(user);
        } else {
            // Copy updatable data
            userDb.privilege = user.privilege;
            userDb.name = user.name;
            userDb.status = user.status;
            userDb.password = user.password;
            userDb.mainCard = user.mainCard;
            userDb.photoIdName = user.photoIdName;
            userDb.photoIdSize = user.photoIdSize;
            userDb.photoIdContent = user.photoIdContent;
            userDb.lastUpdated = user.lastUpdated;
            userDb.isSync = user.isSync;
            update(userDb);
        }
    }

    public void deleteAll() {
        usersDao.deleteAll();
    }

    public void deleteById(int userId) {
        usersDao.deleteById(userId);
    }
}
