package com.example.myyoutube;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class UploadVideoActivity extends AppCompatActivity {

    // Request codes for video and image picking, and permissions
    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final int GALLERY_PERMISSION_REQUEST_CODE = 102;
    private static final String TAG = "UploadVideoActivity";

    // UI components
    private EditText videoTitleEditText;
    private EditText videoDescEditText;
    private Button uploadVideoButton;
    private Button uploadImageButton;
    private Button submitUploadButton;
    private ImageView previewImageView;
    private ImageView uploadVideoSuccess;
    private ImageView uploadImageSuccess;

    // Variables for storing selected video URI and image bitmap
    private Uri videoUri;
    private Bitmap imageBitmap;
    private User currentUser;

    private UserViewModel userViewModel;
    private VideoViewModel videoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadvideo);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);

        // Initialize UI components
        videoTitleEditText = findViewById(R.id.uploadVidName);
        videoDescEditText = findViewById(R.id.uploadVidDesc );
        uploadVideoButton = findViewById(R.id.uploadVideoBtn);
        uploadImageButton = findViewById(R.id.uploadImageBtn);
        submitUploadButton = findViewById(R.id.submitUploadBtn);
        previewImageView = findViewById(R.id.previewImage);
        uploadVideoSuccess = findViewById(R.id.uploadVideoSuccess);
        uploadImageSuccess = findViewById(R.id.uploadImageSuccess);

        userViewModel.getCurrentUser().observe(this, resource -> {
            if (resource.isSuccess()) {
                currentUser = resource.getData();
            } else {
                Toast.makeText(UploadVideoActivity.this, "Failed to load current user", Toast.LENGTH_SHORT).show();
            }
            if (currentUser == null) {
                // If no user is logged in, redirect to login
                Log.d(TAG, "No user logged in, redirecting to login");
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        // Set onClick listeners for buttons
        uploadVideoButton.setOnClickListener(v -> showVideoUploadOptions());
        uploadImageButton.setOnClickListener(v -> openImagePicker());
        submitUploadButton.setOnClickListener(v -> handleUpload());

        // Check and request permissions
        if (!checkPermissions()) {
            Log.d(TAG, "Permissions not granted, requesting permissions");
            requestPermissions();
        } else {
            Log.d(TAG, "All permissions already granted");
        }
    }

    // Method to check required permissions
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            boolean readImagesGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean readVideoGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
            boolean cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "READ_MEDIA_IMAGES granted: " + readImagesGranted);
            Log.d(TAG, "READ_MEDIA_VIDEO granted: " + readVideoGranted);
            Log.d(TAG, "CAMERA granted: " + cameraGranted);
            return readImagesGranted && readVideoGranted && cameraGranted;
        } else {
            boolean readExternalGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writeExternalGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "READ_EXTERNAL_STORAGE granted: " + readExternalGranted);
            Log.d(TAG, "WRITE_EXTERNAL_STORAGE granted: " + writeExternalGranted);
            Log.d(TAG, "CAMERA granted: " + cameraGranted);
            return readExternalGranted && writeExternalGranted && cameraGranted;
        }
    }

    // Method to request necessary permissions
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            Log.d(TAG, "Requesting permissions for Android 13+");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.CAMERA},
                    GALLERY_PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "Requesting permissions for Android 12 and below");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    GALLERY_PERMISSION_REQUEST_CODE);
        }
    }

    // Show options to upload a video (either record or choose from gallery)
    private void showVideoUploadOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Video")
                .setItems(new String[]{"Take Video", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Take video option
                            if (ContextCompat.checkSelfPermission(UploadVideoActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                Log.d(TAG, "Camera permission granted, opening camera");
                                Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                startActivityForResult(cameraIntent, PICK_VIDEO_REQUEST);
                            } else {
                                Log.d(TAG, "Camera permission not granted, requesting permission");
                                ActivityCompat.requestPermissions(UploadVideoActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                            }
                        } else {
                            // Choose from gallery option
                            if (Build.VERSION.SDK_INT >= 33) {
                                if (ContextCompat.checkSelfPermission(UploadVideoActivity.this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                                    Log.d(TAG, "READ_MEDIA_VIDEO permission not granted, requesting permission");
                                    ActivityCompat.requestPermissions(UploadVideoActivity.this, new String[]{Manifest.permission.READ_MEDIA_VIDEO}, GALLERY_PERMISSION_REQUEST_CODE);
                                } else {
                                    Log.d(TAG, "READ_MEDIA_VIDEO permission granted, opening gallery");
                                    dispatchPickVideoIntent();
                                }
                            } else {
                                if (ContextCompat.checkSelfPermission(UploadVideoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    Log.d(TAG, "READ_EXTERNAL_STORAGE permission not granted, requesting permission");
                                    ActivityCompat.requestPermissions(UploadVideoActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
                                } else {
                                    Log.d(TAG, "READ_EXTERNAL_STORAGE permission granted, opening gallery");
                                    dispatchPickVideoIntent();
                                }
                            }
                        }
                    }
                })
                .show();
    }

    // Launch intent to pick a video from the gallery
    private void dispatchPickVideoIntent() {
        Log.d(TAG, "Dispatching pick video intent");
        Intent pickVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickVideoIntent, PICK_VIDEO_REQUEST);
    }

    // Open the image picker to choose an image
    private void openImagePicker() {
        Log.d(TAG, "Opening image picker");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // Handle the result from picking a video or image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_VIDEO_REQUEST && data != null) {
                videoUri = data.getData();
                Log.d(TAG, "Selected Video URI: " + videoUri.toString());
                uploadVideoSuccess.setVisibility(View.VISIBLE); // Show success indicator
            } else if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri imageUri = data.getData();
                Bitmap selectedBitmap;
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "Selected Image URI: " + imageUri.toString());
                imageBitmap = selectedBitmap;
                previewImageView.setImageBitmap(selectedBitmap);
                uploadImageSuccess.setVisibility(View.VISIBLE); // Show success indicator
            }
        } else {
            Log.d(TAG, "Result not OK, requestCode: " + requestCode + ", resultCode: " + resultCode);
        }
    }

    // Handle the submission of the upload
    private void handleUpload() {
            String videoTitle = videoTitleEditText.getText().toString().trim();
            String videoDescription = videoDescEditText.getText().toString().trim();

            // Check if the video title is entered
            if (TextUtils.isEmpty(videoTitle)) {
                Toast.makeText(this, "Please enter a video title", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the video description is entered
            if (TextUtils.isEmpty(videoDescription)) {
                Toast.makeText(this, "Please enter a video description", Toast.LENGTH_SHORT).show();
                return;
            }

        // Check if the video is selected
        if (videoUri == null) {
            Toast.makeText(this, "Please select a video", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the image is selected
        if (imageBitmap == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique file name for the video and image
        long timevid = System.currentTimeMillis();
        String vidName = "video_" + timevid + ".mp4";
        String picName = "pic_" + timevid + ".png";

        // Save video file
        File vidFile = vidToFile(videoUri, vidName);

        // Check if video file creation was successful
        if (vidFile == null) {
            Toast.makeText(this, "Failed to save video file.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the file paths
        String videoFilePath = vidFile.getAbsolutePath();
        File imageFile = saveBitmapToFile(UploadVideoActivity.this, imageBitmap, picName);
        if (imageFile == null) {
            Toast.makeText(this, "Failed to save image file.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log the video file path for debugging
        Log.d(TAG, "Video File Path: " + videoFilePath);
        Log.d(TAG, "Uploading video: " + videoTitle);

        videoViewModel.uploadVideo(currentUser, vidFile, imageFile, videoTitle, videoDescription).observe(this, resource -> {
            if (resource.isSuccess() && resource.getData() != null) {
                Video video = resource.getData();
                File videoFile = FileType.VIDEO.getFilePath(UploadVideoActivity.this, video);
                moveFile(vidFile, videoFile);
                File thumbnailFile = FileType.THUMBNAIL.getFilePath(UploadVideoActivity.this, video);
                moveFile(imageFile, thumbnailFile);
                Toast.makeText(UploadVideoActivity.this, "Video uploaded successfully!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Video uploaded successfully: " + videoTitle);

                // Clear the image preview to release memory
                previewImageView.setImageURI(null);
                uploadVideoSuccess.setVisibility(View.GONE);
                uploadImageSuccess.setVisibility(View.GONE);

                // Redirect to home screen
                Intent intent = new Intent(this, HomeScreenActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(UploadVideoActivity.this, resource.getError(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean moveFile(File file1, File file2) {
        boolean success = file1.renameTo(file2);
        if (success) {
            System.out.println("File moved successfully!");
        } else {
            System.out.println("Failed to move file.");
        }
        return success;
    }

    // Method to save bitmap to a file
    public File saveBitmapToFile(Context context, Bitmap bitmap, String fileName) {
        // Get the directory for the app's private pictures directory
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myyoutube");

        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }

        // Create the file where the bitmap will be saved
        File file = new File(directory, fileName + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Compress the bitmap and write it to the file
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Return the URI of the file
        return file;
    }

    // Method to convert video URI to file
    private File vidToFile(Uri videoUri, String vidName) {
        try {
            Log.d(TAG, "Checking permissions before converting video URI to file");
            if (!checkPermissions()) {
                Log.d(TAG, "Permissions not granted, requesting permissions");
                requestPermissions();
                return null;
            }
            Log.d(TAG, "Converting video URI to file");
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(videoUri);
            File videoFile = new File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), vidName);
            OutputStream outputStream = new FileOutputStream(videoFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            Log.d(TAG, "Video file created successfully: " + videoFile.getAbsolutePath());
            return videoFile;
        } catch (Exception e) {
            Log.e(TAG, "Error converting video URI to file", e);
            return null;
        }
    }

    // Handle the result of permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(cameraIntent, PICK_VIDEO_REQUEST);
            } else {
                Log.d(TAG, "Camera permission denied");
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Gallery permission granted");
                dispatchPickVideoIntent();
            } else {
                Log.d(TAG, "Gallery permission denied");
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
