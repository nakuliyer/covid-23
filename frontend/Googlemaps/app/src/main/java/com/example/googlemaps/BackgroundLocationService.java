package com.example.googlemaps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.text.DateFormat;
import java.util.Date;
import org.greenrobot.eventbus.EventBus;

public class BackgroundLocationService extends Service {

  private static final String CHANNEL_ID = "my_channel";
  private final IBinder binder = new LocalBinder();
  private static final long UPDATE_INTERVAL_IN_MIL = 10000;
  private static final long FASTEST_UPDATE_INTERVAL_IN_MIL = 5000;
  private static final int NOTI_ID = 1223;
  private boolean changingConfiguration = false;
  private NotificationManager notificationManager;

  private LocationRequest locationRequest;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private LocationCallback locationCallback;
  private Handler serviceHandler;
  private Location location;

  public BackgroundLocationService() {

  }

  @Override
  public void onCreate() {
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        onNewLocation(locationResult.getLastLocation());
      }
    };

    createLocationRequest();
    getLastLocation();

    HandlerThread handlerThread = new HandlerThread("COVID-23");
    handlerThread.start();
    serviceHandler = new Handler(handlerThread.getLooper());
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
          getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(channel);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  public void removeLocationUpdates() {
    try {
      fusedLocationProviderClient.removeLocationUpdates(locationCallback);
      stopSelf();
    } catch (SecurityException e) {
      Log.e("COVID-23", "lost location permission");
    }
  }

  private void getLastLocation() {
    try {
      fusedLocationProviderClient.getLastLocation().addOnCompleteListener(
          new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
              if (task.isSuccessful() && task.getResult() != null) {
                location = task.getResult();
              } else {
                Log.e("COVID-23", "failed to get location");
              }
            }
          });
    } catch (SecurityException e) {
      Log.e("COVID-23", "lost location privileges");
    }
  }

  private void createLocationRequest() {
    locationRequest = new LocationRequest();
    locationRequest.setInterval(UPDATE_INTERVAL_IN_MIL);
    locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MIL);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  private void onNewLocation(Location lastLocation) {
    location = lastLocation;
    notificationManager.notify(NOTI_ID, getNotification());
  }

  private Notification getNotification() {
    Intent intent = new Intent(this, BackgroundLocationService.class);
    String text = "Unknown Location";
    if (location != null) {
      text = new StringBuilder().append(location.getLatitude()).append(" / ")
          .append(location.getLongitude()).toString();
    }
    PendingIntent servicePendingIntent = PendingIntent
        .getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    PendingIntent activityPendingIntent = PendingIntent
        .getActivity(this, 0, new Intent(this, LoginActivity.class), 0);

    NotificationCompat.Builder builder = new Builder(this)
        .addAction(R.drawable.ic_baseline_map_24, "Launch", activityPendingIntent)
        .addAction(R.drawable.common_google_signin_btn_icon_light_normal, "Remove",
            servicePendingIntent)
        .setContentText(text)
        .setContentTitle(String
            .format("Location Updated: %1$s", DateFormat.getDateInstance().format(new Date())))
        .setOngoing(true)
        .setPriority(Notification.PRIORITY_DEFAULT)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setTicker(text)
        .setWhen(System.currentTimeMillis());

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      builder.setChannelId(CHANNEL_ID);
    }
    return builder.build();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private class LocalBinder extends Binder {

    private BackgroundLocationService getService() {
      return BackgroundLocationService.this;
    }
  }
}
