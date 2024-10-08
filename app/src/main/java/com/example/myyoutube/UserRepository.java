package com.example.myyoutube;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final Context context;
    private VideoApi videoApi;
    private UserDao userDao;

    public UserRepository(Context context) {
        this.context = context;
        videoApi = RetrofitClient.getRetrofitInstance().create(VideoApi.class);
        AppDatabase appDatabase = DatabaseClient.getInstance(context).getAppDatabase();
        userDao = appDatabase.userDao();
    }

    public LiveData<Resource<Boolean>> registerUser(String username, String password, Bitmap profilePicture) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        // Prepare the form fields
        RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);
        RequestBody passwordBody = RequestBody.create(MediaType.parse("text/plain"), password);

        // Convert Bitmap to ByteArray
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        profilePicture.compress(Bitmap.CompressFormat.JPEG, 100, bos); // You can change the format and quality
        byte[] imageBytes = bos.toByteArray();

        // Create the RequestBody for the profile picture
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        String id = UUID.randomUUID().toString();
        MultipartBody.Part profilePicturePart = MultipartBody.Part.createFormData("profilePicture", "profile_" + id + ".jpg", requestFile);

        // Make the network call
        videoApi.registerUser(usernameBody, passwordBody, profilePicturePart).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    result.postValue(new Resource<>(true, null));
                } else {
                    result.postValue(new Resource<>(false, "Registration failed"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                result.postValue(new Resource<>(false, t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<LoginResponse>> login(String username, String password) {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();

        LoginRequest loginRequest = new LoginRequest(username, password);
        videoApi.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    new Thread(() -> {
                        User user = loginResponse.getUser();
                        user.setToken(loginResponse.getToken());
                        userDao.insertUser(user);

                        result.postValue(Resource.success(response.body()));
                    }).start();
                } else {
                    result.postValue(Resource.error("Invalid username or password"));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                result.postValue(Resource.error("Login failed: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<User>> getCurrentUser() {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();

        new Thread(() -> {
            try {
                User userWithToken = userDao.getUserWithToken();
                result.postValue(Resource.success(userWithToken));
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage()));
            }
        }).start();

        return result;
    }


    public LiveData<Resource<Boolean>> clearCurrentUser() {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        new Thread(() -> {
            try {
                userDao.deleteUserWithToken();
                result.postValue(Resource.success(true));
            } catch (Exception e) {
                result.postValue(Resource.error(e.getMessage()));
            }
        }).start();

        return result;
    }

    public LiveData<Resource<Boolean>> deleteUser(User currentUser) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();

        videoApi.deleteUser(currentUser.getToken(), currentUser.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    new Thread(() -> {
                        try {
                            userDao.deleteUserWithToken();
                        } catch (Exception e) { }
                        result.postValue(Resource.success(true));
                    }).start();
                } else if (response.code() == 400) {
                    result.postValue(Resource.error("User does not exist"));
                } else {
                    result.postValue(Resource.error("Failed to delete user"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                result.postValue(Resource.error("Request failed"));
            }
        });

        return result;
    }

    public LiveData<Resource<User>> getUserById(int userId) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();

        videoApi.getUserById(userId).enqueue(new Callback<UserResult>() {
            @Override
            public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(response.body().getUser()));
                } else if (response.code() == 400) {
                    result.postValue(Resource.error("User does not exist"));
                } else {
                    result.postValue(Resource.error("Failed to retrieve user"));
                }
            }

            @Override
            public void onFailure(Call<UserResult> call, Throwable t) {
                result.postValue(Resource.error("Request failed"));
            }
        });

        return result;
    }


    public LiveData<Resource<User>> updateUserProfile(User currentUser, Bitmap profilePicture, String password) {
        MutableLiveData<Resource<User>> result = new MutableLiveData<>();

        // Create the RequestBody for the password
        RequestBody passwordBody = RequestBody.create(MediaType.parse("multipart/form-data"), password);

        // Create the MultipartBody.Part for the profile picture if it exists
        MultipartBody.Part profilePicturePart = null;
        if (profilePicture != null) {
            // Convert Bitmap to ByteArray
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            profilePicture.compress(Bitmap.CompressFormat.JPEG, 100, bos); // You can change the format and quality
            byte[] imageBytes = bos.toByteArray();

            // Create the RequestBody for the profile picture
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
            String id = UUID.randomUUID().toString();
            profilePicturePart = MultipartBody.Part.createFormData("profilePicture", "profile_" + id + ".jpg", requestFile);
        }

        // Make the Retrofit call asynchronously
        videoApi.editUser(currentUser.getToken(), currentUser.getId(), profilePicturePart, passwordBody).enqueue(new Callback<UserResult>() {
            @Override
            public void onResponse(Call<UserResult> call, Response<UserResult> response) {
                if (response.isSuccessful()) {
                    if (profilePicture != null) {
                        File file = FileType.PROFILE.getFilePath(context, currentUser);
                        if (file.exists()) {
                            file.delete();
                        }
                        new Thread(() -> {
                            User user = response.body().getUser();
                            userDao.updatePictureUrl(user.getProfilePicture(), user.getId());
                            User updatedUser = userDao.getUserWithToken();
                            result.postValue(Resource.success(updatedUser));
                        }).start();
                    }
                } else {
                    result.setValue(Resource.error("Failed to update profile"));
                }
            }

            @Override
            public void onFailure(Call<UserResult> call, Throwable t) {
                result.setValue(Resource.error("Network error"));
            }
        });

        return result;
    }
}

