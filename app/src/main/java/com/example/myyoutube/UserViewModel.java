package com.example.myyoutube;

import android.app.Application;
import android.graphics.Bitmap;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    public UserViewModel(Application app) {
        super(app);
        this.userRepository = new UserRepository(app);
    }

    public LiveData<Resource<Boolean>> registerUser(String username, String password, Bitmap profilePicture) {
        return userRepository.registerUser(username, password, profilePicture);
    }

    public LiveData<Resource<LoginResponse>> login(String username, String password) {
        return userRepository.login(username, password);
    }

    public LiveData<Resource<User>> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<Resource<Boolean>> clearCurrentUser() {
        return userRepository.clearCurrentUser();
    }

    public LiveData<Resource<Boolean>> deleteUser(User currentUser) {
        return userRepository.deleteUser(currentUser);
    }

    public LiveData<Resource<User>> getUserById(int userId) {
        return userRepository.getUserById(userId);
    }

    public LiveData<Resource<User>> updateUserProfile(User currentUser, Bitmap profilePicture, String password) {
        return userRepository.updateUserProfile(currentUser, profilePicture, password);
    }
}

