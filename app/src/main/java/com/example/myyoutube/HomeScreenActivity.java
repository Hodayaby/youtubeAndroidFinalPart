package com.example.myyoutube;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.List;

public class HomeScreenActivity extends AppCompatActivity implements PostsListAdapter.PostsAdapterListener {

    // Declare UI elements and adapters
    public static PostsListAdapter postsListAdapter;
    public static RecyclerView recyclerView;
    private EditText searchBar;
    private ImageView profileButton;
    private ImageView profileImage;
    private TextView noResultsText;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private TextView usernameTextView;
    private TextView displayNameTextView;
    private ImageView profileImageView;

    private User currentUser;
    private boolean showingFavoriteVideos = false;

    private VideoViewModel videoViewModel;
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Set theme based on ThemeManager
        setDarkMode(ThemeManager.isDarkMode());

        // Set the content view and log the activity start
        setContentView(R.layout.activity_homescreen);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);
        profileButton = findViewById(R.id.btnAccount);
        profileImage = findViewById(R.id.profileImage);
        noResultsText = findViewById(R.id.noResultsText);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Initialize navigation header views
        View headerView = navigationView.getHeaderView(0);
        usernameTextView = headerView.findViewById(R.id.nav_username);
        displayNameTextView = headerView.findViewById(R.id.nav_greeting);
        profileImageView = headerView.findViewById(R.id.nav_profile_image);

        // Set up drawer toggle for navigation
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set up menu button click listener to open the navigation drawer
        findViewById(R.id.menuButton).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set up floating action button to open UploadVideoActivity
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            // Check if a user is logged in
            if (currentUser != null) {
                // If user is logged in, open UploadVideoActivity
                Intent intent = new Intent(HomeScreenActivity.this, UploadVideoActivity.class);
                startActivity(intent);
            } else {
                // If no user is logged in, show a message
                Toast.makeText(HomeScreenActivity.this, "Please login to add videos", Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getCurrentUser().observe(this, new Observer<Resource<User>>() {
            @Override
            public void onChanged(Resource<User> userResource) {
                if (userResource.isSuccess()) {
                    User user = userResource.getData();
                    if (user != null) {
                        currentUser = user;
                    }
                    // Load current user profile image and details
                    updateProfileButtonImage();
                    updateUserDetails();
                } else {
                    Toast.makeText(HomeScreenActivity.this, "Failed to load current user", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize adapter and RecyclerView
        postsListAdapter = new PostsListAdapter(this, this, videoViewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postsListAdapter);

        // Set up search bar text change listener to filter posts based on search input
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the posts based on search input
                if (count > 0 || (count == 0 && before > 0)) {
                    postsListAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        // Set up home button click listener to scroll to the top of the list and reset the search
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            searchBar.setText(""); // Clear the search bar
            recyclerView.scrollToPosition(0); // Scroll to the top

            // If showing favorite videos, reload all videos
            if (showingFavoriteVideos) {
                postsListAdapter.resetPosts();
                showingFavoriteVideos = false;
            }
        });

        // Set up profile button click listener (future implementation)
        profileButton.setOnClickListener(v -> {
            // Handle profile button click
        });

        // Set up login button click listener
        findViewById(R.id.btnAccount).setOnClickListener(v -> {
            searchBar.setText(""); // מאפס את תיבת החיפוש לפני מעבר למסך ההתחברות
            Intent intent = new Intent(HomeScreenActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Set up navigation item click listener
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favorite_videos) {
                showFavoriteVideos();
            } else if (id == R.id.nav_dark_mode) {
                // Handle dark mode toggle
                boolean isDarkMode = !ThemeManager.isDarkMode();
                ThemeManager.setDarkMode(isDarkMode);
                setDarkMode(isDarkMode);
                updateMenuTitle(navigationView.getMenu().findItem(R.id.nav_dark_mode), isDarkMode); // Update the menu item
                recreate(); // Recreate the activity to apply the new theme
            } else if (id == R.id.nav_logout) {
                // Handle logout action
                handleLogout();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Update the dark mode menu item title initially
        updateMenuTitle(navigationView.getMenu().findItem(R.id.nav_dark_mode), ThemeManager.isDarkMode());

        profileImage.setOnClickListener(v -> {
            if (currentUser == null) return;
            Intent intent = new Intent(HomeScreenActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", currentUser.getId());
            intent.putExtra("userName", currentUser.getUsername());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchBar.setText("");

        // Observe the LiveData from the ViewModel
        videoViewModel.getAllVideos().observe(this, new Observer<Resource<List<Video>>>() {
            @Override
            public void onChanged(Resource<List<Video>> resource) {
                if (resource.isSuccess()) {
                    List<Video> videos = resource.getData();
                    postsListAdapter.setPosts(videos);
                } else {
                    String errorMessage = resource.getError();
                    Toast.makeText(HomeScreenActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
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

    // Update the menu title based on dark mode status
    private void updateMenuTitle(MenuItem menuItem, boolean isDarkMode) {
        if (isDarkMode) {
            menuItem.setTitle("Light Mode");
            menuItem.setIcon(R.drawable.ic_sun); // Replace with the sun icon
        } else {
            menuItem.setTitle("Dark Mode");
            menuItem.setIcon(R.drawable.ic_dark); // Replace with the moon icon
        }
    }

    // Update the profile button image based on the current user's status
    private void updateProfileButtonImage() {
        if (currentUser != null && currentUser.getProfilePicture() != null) {
            profileButton.setImageResource(R.drawable.ic_account);
        } else {
            profileButton.setImageResource(R.drawable.ic_noaccount);
        }
    }

    // Update user details in the navigation header
    private void updateUserDetails() {
        if (currentUser != null) {
            videoViewModel.downloadFile(currentUser).observe(HomeScreenActivity.this, resource -> {
                if (resource.isSuccess() && resource.getData() != null && resource.getData() == true) {
                    File file = FileType.PROFILE.getFilePath(HomeScreenActivity.this, currentUser);
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    if (bmp != null) {
                        profileImageView.setImageBitmap(bmp);
                        profileImage.setImageBitmap(bmp);
                        return;
                    }
                }
                profileImageView.setImageResource(R.drawable.ic_account);
                profileImage.setImageResource(R.drawable.ic_profile);
            });

            if (currentUser.getUsername() != null) {
                usernameTextView.setText("username: " + currentUser.getUsername());
                displayNameTextView.setText("Welcome " + currentUser.getUsername());
            }
        } else {
            usernameTextView.setText("No user logged in");
            displayNameTextView.setText("Welcome");
            profileImageView.setImageResource(R.drawable.ic_account);
            profileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    // Show favorite videos of the current user
    private void showFavoriteVideos() {
        if (currentUser != null && currentUser.getUsername() != null) {
            postsListAdapter.showLikedOnlyBy(currentUser.getUsername());
            showingFavoriteVideos = true; // Update the flag
        } else {
            Toast.makeText(this, "Please login to see your favorite videos", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle user logout action
    private void handleLogout() {
        // Check if a user is logged in
        if (currentUser != null) {
            // Logout the user
            searchBar.setText("");

            userViewModel.clearCurrentUser().observe(this, new Observer<Resource<Boolean>>() {
                @Override
                public void onChanged(Resource<Boolean> booleanResource) {
                    updateUserDetails();
                    updateProfileButtonImage(); // Update the profile button image as well

                    // Navigate to login screen
                    Intent intent = new Intent(HomeScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish(); // Finish the current activity
                }
            });
        } else {
            // Show a message if no user is logged in
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show();
        }
    }

    // Callback when posts are filtered
    @Override
    public void onPostsFiltered(int count) {
        if (count == 0) {
            noResultsText.setVisibility(View.VISIBLE);
        } else {
            noResultsText.setVisibility(View.GONE);
        }
    }
}
