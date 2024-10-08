package com.example.myyoutube;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVideos(List<Video> videos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Video video);

    @Query("SELECT * FROM videos")
    List<Video> getAllVideos();

    @Query("SELECT * FROM videos WHERE id = :videoId")
    Video getVideoById(int videoId);

    @Query("SELECT * FROM videos WHERE _id = :id")
    Video getVideoBy_Id(String id);

    @Query("DELETE FROM videos WHERE id = :id")
    void deleteById(int id);
}
