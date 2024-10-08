package com.example.myyoutube;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.io.File;
import java.util.List;

public class VideoViewModel extends AndroidViewModel {
    private VideoRepository videoRepository;

    public VideoViewModel(Application app) {
        super(app);
        videoRepository = new VideoRepository(app);
    }

    public LiveData<Resource<List<Video>>> getAllVideos() {
        return videoRepository.getAllVideos();
    }

    public LiveData<Resource<List<Video>>> getRecommendedVideos(String userId, String videoId) {
        return videoRepository.getRecommendedVideos(userId, videoId);
    }

    public LiveData<Resource<Boolean>> downloadFile(Video video, FileType fileType) {
        return videoRepository.downloadFile(video, fileType);
    }

    public LiveData<Resource<Boolean>> downloadFile(User user) {
        return videoRepository.downloadFile(user);
    }

    public LiveData<Resource<Video>> getVideoById(User currentUser, String videoId) {
        return videoRepository.getVideoById(currentUser, videoId);
    }

    public LiveData<Resource<Video>> uploadVideo(User currentUser, File videoFile, File thumbnail, String title, String description) {
        return videoRepository.uploadVideo(currentUser, videoFile, thumbnail, title, description);
    }

    public LiveData<Resource<Video>> likeDislikeVideo(User currentUser, Video video) {
        return videoRepository.likeDislikeVideo(currentUser, video);
    }

    public LiveData<Resource<Boolean>> deleteVideo(User currentUser, Video video) {
        return videoRepository.deleteVideo(currentUser, video);
    }

    public LiveData<Resource<Boolean>> editVideo(
            User currentUser,
            Video video,
            File videoFile,
            File thumbnail) {
        return videoRepository.editVideo(currentUser, video, videoFile, thumbnail);
    }

    public LiveData<Resource<Boolean>> addComment(User currentUser, Comment comment) {
        return videoRepository.addComment(currentUser, comment);
    }

    public LiveData<Resource<Boolean>> editComment(User currentUser, Video video, Comment comment) {
        return videoRepository.editComment(currentUser, video, comment);
    }

    public LiveData<Resource<Boolean>> deleteComment(User currentUser, Video video, Comment comment) {
        return videoRepository.deleteComment(currentUser, video, comment);
    }

    public LiveData<Resource<List<Video>>> getUserVideos(int userId) {
        return videoRepository.getUserVideos(userId);
    }
}
