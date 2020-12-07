package com.example.googlemaps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactTracingActivity extends AppCompatActivity {

    final String TAG = "CoV23_CT";
    final String LOCAL_CODES_KEY = "covid_23_local_codes";
    final String COMPROMISED_URL = "https://covid-23.herokuapp.com/check_compromised";
    final String NEW_CODE = "https://covid-23.herokuapp.com/get_new_code";
    final String I_HAVE_COVID_CODE = "https://covid-23.herokuapp.com/post_compromised_codes";
    final String AM_I_COMPROMISED = "https://covid-23.herokuapp.com/am_i_compromised";
    final String MARK_RECOVERED = "https://covid-23.herokuapp.com/mark_recovered";

    Button covidBtn;
    Button recoveredBtn;
    TextView locationEnabled;
    TextView infentionLikelihood;
    TextView infectionLikelihoodPreText;
    TextView youAreInfected;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_tracing);

        covidBtn = findViewById(R.id.ct_covid_btn);
        recoveredBtn = findViewById(R.id.ct_recovered_btn);
        locationEnabled = findViewById(R.id.ct_location_status);
        infentionLikelihood = findViewById(R.id.ct_compromised_status);
        infectionLikelihoodPreText = findViewById(R.id.ct_compromised);
        youAreInfected = findViewById(R.id.ct_you_are_infected);

        queue = Volley.newRequestQueue(this);

        covidBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment alarmMessage = new AlarmFragment();
                alarmMessage.show(getSupportFragmentManager(), "Settings");
            }
        });

        recoveredBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment alarmMessage = new RecoveryAlarmFragment();
                alarmMessage.show(getSupportFragmentManager(), "Settings");
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Contact Tracing");
        }

        isCompromisedAPI();
        isCovidAPI();
        if (getLocalCodes().length == 0) {
            infentionLikelihood.setText("Disabled");
            infentionLikelihood.setTextColor(Color.parseColor("#E91E63"));
        } else {
            infentionLikelihood.setText("Enabled");
            infentionLikelihood.setTextColor(Color.parseColor("#4CAF50"));
        }
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
                    ContactTracingActivity activity = (ContactTracingActivity) getActivity();
                    if (activity != null) {
                        activity.postCovidAPI();
                    }
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

    public static class RecoveryAlarmFragment extends androidx.fragment.app.DialogFragment {

        @NonNull
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("EMERGENCY BUTTON");
            builder.setMessage(
                "This is only meant to be pressed in the case that you have recovered from COVID-19. Are you sure about this?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ContactTracingActivity activity = (ContactTracingActivity) getActivity();
                    if (activity != null) {
                        activity.postRecoveredAPI();
                    }
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
        Log.d(TAG, "found codes: " + (new Gson()).toJson(codes));
        if (codes.length == 0) {
            StringRequest stringRequest = new StringRequest(Method.GET, NEW_CODE,
                new Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "received message: " + response);
                        addLocalCode(response);
                        postIsCompromisedAPI();
                    }
                }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "failed to get a new code");
                }
            });
            queue.add(stringRequest);
        } else {
            postIsCompromisedAPI();
        }
        return false;
    }

    private void postIsCompromisedAPI() {
        String[] codes = getLocalCodes();
        if (codes.length == 0) {
            Log.e(TAG, "expected codes");
            return;
        }
        Log.d(TAG, "posting with codes: " + (new Gson()).toJson(codes));
        JSONObject jsonBody = new JSONObject();
        JSONArray jsonCodes = new JSONArray();
        for (String code : codes) {
            jsonCodes.put(code);
        }
        try {
            jsonBody.put("codes", jsonCodes);
        } catch (JSONException e) {
            Log.e(TAG, "some whack error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(COMPROMISED_URL, jsonBody,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "covid response thingy: " + response);
                    boolean b = false;
                    try {
                        b = response.getBoolean("result");
                    } catch (JSONException e) {
                        Log.e(TAG, "no boolean at response.result; see above");
                    }
                    if (b) {
                        infentionLikelihood.setText("High");
                        infentionLikelihood.setTextColor(Color.parseColor("#E91E63"));
                    } else {
                        infentionLikelihood.setText("Low");
                        infentionLikelihood.setTextColor(Color.parseColor("#4CAF50"));
                    }
                }
            }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "some other whack error");
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void postCovidAPI() {
        String[] codes = getLocalCodes();
        if (codes.length == 0) {
            Log.e(TAG, "expected codes");
            return;
        }
        Log.d(TAG, "posting i have covid with codes: " + (new Gson()).toJson(codes));
        JSONObject jsonBody = new JSONObject();
        JSONArray jsonCodes = new JSONArray();
        for (String code : codes) {
            jsonCodes.put(code);
        }
        try {
            jsonBody.put("codes", jsonCodes);
        } catch (JSONException e) {
            Log.e(TAG, "some whack error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(I_HAVE_COVID_CODE, jsonBody,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "i have covid response thingy: " + response);
                }
            }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "some other whack error");
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void postRecoveredAPI() {
        String[] codes = getLocalCodes();
        if (codes.length == 0) {
            Log.e(TAG, "expected codes");
            return;
        }
        Log.d(TAG, "posting i am recovered with codes: " + (new Gson()).toJson(codes));
        JSONObject jsonBody = new JSONObject();
        JSONArray jsonCodes = new JSONArray();
        for (String code : codes) {
            jsonCodes.put(code);
        }
        try {
            jsonBody.put("codes", jsonCodes);
        } catch (JSONException e) {
            Log.e(TAG, "some whack error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(MARK_RECOVERED, jsonBody,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "i am recovered response thingy: " + response);
                }
            }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "some other whack error");
            }
        });
        queue.add(jsonObjectRequest);
    }

    private boolean isCovidAPI() {
        String[] codes = getLocalCodes();
        Log.d(TAG, "found codes: " + (new Gson()).toJson(codes));
        if (codes.length == 0) {
            StringRequest stringRequest = new StringRequest(Method.GET, NEW_CODE,
                new Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "received message: " + response);
                        addLocalCode(response);
                        postIsCovidAPI();
                    }
                }, new ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "failed to get a new code");
                }
            });
            queue.add(stringRequest);
        } else {
            postIsCovidAPI();
        }
        return false;
    }

    private void postIsCovidAPI() {
        String[] codes = getLocalCodes();
        if (codes.length == 0) {
            Log.e(TAG, "expected codes");
            return;
        }
        JSONObject jsonBody = new JSONObject();
        JSONArray jsonCodes = new JSONArray();
        for (String code : codes) {
            jsonCodes.put(code);
        }
        try {
            jsonBody.put("codes", jsonCodes);
        } catch (JSONException e) {
            Log.e(TAG, "some whack error");
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(AM_I_COMPROMISED, jsonBody,
            new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "do i have covid response thingy: " + response);
                    boolean b = false;
                    try {
                        b = response.getBoolean("result");
                    } catch (JSONException e) {
                        Log.e(TAG, "no boolean at response.result; see above");
                    }
                    if (b) {
                        infentionLikelihood.setVisibility(View.GONE);
                        infectionLikelihoodPreText.setVisibility(View.GONE);
                        youAreInfected.setVisibility(View.VISIBLE);
                        covidBtn.setVisibility(View.GONE);
                        recoveredBtn.setVisibility(View.VISIBLE);
                    }
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