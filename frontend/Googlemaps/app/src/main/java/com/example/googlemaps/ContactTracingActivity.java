package com.example.googlemaps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ContactTracingActivity extends AppCompatActivity {

  final String LOCAL_CODES_KEY = "covid_23_local_codes";
  final String COMPROMISED_URL = "https://covid-23.herokuapp.com/check_compromised";
  final String NEW_CODE = "https://covid-23.herokuapp.com/get_new_code";

  Button covidBtn;
  TextView locationEnabled;
  TextView infentionLikelihood;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_contact_tracing);

    covidBtn = findViewById(R.id.ct_covid_btn);
    locationEnabled = findViewById(R.id.ct_location_status);
    infentionLikelihood = findViewById(R.id.ct_compromised_status);

    covidBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DialogFragment alarmMessage = new AlarmFragment();
        alarmMessage.show(getSupportFragmentManager(), "Settings");
      }
    });

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Contact Tracing");
    }

    boolean isCompromised = isCompromisedAPI();
    locationEnabled.setText("" + isCompromised);
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

    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle("EMERGENCY BUTTON");
      builder.setMessage(
          "This is only meant to be pressed in the case that you have COVID-19. Are you sure about this?");
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

  private boolean isCompromisedAPI() {
    String[] codes = getLocalCodes();
    if (codes.length == 0) {
      codes = new String[1];
      codes[0] = getNextCodeAPI();
    }
    Log.d("COVID-23", codes[0]);
//    try {
//      URL url = new URL(COMPROMISED_URL);
//      HttpURLConnection conx = (HttpURLConnection) url.openConnection();
//      conx.setRequestMethod("POST");
//      conx.setRequestProperty("Content-Type", "application/json; utf-8");
//      conx.setRequestProperty("Accept", "application/json");
//      Gson gson = new Gson();
//      String jsonInput = "{\"codes\": gson.toJson(codes)}";
//      OutputStream os = conx.getOutputStream();
//      byte[] input = jsonInput.getBytes("utf-8");
//      os.write(input, 0, input.length);
//
//      if (conx.getResponseCode() == HttpURLConnection.HTTP_OK) {
//        BufferedReader in = new BufferedReader(new InputStreamReader(conx.getInputStream()));
//        String inputLine;
//        StringBuilder response = new StringBuilder();
//        while ((inputLine = in.readLine()) != null) {
//          response.append(inputLine);
//        }
//        in.close();
//        HashMap<String, Boolean> result = gson.fromJson(response.toString(), HashMap.class);
//        return result.get("result");
//      } else {
//        Log.e("COVID-23", "failed");
//      }
//    } catch (IOException e) {
//      // pass
//    }
    return false;
  }

  private String getNextCodeAPI() {
    try {
      URL url = new URL(NEW_CODE);
      HttpURLConnection conx = (HttpURLConnection) url.openConnection();
      conx.setRequestMethod("GET");
      if (conx.getResponseCode() == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(conx.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();
        return response.toString();
      } else {
        Log.e("COVID-23", "failed");
      }
    } catch (IOException e) {
      // pass
    }
    return "0000000";
  }

  private String[] getLocalCodes() {
    SharedPreferences sharedPrefs = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext());
    Gson gson = new Gson();
    String json = sharedPrefs.getString(LOCAL_CODES_KEY, "[]");
    return gson.fromJson(json, String[].class);
  }

  public void addLocalCode(String code) {
    String[] codes = getLocalCodes();
    String[] newCodes = new String[codes.length + 1];
    for (int i = 0; i < codes.length; ++i) {
      newCodes[i] = codes[i]; // copying over
    }
    newCodes[codes.length] = code;

    SharedPreferences sharedPrefs = PreferenceManager
        .getDefaultSharedPreferences(getApplicationContext());
    Editor editor = sharedPrefs.edit();
    Gson gson = new Gson();
    String json = gson.toJson(newCodes);
    editor.putString(LOCAL_CODES_KEY, json);
    editor.commit();
  }
}