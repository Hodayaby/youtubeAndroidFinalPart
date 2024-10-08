package com.example.myyoutube;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VideoViewActivity extends AppCompatActivity implements PostsListAdapter.PostsAdapterListener {

    private static final int PICK_IMAGE_REQUEST = 2;

    private TextView videoTitleTextView;
    private TextView videoAuthorTextView;
    private TextView videoDescTextView;
    private TextView videoViewsTextView;
    private ImageView videoChannelImageView;
    private VideoView videoView;
    private EditText commentEditText;
    private ImageButton likeButton;
//    private ImageButton dislikeButton;
    private Button addCommentButton;
    private LinearLayout commentsContainer;
    private ImageButton shareButton;
    private ImageButton editVideoButton;
    private ImageButton deleteVideoButton;

    private Bitmap imageBitmap;
    private ImageView previewImageView;
    private ImageView uploadImageSuccess;

    private Video currentPost;
    private User currentUser;

    private VideoViewModel videoViewModel;
    private UserViewModel userViewModel;

    public PostsListAdapter postsListAdapter;
    public RecyclerView recyclerView;
    private boolean recommendedLoadStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Get data from intent
        String videoId = getIntent().getStringExtra("videoId");

        userViewModel.getCurrentUser().observe(this, userResource -> {
            if (userResource.isSuccess()) {
                currentUser = userResource.getData();
            } else {
                Toast.makeText(VideoViewActivity.this, userResource.getError(), Toast.LENGTH_SHORT).show();
            }

            // If user is not logged in, hide the comment box
            if (currentUser == null || currentUser.getUsername() == null) {
                commentEditText.setVisibility(View.GONE);
                addCommentButton.setVisibility(View.GONE);
            } else {
                commentEditText.setVisibility(View.VISIBLE);
                addCommentButton.setVisibility(View.VISIBLE);
            }

            videoViewModel.getVideoById(currentUser, videoId).observe(this, new Observer<Resource<Video>>() {
                @Override
                public void onChanged(Resource<Video> resource) {
                    if (!resource.isSuccess()) {
                        Toast.makeText(VideoViewActivity.this, resource.getError(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Video video = resource.getData();
                    currentPost = video;

                    // Set data to views
                    videoTitleTextView.setText(video.getTitle());
                    videoAuthorTextView.setText(video.getUploadedBy());
                    videoDescTextView.setText(video.getDescription());
                    videoViewsTextView.setText(video.getViews() + " • 7 months ago");
//                videoChannelImageView.setImageResource(videoChannelImage);

                    videoViewModel.downloadFile(video, FileType.VIDEO).observe(VideoViewActivity.this, new Observer<Resource<Boolean>>() {
                        @Override
                        public void onChanged(Resource<Boolean> fileResource) {
                            if (fileResource.isSuccess() && fileResource.getData() != null && fileResource.getData() == true) {
                                // Set up the VideoView
                                File videoFile = FileType.VIDEO.getFilePath(VideoViewActivity.this, video);
                                videoView.setVideoPath(videoFile.getPath());
//                            videoView.setVideoURI(Uri.parse(videoUri));
                                MediaController mediaController = new MediaController(VideoViewActivity.this);
                                videoView.setMediaController(mediaController);
                                mediaController.setAnchorView(videoView);
                                videoView.start();
                            } else {
                                String error = resource.getError();
                                Toast.makeText(VideoViewActivity.this, error != null ? error : "Failed to download video", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    // Check if the post is liked or disliked and update button colors
                    updateLikeDislikeButtons();

                    // Load comments
                    loadComments();

                    loadRecommendedVideos();
                }
            });

        });

        // Set theme based on ThemeManager
        setDarkMode(ThemeManager.isDarkMode());

        setContentView(R.layout.activity_video_view);

        // Initialize views
        videoTitleTextView = findViewById(R.id.videoTitle);
        videoAuthorTextView = findViewById(R.id.videoArtist);
        videoViewsTextView = findViewById(R.id.videoDetails);
        videoDescTextView = findViewById(R.id.videoDesc);
        videoChannelImageView = findViewById(R.id.videoChannelImage);
        videoView = findViewById(R.id.videoView);
        commentEditText = findViewById(R.id.commentBox);
        likeButton = findViewById(R.id.likeButton);
//        dislikeButton = findViewById(R.id.dislikeButton);
        addCommentButton = findViewById(R.id.addCommentButton);
        commentsContainer = findViewById(R.id.commentsContainer);
        shareButton = findViewById(R.id.shareButton);
        editVideoButton = findViewById(R.id.editButton);
        deleteVideoButton = findViewById(R.id.deleteButton);
        previewImageView = findViewById(R.id.previewImage);
        uploadImageSuccess = findViewById(R.id.uploadImageSuccess);
        findViewById(R.id.profileRow).setOnClickListener(v -> {
            if (currentPost == null) return;

            Intent intent = new Intent(VideoViewActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", currentPost.getAuthorId());
            intent.putExtra("userName", currentPost.getUploadedBy());
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.homeVideosRecyclerView);
        postsListAdapter = new PostsListAdapter(this, this, videoViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postsListAdapter);

        // Set up like button click listener
        likeButton.setOnClickListener(v -> handleLikeClick());

        // Set up dislike button click listener
//        dislikeButton.setOnClickListener(v -> handleDislikeClick());

        // Set up add comment button click listener
        addCommentButton.setOnClickListener(v -> {
            if (currentUser == null || currentUser.getUsername() == null) {
                Toast.makeText(VideoViewActivity.this, "Please login to add comments", Toast.LENGTH_SHORT).show();
            } else {
                addComment();
            }
        });

        // Set up share button click listener
        shareButton.setOnClickListener(v -> {
            if (currentUser == null || currentUser.getUsername() == null) {
                Toast.makeText(VideoViewActivity.this, "Please login to share videos", Toast.LENGTH_SHORT).show();
            } else {
                shareVideo();
            }
        });

        // Set up edit video button click listener
        editVideoButton.setOnClickListener(v -> {
            if (currentUser == null || !currentPost.getUploadedBy().equals(currentUser.getUsername())) {
                Toast.makeText(VideoViewActivity.this, "You cannot edit this video", Toast.LENGTH_SHORT).show();
            } else {
                showEditVideoDialog();
            }
        });

        // Set up delete video button click listener
        deleteVideoButton.setOnClickListener(v -> {
            if (currentUser == null || !currentPost.getUploadedBy().equals(currentUser.getUsername())) {
                Toast.makeText(VideoViewActivity.this, "You cannot delete this video", Toast.LENGTH_SHORT).show();
            } else {
                deleteVideo();
            }
        });
    }

    private void setDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void updateLikeDislikeButtons() {
        if (currentUser != null) {
            if (currentPost.getLikes().contains(currentUser.getUsername())) {
                likeButton.setColorFilter(getResources().getColor(R.color.blue));
            } else {
                likeButton.clearColorFilter();
            }

//            if (currentUser.isDisliked(currentPost)) {
//                dislikeButton.setColorFilter(getResources().getColor(R.color.blue));
//            } else {
//                dislikeButton.clearColorFilter();
//            }
        }
    }

    private void handleLikeClick() {
        if (currentUser == null || currentUser.getUsername() == null) {
            Toast.makeText(this, "Please login to like videos", Toast.LENGTH_SHORT).show();
        } else {
            videoViewModel.likeDislikeVideo(currentUser, currentPost).observe(this, resource -> {
                if (resource.isSuccess()) {
                    currentPost = resource.getData();
                    updateLikeDislikeButtons();
                } else {
                    Toast.makeText(VideoViewActivity.this, "Failed to update like/dislike", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

//    private void handleDislikeClick() {
//        if (currentUser == null || currentUser.getUsername() == null) {
//            Toast.makeText(this, "Please login to dislike videos", Toast.LENGTH_SHORT).show();
//        } else {
//            if (currentUser.isDisliked(currentPost)) {
//                currentUser.removeDislikedPost(currentPost);
//            } else {
//                if (currentUser.isLiked(currentPost)) {
//                    currentUser.removeLikedPost(currentPost);
//                }
//                currentUser.addDislikedPost(currentPost);
//            }
//        }
//        updateLikeDislikeButtons();
//    }

    // Load comments and update the view
    private void loadComments() {
        commentsContainer.removeAllViews();
        List<Comment> comments = currentPost.getComments();
        for (Comment comment : comments) {
            addCommentView(comment);
        }
    }

    // Add comment to the post and update the view
    private void addComment() {
        String commentText = commentEditText.getText().toString();
        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

//        String profileImageBase64 = null;
//        if (currentUser.getProfileImage() != null) {
//            profileImageBase64 = userListManager.encodeBitmapToBase64(currentUser.getProfileImage());
//        }

        Comment newComment = new Comment();
        newComment.setText(commentText);
        newComment.setUser(currentUser.getUsername());
        newComment.setVideoId(currentPost.getId());

        videoViewModel.addComment(currentUser, newComment).observe(this, resource -> {
            if (resource.isSuccess()) {
                Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
                ArrayList<Comment> comments = new ArrayList<>(currentPost.getComments());
                comments.add(newComment);
                currentPost.setComments(comments);
                loadComments(); // Reload comments to display the new comment
                commentEditText.setText(""); // Clear the comment box
            } else {
                Toast.makeText(this, "Failed to add the comment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Add a comment view with edit and delete buttons if applicable
    private void addCommentView(Comment comment) {
        View commentView = LayoutInflater.from(this).inflate(R.layout.comment_layout, commentsContainer, false);

        TextView usernameTextView = commentView.findViewById(R.id.commentUsername);
        TextView commentTextView = commentView.findViewById(R.id.commentContent);
        TextView timestampTextView = commentView.findViewById(R.id.commentTimestamp);
        ImageView userImageView = commentView.findViewById(R.id.commentUserImage);
        ImageButton editButton = commentView.findViewById(R.id.editCommentButton);
        ImageButton deleteButton = commentView.findViewById(R.id.deleteCommentButton);
        LinearLayout editDeleteContainer = commentView.findViewById(R.id.editDeleteContainer);

        usernameTextView.setText(comment.getUser());
        commentTextView.setText(comment.getText());
        timestampTextView.setText("yyyy-MM-dd HH:mm:ss");

        // Set the user image
//        if (comment.getProfileImageBase64() != null) {
//            Bitmap userImageBitmap = userListManager.decodeBase64ToBitmap(comment.getProfileImageBase64());
//            userImageView.setImageBitmap(userImageBitmap);
//        } else {
            userImageView.setImageResource(R.drawable.ic_profile); // Default image
//        }

        if (currentUser != null && comment.getUser().equals(currentUser.getUsername())) {
            editDeleteContainer.setVisibility(View.VISIBLE); // Show the container
            editButton.setOnClickListener(v -> editComment(comment, commentTextView));
            deleteButton.setOnClickListener(v -> deleteComment(comment, commentView));
        } else {
            editDeleteContainer.setVisibility(View.GONE); // Hide the container
        }

        commentsContainer.addView(commentView);
    }

    private void loadRecommendedVideos() {
        if (currentPost == null) {
            return;
        }
        if (recommendedLoadStarted) {
            return;
        }
        recommendedLoadStarted = true;

        String userId = currentUser != null ? currentUser.get_id() : null;

        videoViewModel.getRecommendedVideos(userId, currentPost.get_id()).observe(this, new Observer<Resource<List<Video>>>() {
            @Override
            public void onChanged(Resource<List<Video>> resource) {
                if (resource.isSuccess()) {
                    List<Video> videos = resource.getData();
                    postsListAdapter.setPosts(videos);
                } else {
                    String errorMessage = resource.getError();
                    Toast.makeText(VideoViewActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Edit comment and update the view
    private void editComment(Comment comment, TextView commentTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Comment");

        final EditText input = new EditText(this);
        input.setText(comment.getText());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String editedComment = input.getText().toString();
            if (!TextUtils.isEmpty(editedComment)) {
                comment.setText(editedComment);
//                comment.setTimestamp(new Comment(currentUser.getUsername(), editedComment).getTimestamp()); // Update timestamp
                videoViewModel.editComment(currentUser, currentPost, comment).observe(VideoViewActivity.this, resource -> {
                    if (resource.isSuccess()) {
                        Toast.makeText(VideoViewActivity.this, "Comment updated", Toast.LENGTH_SHORT).show();
                        commentTextView.setText(editedComment);
                    } else {
                        Toast.makeText(VideoViewActivity.this, "Failed to update the comment", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(VideoViewActivity.this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Delete comment and remove the view
    private void deleteComment(Comment comment, View commentView) {
        videoViewModel.deleteComment(currentUser, currentPost, comment).observe(this, resource -> {
              if (resource.isSuccess()) {
                  ArrayList<Comment> comments = new ArrayList<>(currentPost.getComments());
                  comments.remove(comment);
                  currentPost.setComments(comments);
                  commentsContainer.removeView(commentView);
                  Toast.makeText(VideoViewActivity.this, "Comment removed", Toast.LENGTH_SHORT).show();
              } else {
                  Toast.makeText(VideoViewActivity.this, "Failed to remove the comment", Toast.LENGTH_SHORT).show();
              }
        });
    }

    // Share video using Android Sharesheet
    private void shareVideo() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this video: " + currentPost.getUrl());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    // Show custom dialog to edit video details
    private void showEditVideoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Video");

        View view = getLayoutInflater().inflate(R.layout.edit_video_dialog, null);
        EditText videoTitleInput = view.findViewById(R.id.editVideoTitle);
        EditText videoDescInput = view.findViewById(R.id.editVideoDesc);

        // Set the current video title and description in the input fields
        videoTitleInput.setText(currentPost.getTitle());
        videoDescInput.setText(currentPost.getDescription());

        builder.setView(view);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String editedTitle = videoTitleInput.getText().toString();
            String editedDesc = videoDescInput.getText().toString();

            if (!TextUtils.isEmpty(editedTitle) && !TextUtils.isEmpty(editedDesc)) {
                currentPost.setTitle(editedTitle);
                currentPost.setDescription(editedDesc);
                videoTitleTextView.setText(editedTitle);
                videoDescTextView.setText(editedDesc);
                videoViewModel.editVideo(currentUser, currentPost, null, null).observe(VideoViewActivity.this, resource -> {
                    if (resource.isSuccess()) {
                        Toast.makeText(VideoViewActivity.this, "Video details updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VideoViewActivity.this, "Failed to update video", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(VideoViewActivity.this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Delete video
    private void deleteVideo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Video");

        builder.setMessage("Are you sure you want to delete this video?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            videoViewModel.deleteVideo(currentUser, currentPost).observe(VideoViewActivity.this, resource -> {
                if (resource.isSuccess()) {
                    Toast.makeText(VideoViewActivity.this, "Video deleted successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to home screen
                    Intent intent = new Intent(this, HomeScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(VideoViewActivity.this, "Failed to delete video", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    // Open image picker to change the thumbnail
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // Handle the result of the image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null) {
            Uri imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                previewImageView.setImageBitmap(imageBitmap);
                uploadImageSuccess.setVisibility(View.VISIBLE); // Show success indicator
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPostsFiltered(int count) {

    }
}
