package com.example.tunepal;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    ImageView backIconn;
    TextView retrieveName, retrievePhone, retrieveEmail, removeDataText;
    Button updateYourProfile, logOutButton;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        backIconn = findViewById(R.id.profileBackIcon);
        retrieveName = findViewById(R.id.profileUserFullName);
        retrievePhone = findViewById(R.id.profileUserPhoneNumber);
        retrieveEmail = findViewById(R.id.profileUserEmail);
        updateYourProfile = findViewById(R.id.updateYourProfileButton);
        TextView removeDataText = findViewById(R.id.removingDataUpdateScreen);
        Button logOutButton = findViewById(R.id.buttonLogOut);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Display user data
        displayUserData();

        // Back button to close the activity
        backIconn.setOnClickListener(v -> finish());

        // Update profile button to open the update activity
        updateYourProfile.setOnClickListener(v -> startActivityForResult(
                new Intent(ProfileActivity.this, UpdatingSavedData.class), 1));

        logOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();  // Sign out from Firebase
            // Redirect to the main page or login screen
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Optionally finish the ProfileActivity so the user can't return to it via back press
        });

        removeDataText.setOnClickListener(v -> {
            retrieveName.setText("");
            retrievePhone.setText("");
            retrieveEmail.setText("");
        });

    }

    private void displayUserData() {
        // Retrieve stored user details from SharedPreferences
        String name = sharedPreferences.getString("name", "N/A");
        String phone = sharedPreferences.getString("phone", "N/A");
        String email = sharedPreferences.getString("email", "N/A");

        // Display the retrieved data in the UI
        retrieveName.setText(name);
        retrievePhone.setText(phone);
        retrieveEmail.setText(email);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // After returning from UpdatingSavedData, refresh the profile data
            displayUserData();
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        }
    }
}
