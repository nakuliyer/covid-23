package com.example.googlemaps;

import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.JsonObject;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StatsActivity extends AppCompatActivity {

    public static final String TAG = "CoV23_STATS";
    public static final String CHAMPAIGN_DATA = "https://covid-23.herokuapp.com/champaign";
    public static final String ILLINOIS_DATA = "https://covid-23.herokuapp.com/illinois";
    public static final String NATIONAL_DATA = "https://covid-23.herokuapp.com/nation";

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        queue = Volley.newRequestQueue(this);

        displayChartFromAPI(regions.kChampaign, types.kCases);
        displayChartFromAPI(regions.kNation, types.kCases);
        displayChartFromAPI(regions.kState, types.kCases);
        // Future TODO: add types.kPercentages

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Statistics");
        }
    }

    private void displayChartFromAPI(final regions region, final types type) {
        String url = getUrl(region);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    ArrayList<Entry> chartData = getData(region, type, response);
                    int component = getComponent(region);
                    addLineChartToComponent(chartData, component);
                }
            }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(150000,
            150000,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonObjectRequest);
    }

    private void addLineChartToComponent(ArrayList<Entry> data, int component) {
        LineChart lineChart = findViewById(component);
        LineDataSet lineDataSet1 = new LineDataSet(data, "Data Set 1");
        lineDataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet1);

        // https://stackoverflow.com/questions/53302682/mpandroid-linechart-with-single-data-entry
        //customization
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setExtraLeftOffset(15);
        lineChart.setExtraRightOffset(15);
        //to hide background lines
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        //to hide right Y and top X border
        YAxis rightYAxis = lineChart.getAxisRight();
        rightYAxis.setEnabled(false);
        XAxis topXAxis = lineChart.getXAxis();
        topXAxis.setEnabled(false);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(5);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long epoch = (long) value;
                Date date = new Date(epoch);
                SimpleDateFormat df = new SimpleDateFormat("MMM dd");
                return df.format(date);
            }
        });

        lineDataSet1.setLineWidth(4f);
        lineDataSet1.setCircleRadius(3f);
        lineDataSet1.setDrawValues(false);
        lineDataSet1.setCircleHoleColor(getResources().getColor(R.color.colorPrimary));
        lineDataSet1.setCircleColor(getResources().getColor(R.color.colorPrimary));

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        lineChart.animateX(1000);
        lineChart.invalidate();
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
    }

    private ArrayList<Entry> getData(regions region, types type, JSONObject api) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        if (region == regions.kNation || region == regions.kState) {
            try {
                JSONArray percentages = api.getJSONArray("percentages");
                JSONArray positives = api.getJSONArray("positives");
                JSONArray times = api.getJSONArray("times");

                ArrayList<Entry> res = new ArrayList<Entry>();
                for (int i = 0; i < times.length(); ++i) {
                    long x = df.parse(times.getString(i)).getTime();
                    if (type == types.kPercentages) {
                        res.add(new Entry(x, (float) percentages.getDouble(i)));
                    } else if (type == types.kCases) {
                        res.add(new Entry(x, positives.getInt(i)));
                    }
                }
                return res;
            } catch (JSONException | ParseException e) {
                Log.e(TAG, e.toString());
            }
        } else if (region == regions.kChampaign) {
            try {
                JSONArray percentages = api.getJSONArray("case_positivity_percent");
                JSONArray positives = api.getJSONArray("new_cases");

                ArrayList<Entry> res = new ArrayList<Entry>();
                if (type == types.kPercentages) {
                    JSONArray times = percentages.getJSONArray(0);
                    for (int i = 0; i < times.length(); ++i) {
                        res.add(new Entry(df.parse(times.getString(i).substring(0, 10)).getTime(),
                            (float) percentages.getJSONArray(1).getDouble(i)));
                    }
                } else if (type == types.kCases) {
                    JSONArray times = positives.getJSONArray(0);
                    for (int i = 0; i < times.length(); ++i) {
                        res.add(new Entry(df.parse(times.getString(i).substring(0, 10)).getTime(),
                            positives.getJSONArray(1).getInt(i)));
                    }
                }
                return res;
            } catch (JSONException | ParseException e) {
                Log.e(TAG, e.toString());
            }
        }
        return null;
    }

    private int getComponent(regions region) {
        switch (region) {
            case kNation:
                return R.id.national_data;
            case kState:
                return R.id.illinois_data;
            case kChampaign:
                return R.id.champaign_data;
        }
        throw new RuntimeException("screwed");
    }

    private String getUrl(regions region) {
        switch (region) {
            case kNation:
                return NATIONAL_DATA;
            case kState:
                return ILLINOIS_DATA;
            case kChampaign:
                return CHAMPAIGN_DATA;
        }
        throw new RuntimeException("even more screwed");
    }
}

enum regions {
    kNation,
    kState,
    kChampaign
}

enum types {
    kPercentages,
    kCases
}
