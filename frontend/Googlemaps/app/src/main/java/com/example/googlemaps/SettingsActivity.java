package com.example.googlemaps;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {
  FirebaseAuth firebaseAuth;
  Button logOutButton;
  Button resetPass;

  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    firebaseAuth = FirebaseAuth.getInstance();

    logOutButton = findViewById(R.id.log_out);
    logOutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        firebaseAuth.signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
      }
    });
    resetPass = findViewById(R.id.reset_pass);
    final EditText resetPassText = new EditText(SettingsActivity.this);
    resetPass.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder resetPassDialog = new AlertDialog.Builder(SettingsActivity.this);
        resetPassDialog.setTitle("Reset Password?");
        resetPassDialog.setMessage("Enter new password (min. 6 characters)");
        resetPassDialog.setView(resetPassText);
        resetPassDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          // Updates password if yes is clicked
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String newPassword = resetPassText.getText().toString();
            FirebaseUser user = firebaseAuth.getCurrentUser();
            user.updatePassword(newPassword);
          }
        });
        resetPassDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // Do nothing if no is clicked
          }
        });
        resetPassDialog.create().show();
      }
    });
  }
}