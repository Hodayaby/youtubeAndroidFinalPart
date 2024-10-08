package com.example.myyoutube;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Video.class, Comment.class, User.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract VideoDao videoDao();
    public abstract CommentDao commentDao();
    public abstract UserDao userDao();
}

