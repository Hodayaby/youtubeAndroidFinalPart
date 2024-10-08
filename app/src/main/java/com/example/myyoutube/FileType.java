package com.example.myyoutube;

import android.content.Context;

import java.io.File;

public enum FileType {
    THUMBNAIL,
    VIDEO,
    PROFILE;

    public String getFileName(Video video) {
        return video.get_id() + "_" + this.name();
    }

    public String getFileName(User user) {
        return user.get_id() + "_" + this.name();
    }

    public File getFilePath(Context context, Video video) {
        return new File(context.getFilesDir(), getFileName(video));
    }

    public File getFilePath(Context context, User user) {
        return new File(context.getFilesDir(), getFileName(user));
    }
}
