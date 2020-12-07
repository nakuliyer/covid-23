package com.example.googlemaps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
        final String stateName = intent.getStringExtra("State");
        String URL = "https://covid-23.herokuapp.com/news";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject superJson = response.getJSONObject("result");
                            JSONObject json = superJson.getJSONObject("locations");
                            if (stateName.equalsIgnoreCase("California")) {
                                JSONObject miniJSON = json.getJSONObject("US-CA");
                                final JSONArray array = new JSONArray(miniJSON.getString("news"));
                                for (int i = 0; i < 3; i++) {
                                    urls[i] = array.getJSONObject(i).getString("originalUrl");
                                    excerpts[i] = array.getJSONObject(i).getString("title");
                                }

                            } else if (stateName.equalsIgnoreCase("Illinois")) {
                                JSONObject miniJSON = json.getJSONObject("US-IL");
                                final JSONArray array = new JSONArray(miniJSON.getString("news"));
                                for (int i = 0; i < 3; i++) {
                                    urls[i] = array.getJSONObject(i).getString("originalUrl");
                                    excerpts[i] = array.getJSONObject(i).getString("title");
                                }

                            } else if (stateName.equalsIgnoreCase("Massachusetts")) {
                                JSONObject miniJSON = json.getJSONObject("US-MA");
                                final JSONArray array = new JSONArray(miniJSON.getString("news"));
                                for (int i = 0; i < 3; i++) {
                                    urls[i] = array.getJSONObject(i).getString("originalUrl");
                                    excerpts[i] = array.getJSONObject(i).getString("title");
                                }

                            } else {

                            }
                            JSONObject miniJSON = json.getJSONObject("US");
                            final JSONArray array = new JSONArray(miniJSON.getString("news"));
                            for (int i = 0; i < 3; i++) {
                                urls[i] = array.getJSONObject(i).getString("originalUrl");
                                excerpts[i] = array.getJSONObject(i).getString("title");
                            }
                            final TextView news1 = (TextView) findViewById(R.id.news1);
                            final TextView news2 = (TextView) findViewById(R.id.news2);
                            final TextView news3 = (TextView) findViewById(R.id.news3);
                            news1.setText(excerpts[0]);
                            news2.setText(excerpts[1]);
                            news3.setText(excerpts[2]);
                            Button buttonOne = (Button) findViewById(R.id.button1);
                            Button buttonTwo = (Button) findViewById(R.id.button2);
                            Button buttonThree = (Button) findViewById(R.id.button3);
                            buttonOne.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = null;
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[0]));
                                    startActivity(intent);
                                }
                            });
                            buttonTwo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = null;
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[1]));
                                    startActivity(intent);
                                }
                            });
                            buttonThree.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = null;
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urls[2]));
                                    startActivity(intent);
                                }
                            });
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