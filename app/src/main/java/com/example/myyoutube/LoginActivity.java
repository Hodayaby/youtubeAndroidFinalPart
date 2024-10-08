package com.example.myyoutube;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView loginErrorTextView;
    private TextView usernameErrorTextView;
    private TextView passwordErrorTextView;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Set theme based on ThemeManager
        setDarkMode(ThemeManager.isDarkMode());

        setContentView(R.layout.activity_login);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginErrorTextView = findViewById(R.id.login_error);
        usernameErrorTextView = findViewById(R.id.username_error);
        passwordErrorTextView = findViewById(R.id.password_error);

        // Login button click listener
        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Validate username and password
            if (validateLogin(username, password)) {
                userViewModel.login(username, password).observe(LoginActivity.this, new Observer<Resource<LoginResponse>>() {
                    @Override
                    public void onChanged(Resource<LoginResponse> resource) {
                        if (resource.isSuccess() && resource.getData() != null) {
                            Toast.makeText(LoginActivity.this, "Login succeeded", Toast.LENGTH_SHORT).show();
                            // Proceed to HomeScreenActivity
                            Intent intent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                            startActivity(intent);
                            finish(); // Finish LoginActivity to prevent going back to it
                        } else {
                            Toast.makeText(LoginActivity.this, resource.getError(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Register link click listener
        Button registerButton = findViewById(R.id.registerMoveBtn);
        registerButton.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        });
    }

    // Validate login credentials
    private boolean validateLogin(String username, String password) {
        boolean isValid = true;

        // Validate username
        if (TextUtils.isEmpty(username)) {
            usernameErrorTextView.setText("Username is required");
            isValid = false;
        } else {
            usernameErrorTextView.setText("");
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordErrorTextView.setText("Password is required");
            isValid = false;
        } else if (password.length() < 8) {
            passwordErrorTextView.setText("Password must be at least 8 characters long");
            isValid = false;
        } else {
            passwordErrorTextView.setText("");
        }

        return isValid;
    }

    private void setDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
