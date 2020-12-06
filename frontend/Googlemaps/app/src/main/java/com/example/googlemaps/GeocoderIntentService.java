package com.example.googlemaps;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocoderIntentService extends IntentService {
    private static final String TAG = GeocoderIntentService.class.getSimpleName();
    protected ResultReceiver mReceiver;
    public GeocoderIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String errorMessage = "";
        mReceiver = intent.getParcelableExtra("receiver");
        if (mReceiver == null) {
            Log.e(TAG, "No receiver found");
            return;
        }
        Location location = intent.getParcelableExtra("location");
        if (location == null) {
            errorMessage = "Location not found";
            Log.e(TAG, errorMessage);

            // Result code 1 = Failure
            deliverResult(1, errorMessage);
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        // Reverse geocoding to get coordinates of current position
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            errorMessage = "Location service unavailable";
            ioException.printStackTrace();
            Log.e(TAG, errorMessage, ioException);
        }
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "Address not found";
                Log.e(TAG, errorMessage);
            }
            deliverResult(1, errorMessage);
        } else {
            // Result code 0 = Success
            // Use coordinates to retrieve state
            Address address = addresses.get(0);
            String state = address.getAdminArea();
            deliverResult(0, state);
        }
    }

    // Sends state (code 0) or error message (code 1) to receiver
    private void deliverResult(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString("result", message);
        mReceiver.send(resultCode, bundle);
    }
}
