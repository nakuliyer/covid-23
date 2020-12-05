package com.example.googlemaps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

  private static FirebaseAuth firebaseAuth;
  private static Context context;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_activity);
    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.settings, new SettingsFragment())
          .commit();
    }
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    context = this;
    firebaseAuth = FirebaseAuth.getInstance();
  }

  // Going back to the MapsActivity
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
  public static class AlarmFragment extends androidx.fragment.app.DialogFragment {
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle("EMERGENCY BUTTON");
      builder.setMessage("This is only meant to be pressed in the case that you have COVID-19. Are you sure about this?");
      builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          // Implement api stuff here
        }
      });
      builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          //Do nothing
        }
      });
      return builder.create();
    }
  }
  public static class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.root_preferences, rootKey);

      // Get logout button from settings
      Preference logout = findPreference("logoutButton");
      logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        // When logout button is clicked, go to the login page
        public boolean onPreferenceClick(Preference preference) {
          firebaseAuth.signOut();
          context.startActivity(new Intent(context, LoginActivity.class));
          return true;
        }
      });

      // Get reset password button from settings
      Preference resetPass = findPreference("resetPass");
      resetPass.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        EditText resetPasswordText = new EditText(getContext());
        @Override
        public boolean onPreferenceClick(Preference preference) {
          AlertDialog.Builder resetPassDialog = new AlertDialog.Builder(getContext());
          resetPassDialog.setTitle("Reset Password?");
          resetPassDialog.setMessage("Enter new password (min. 6 characters)");
          resetPassDialog.setView(resetPasswordText);
          resetPassDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              String newPassword = resetPasswordText.getText().toString();
              FirebaseUser user = firebaseAuth.getCurrentUser();
              user.updatePassword(newPassword);
            }
          });
          resetPassDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // Do nothing
            }
          });
          resetPassDialog.create().show();
          return true;
        }
      });

      Preference alarm = findPreference("alarmButton");
      alarm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          DialogFragment alarmMessage = new AlarmFragment();
          alarmMessage.show(getParentFragmentManager(), "Settings");
          return true;
        }
      });
    }
  }
}