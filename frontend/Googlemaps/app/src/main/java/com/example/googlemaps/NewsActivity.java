package com.example.googlemaps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;

public class NewsActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Intent intent = getIntent();
        final String[] excerpts = new String[4];
        final String[] urls = new String[4];
        final String stateName = intent.getStringExtra("State name");
        String URL = "https://covid-23.herokuapp.com/news";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String toPass = response.getString("date");
                            JSONObject json = response.getJSONObject("locations");
//                            if (stateName.equalsIgnoreCase("California")) {
//                                JSONObject miniJSON = json.getJSONObject("US-CA");
//                                final JSONArray array = new JSONArray(miniJSON.getString("news"));
//                                for (int i = 0; i < 3; i++) {
//                                    urls[i] = array.getJSONObject(i).getString("originalUrl");
//                                    excerpts[i] = array.getJSONObject(i).getString("excerpt");
//                                }
//
//                            } else if (stateName.equalsIgnoreCase("Illinois")) {
//                                JSONObject miniJSON = json.getJSONObject("US-IL");
//                                final JSONArray array = new JSONArray(miniJSON.getString("news"));
//                                for (int i = 0; i < 3; i++) {
//                                    urls[i] = array.getJSONObject(i).getString("originalUrl");
//                                    excerpts[i] = array.getJSONObject(i).getString("excerpt");
//                                }
//
//                            } else if (stateName.equalsIgnoreCase("Massachusetts")) {
//                                JSONObject miniJSON = json.getJSONObject("US-MA");
//                                final JSONArray array = new JSONArray(miniJSON.getString("news"));
//                                for (int i = 0; i < 3; i++) {
//                                    urls[i] = array.getJSONObject(i).getString("originalUrl");
//                                    excerpts[i] = array.getJSONObject(i).getString("excerpt");
//                                }
//
//                            } else {

//                            }
                            JSONObject miniJSON = json.getJSONObject("US");
                            final JSONArray array = new JSONArray(miniJSON.getString("news"));
                            for (int i = 0; i < 3; i++) {
                                urls[i] = array.getJSONObject(i).getString("originalUrl");
                                excerpts[i] = array.getJSONObject(i).getString("excerpt");
                            }
                            final TextView news1 = (TextView) findViewById(R.id.news1);
                            final TextView news2 = (TextView) findViewById(R.id.news2);
                            news1.setText(excerpts[0] + "\n" + urls[0]);
                            news2.setText(excerpts[1] + "\n" + urls[1]);
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
