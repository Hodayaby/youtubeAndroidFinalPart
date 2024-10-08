package com.example.myyoutube;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertComments(List<Comment> videos);

    @Query("SELECT * FROM comments WHERE videoId = :videoId")
    List<Comment> getCommentsByVideoId(int videoId);

    @Query("DELETE FROM videos WHERE id = :id")
    void deleteById(int id);
}
