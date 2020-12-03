package com.example.googlemaps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
  FirebaseAuth firebaseAuth;
  Button logOutButton;

  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    logOutButton = findViewById(R.id.log_out);

    firebaseAuth = FirebaseAuth.getInstance();

    logOutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        firebaseAuth.signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
      }
    });
  }
}