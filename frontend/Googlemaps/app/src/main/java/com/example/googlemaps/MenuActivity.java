package com.example.googlemaps;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener  authStateListener;
    String positive1;
    String positive2;
    int positivetoday;
    int difference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user == null){
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }
        };
        final Button buttonmap = findViewById(R.id.buttonmap);
        buttonmap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openMapActivity();
            }
        });
        final Button buttongraph = findViewById(R.id.buttongraph);
        buttongraph.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openGraphActivity();
            }
        });

        final Button btnLogout = findViewById(R.id.buttonlogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
        String URL = "https://api.covidtracking.com/v1/us/daily.json";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            JSONObject today = response.getJSONObject(0);
                            JSONObject yesterday = response.getJSONObject(1);
                            positive1 = today.getString("positive");
                            positive2 = yesterday.getString("positive");
                            positivetoday = Integer.parseInt(positive1);
                            difference = Integer.parseInt(positive1) - Integer.parseInt(positive2);
                            addNotification();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Response23", error.toString());
            }
        });
        queue.add(request);
    }
    public void openMapActivity(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    public void openGraphActivity(){
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }
    private void addNotification() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("COVID",
                    "COVIDNotify",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("COVIDNotification");
            mNotificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "COVID")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Daily Report")
                .setContentText(difference + " tested positive today in the country.")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());


    }
}
