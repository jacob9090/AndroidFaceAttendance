package com.fruitoftek.androidfaceattendance.ui.users;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.data.repositories.UsersRepository;

public class UsersViewModel extends AndroidViewModel {

    // Caching users to be used later for filtering
    public List<Users> allUsers;
    private UsersRepository usersRepository;

    public UsersViewModel(Application application) {
        super(application);
        usersRepository = new UsersRepository(application);
    }

    public LiveData<List<Users>> getAllUsersLive() {
        return usersRepository.getAllUsersLive();
    }
}