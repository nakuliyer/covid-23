/* https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android */
package com.example.googlemaps;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import androidx.preference.PreferenceManager;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.gson.Gson;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ketan Ramani on 05/11/18.
 */

public class BackgroundLocationUpdateService extends Service implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

  final String LOCAL_CODES_KEY = "covid_23_local_codes";
  final String POST_LOCATION = "https://covid-23.herokuapp.com/post_location";
  final String NEW_CODE = "https://covid-23.herokuapp.com/get_new_code";

  RequestQueue queue;

  /* Declare in manifest
  <service android:name=".BackgroundLocationUpdateService"/>
  */
  private final String TAG = "CoV23_LOCATION_CT";
  private Context context;
  private boolean stopService = false;

  /* For Google Fused API */
  protected GoogleApiClient mGoogleApiClient;
  protected LocationSettingsRequest mLocationSettingsRequest;
  private String latitude = "0.0", longitude = "0.0";
  private FusedLocationProviderClient mFusedLocationClient;
  private SettingsClient mSettingsClient;
  private LocationCallback mLocationCallback;
  private LocationRequest mLocationRequest;
  private Location mCurrentLocation;
  /* For Google Fused API */

  @Override
  public void onCreate() {
    super.onCreate();
    context = this;
    queue = Volley.newRequestQueue(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    StartForeground();
    final Handler handler = new Handler();
    final Runnable runnable = new Runnable() {

      @Override
      public void run() {
        try {
          if (!stopService) {
            //Perform your task here
          }

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          if (!stopService) {
            handler.postDelayed(this, TimeUnit.SECONDS.toMillis(10));
          }
        }
      }
    };
    handler.postDelayed(runnable, 2000);

    buildGoogleApiClient();

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    Log.e(TAG, "Service Stopped");
    stopService = true;
    if (mFusedLocationClient != null) {
      mFusedLocationClient.removeLocationUpdates(mLocationCallback);
      Log.e(TAG, "Location Update Callback Removed");
    }
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void StartForeground() {

  }

  Location location;

  @Override
  public void onLocationChanged(Location location) {
    Log.i(TAG, "Location Changed Latitude : " + location.getLatitude() + "\tLongitude : " + location
        .getLongitude());

    latitude = String.valueOf(location.getLatitude());
    longitude = String.valueOf(location.getLongitude());
    this.location = location;

    if (latitude.equalsIgnoreCase("0.0") && longitude.equalsIgnoreCase("0.0")) {
      requestLocationUpdate();
    } else {
      String[] codes = getLocalCodes();
      Log.d(TAG, "found codes: " + (new Gson()).toJson(codes));
      if (codes.length == 0) {
        StringRequest stringRequest = new StringRequest(Method.GET, NEW_CODE,
            new Listener<String>() {
              @Override
              public void onResponse(String response) {
                Log.d(TAG, "received message: " + response);
                addLocalCode(response);
                reportLocation();
              }
            }, new ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "failed to get a new code");
          }
        });
        queue.add(stringRequest);
      } else {
        reportLocation();
      }
    }
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {

  }

  @Override
  public void onProviderEnabled(String provider) {

  }

  @Override
  public void onProviderDisabled(String provider) {

  }

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(10 * 1000);
    mLocationRequest.setFastestInterval(5 * 1000);
    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
    builder.addLocationRequest(mLocationRequest);
    builder.setAlwaysShow(true);
    mLocationSettingsRequest = builder.build();

    mSettingsClient
        .checkLocationSettings(mLocationSettingsRequest)
        .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
          @Override
          public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            Log.e(TAG, "GPS Success");
            requestLocationUpdate();
          }
        }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        int statusCode = ((ApiException) e).getStatusCode();
        switch (statusCode) {
          case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
            try {
              int REQUEST_CHECK_SETTINGS = 214;
              ResolvableApiException rae = (ResolvableApiException) e;
              rae.startResolutionForResult((AppCompatActivity) context, REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException sie) {
              Log.e(TAG, "Unable to execute request.");
            }
            break;
          case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
            Log.e(TAG,
                "Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
        }
      }
    }).addOnCanceledListener(new OnCanceledListener() {
      @Override
      public void onCanceled() {
        Log.e(TAG, "checkLocationSettings -> onCanceled");
      }
    });
  }

  @Override
  public void onConnectionSuspended(int i) {
    connectGoogleClient();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    buildGoogleApiClient();
  }

  protected synchronized void buildGoogleApiClient() {
    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    mSettingsClient = LocationServices.getSettingsClient(context);

    mGoogleApiClient = new GoogleApiClient.Builder(context)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();

    connectGoogleClient();

    mLocationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Log.i(TAG, "Location Received");
        mCurrentLocation = locationResult.getLastLocation();
        onLocationChanged(mCurrentLocation);
      }
    };
  }

  private void connectGoogleClient() {
    GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
    int resultCode = googleAPI.isGooglePlayServicesAvailable(context);
    if (resultCode == ConnectionResult.SUCCESS) {
      mGoogleApiClient.connect();
    }
  }

  @SuppressLint("MissingPermission")
  private void requestLocationUpdate() {
    mFusedLocationClient
        .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
  }

  private void reportLocation() {
    String[] codes = getLocalCodes();
    if (codes.length == 0) {
      Log.e(TAG, "expected codes");
      return;
    }
    final JSONObject jsonBody = new JSONObject();
    JSONArray jsonCodes = new JSONArray();
    for (String code : codes) {
      jsonCodes.put(code);
    }
    try {
      jsonBody.put("code", jsonCodes);
      jsonBody.put("lat", location.getLatitude());
      jsonBody.put("long", location.getLongitude());
    } catch (JSONException e) {
      Log.e(TAG, "some whack error");
    }

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(POST_LOCATION, jsonBody,
        new Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            Log.d(TAG, "posted location" + jsonBody + " with response " + response);
          }
        }, new ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "some other whack error");
      }
    });
    queue.add(jsonObjectRequest);
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