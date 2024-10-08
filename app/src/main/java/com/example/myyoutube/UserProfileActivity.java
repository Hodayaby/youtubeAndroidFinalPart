package com.example.myyoutube;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class UserProfileActivity extends AppCompatActivity implements PostsListAdapter.PostsAdapterListener {

    private PostsListAdapter postsListAdapter;
    private RecyclerView recyclerView;
    private TextView userFullName;
    private TextView userNameTextView;

    private VideoViewModel videoViewModel;
    private UserViewModel userViewModel;

    private Button editUserButton;
    private Button deleteUserButton;
    private ImageView userProfileImage;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Set theme based on ThemeManager
        setDarkMode(ThemeManager.isDarkMode());

        // Set the content view and log the activity start
        setContentView(R.layout.activity_userprofile);

        recyclerView = findViewById(R.id.userVideosRecyclerView);
        userFullName = findViewById(R.id.userFullName);
        userNameTextView = findViewById(R.id.userNameTextView);
        editUserButton = findViewById(R.id.editUserButton);
        deleteUserButton = findViewById(R.id.deleteUserButton);
        userProfileImage = findViewById(R.id.userProfileImage);

        Intent intent = getIntent();
        int userId = intent.getIntExtra("userId", -1);
        String userName = intent.getStringExtra("userName");

        userFullName.setText(userName);
        userNameTextView.setText(userName);

        postsListAdapter = new PostsListAdapter(this, this, videoViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postsListAdapter);

        userViewModel.getCurrentUser().observe(this, userResource -> {
            if (userResource.isSuccess()) {
                currentUser = userResource.getData();
            } else {
                Toast.makeText(UserProfileActivity.this, userResource.getError(), Toast.LENGTH_SHORT).show();
            }

            boolean shouldLoadPhoto = true;
            if (currentUser != null) {
                if (currentUser.getId() == userId) {
                    editUserButton.setVisibility(View.VISIBLE);
                    deleteUserButton.setVisibility(View.VISIBLE);
                    updatePhoto(currentUser);
                    shouldLoadPhoto = false;
                }
            }

            if (shouldLoadPhoto) {
                userViewModel.getUserById(userId).observe(this, getUserResource -> {
                    if (getUserResource.isSuccess()) {
                        User user = getUserResource.getData();
                        updatePhoto(user);
                    } else {
                        Toast.makeText(UserProfileActivity.this, getUserResource.getError(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        videoViewModel.getUserVideos(userId).observe(this, resource -> {
            if (resource.isSuccess()) {
                postsListAdapter.setPosts(resource.getData());
            } else {
                Toast.makeText(UserProfileActivity.this, resource.getError(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Intent homeIntent = new Intent(UserProfileActivity.this, HomeScreenActivity.class);
            startActivity(homeIntent);
            finish();
        });

        deleteUserButton.setOnClickListener(v -> {
            userViewModel.deleteUser(currentUser).observe(this, resource -> {
                if (resource.isSuccess()) {
                    Toast.makeText(UserProfileActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                    // Navigate to login screen
                    Intent homeIntent = new Intent(UserProfileActivity.this, LoginActivity.class);
                    startActivity(homeIntent);
                    finish(); // Finish the current activity
                } else {
                    Toast.makeText(UserProfileActivity.this, resource.getError(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        editUserButton.setOnClickListener(v -> showEditProfileDialog());
    }

    private Bitmap profileBitmap;

    private void updatePhoto(User user) {
        videoViewModel.downloadFile(user).observe(this, resource -> {
            if (resource.isSuccess() && resource.getData() != null && resource.getData() == true) {
                File file = FileType.PROFILE.getFilePath(UserProfileActivity.this, user);
                Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bmp != null) {
                    userProfileImage.setImageBitmap(bmp);
                    profileBitmap = bmp;
                    return;
                }
            }
            userProfileImage.setImageResource(R.drawable.ic_profile);
        });
    }

    // Set dark mode based on the current theme
    private void setDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    View dialogView;

    private void showEditProfileDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        dialogView = inflater.inflate(R.layout.dialog_editprofile, null);

        EditText editPassword = dialogView.findViewById(R.id.editPassword);
        EditText editConfirmPassword = dialogView.findViewById(R.id.editConfirmPassword);
        ImageView profileImagePreview = dialogView.findViewById(R.id.profileImagePreview);
        Button changeProfileImageButton = dialogView.findViewById(R.id.changeProfileImageButton);
        if (profileBitmap != null) {
            profileImagePreview.setImageBitmap(profileBitmap);
        } else {
            profileImagePreview.setImageResource(R.drawable.ic_profile);
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView)
                .setTitle("Edit Profile")
                .setPositiveButton("Save", null) // to prevent save from dismissing the dialog
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    dialogView = null;
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            String password = editPassword.getText().toString();
            String confirmPassword = editConfirmPassword.getText().toString();

            boolean isValid = true;
            // Validate password
            if (password.isEmpty() || password.length() < 8 || !isPasswordValid(password)) {
                isValid = false;
                setErrorOnEditText(editPassword, "Password must be at least 8 characters long and include letters and numbers");
            } else {
                clearErrorOnEditText(editPassword);
            }
            if (!password.equals(confirmPassword)) {
                isValid = false;
                setErrorOnEditText(editConfirmPassword, "Passwords do not match");
            } else {
                clearErrorOnEditText(editConfirmPassword);
            }

            if (isValid) {
                userViewModel.updateUserProfile(currentUser, selectedBitmap, password).observe(UserProfileActivity.this, resource -> {
                    if (resource.isSuccess()) {
                        Toast.makeText(UserProfileActivity.this, "User updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        dialogView = null;
                        currentUser = resource.getData();
                        updatePhoto(currentUser);
                    } else {
                        Toast.makeText(UserProfileActivity.this, resource.getError(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Handle change profile image button click
        changeProfileImageButton.setOnClickListener(v -> {
            showImageUploadOptions();
        });
    }

    // Method to set error on EditText
    private void setErrorOnEditText(EditText editText, String errorMessage) {
        editText.setError(errorMessage);
    }

    // Method to clear error on EditText
    private void clearErrorOnEditText(EditText editText) {
        editText.setError(null);
    }

    private boolean isPasswordValid(String password) {
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasLetter && hasDigit;
    }

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private void showImageUploadOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Image")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Take photo option selected
                            if (ContextCompat.checkSelfPermission(UserProfileActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
                            } else {
                                ActivityCompat.requestPermissions(UserProfileActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                            }
                        } else {
                            // Choose from gallery option selected
                            Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhotoIntent, PICK_IMAGE_REQUEST);
                        }
                    }
                })
                .show();
    }

    private Bitmap selectedBitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri imageUri = data.getData();
                try {
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    if (dialogView != null) {
                        ImageView profileImagePreview = dialogView.findViewById(R.id.profileImagePreview);
                        profileImagePreview.setImageBitmap(selectedBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST && data != null) {
                selectedBitmap = (Bitmap) data.getExtras().get("data");
                if (dialogView != null) {
                    ImageView profileImagePreview = dialogView.findViewById(R.id.profileImagePreview);
                    profileImagePreview.setImageBitmap(selectedBitmap);
                }
            }
        }
    }

    @Override
    public void onPostsFiltered(int count) {

    }
}
