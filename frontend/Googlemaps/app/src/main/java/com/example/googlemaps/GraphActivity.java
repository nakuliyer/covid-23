package com.example.googlemaps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import org.json.JSONObject;


public class GraphActivity extends AppCompatActivity {
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> seriesfar;
    int y,x;
    int[] weekly = new int[7];
    int[] sofars= new int[250];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        final GraphView graph = (GraphView) findViewById(R.id.graph);
        final GraphView sofar = (GraphView) findViewById(R.id.graph2);
        series = new LineGraphSeries<>();
        seriesfar = new LineGraphSeries<>();
        final Button btnLogout = findViewById(R.id.lgot);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            }
        });
        String URL = "https://api.covidtracking.com/v1/us/daily.json";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new com.android.volley.Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < weekly.length; i++) {
                                JSONObject toPass = response.getJSONObject(i);
                                String number = toPass.getString("positive");
                                int positive = Integer.parseInt(number);
                                x = i;
                                y = i;
                                series.appendData(new DataPoint(x,positive), true, 7);
                            }
                            graph.addSeries(series);

                            for (int i = 0; i < sofars.length; i++) {
                                JSONObject toPass = response.getJSONObject(i);
                                String number = toPass.getString("positive");
                                int positive = Integer.parseInt(number);
                                x = i;
                                y = i;
                                seriesfar.appendData(new DataPoint(x,positive), true, 250);
                            }
                            sofar.addSeries(seriesfar);


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

}
