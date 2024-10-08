package com.example.myyoutube;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(int userId);

    @Query("SELECT * FROM users WHERE token is not null LIMIT 1")
    User getUserWithToken();

    @Query("DELETE FROM users WHERE token is not null")
    void deleteUserWithToken();

    @Query("UPDATE users SET profilePicture = :pictureUrl WHERE id = :id")
    void updatePictureUrl(String pictureUrl, int id);
}
