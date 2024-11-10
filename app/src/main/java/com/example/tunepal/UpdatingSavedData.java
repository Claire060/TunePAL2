package com.example.tunepal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UpdatingSavedData extends AppCompatActivity {

    EditText editName, editPhone, editEmail, editPassword;
    Button submitButton;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updating_saved_data);

        editName = findViewById(R.id.updatingUserFullName);
        editPhone = findViewById(R.id.updatingUserPhoneNumber);
        editEmail = findViewById(R.id.updatingUserEmail);
        editPassword = findViewById(R.id.updatingUserPassword);
        submitButton = findViewById(R.id.SubmitButtonTestingDatabase);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // Prefill fields with current data (if any)
        editName.setText(sharedPreferences.getString("name", ""));
        editPhone.setText(sharedPreferences.getString("phone", ""));
        editEmail.setText(sharedPreferences.getString("email", ""));

        // Submit button to save new profile data
        submitButton.setOnClickListener(v -> {
            String updatedName = editName.getText().toString();
            String updatedPhone = editPhone.getText().toString();
            String updatedEmail = editEmail.getText().toString();
            String updatedPassword = editPassword.getText().toString();

            // Save new data to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("name", updatedName);
            editor.putString("phone", updatedPhone);
            editor.putString("email", updatedEmail);
            editor.putString("password", updatedPassword);
            editor.apply();

            // Show message or return to ProfileActivity
            setResult(RESULT_OK);
            finish();
        });

    }
}
