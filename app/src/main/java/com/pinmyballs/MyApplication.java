package com.pinmyballs;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.parse.Parse;

/**
 * Created by Raphaël on 02/10/2015. Continued by BinetLoisir on 21/02/2017
 */

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        //Parse Configuration
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId(getResources().getString(R.string.parseApplicationId))
                .clientKey(getResources().getString(R.string.parseClientKey))
                .server(getResources().getString(R.string.parseServerUrl)).build());

        //Places
        // Initialize Places.
        //Places.initialize(getApplicationContext(), getResources().getString(R.string.googleMapsApiKey));
        Places.initialize(getApplicationContext(), BuildConfig.ApiKey);

        Log.d(TAG, "onCreate: " + "Places initialized");

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
        Log.d(TAG, "onCreate: " + "PlacesClient instanciated");
    }
}
