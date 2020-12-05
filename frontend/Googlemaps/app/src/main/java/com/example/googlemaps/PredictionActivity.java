package com.example.googlemaps;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PredictionActivity extends AppCompatActivity {
  TextView prediction_text;
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_prediction);
    prediction_text = (TextView) findViewById(R.id.prediction);
    float[] values = getIntent().getFloatArrayExtra("prediction_values");
    float prediction = (303 * values[1]) + values[0];
    prediction_text.setText((int) prediction + " cases");
  }
}
