package com.pinmyballs.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.pinmyballs.CommentaireActivity;
import com.pinmyballs.HomeActivity;
import com.pinmyballs.ListeActivity;
import com.pinmyballs.R;
import com.pinmyballs.SignalementActivity;
import com.pinmyballs.TournoiActivity;

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHelper";

    @SuppressLint("LongLogTag")
    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setupBottomNavigationView: Setting up");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(true);
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx bottomNavigationViewEx) {
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_tournaments:
                        Intent intent0 = new Intent(context, TournoiActivity.class); //ACTIVITY_NUM = 0
                        context.startActivity(intent0);
                        break;
                    case R.id.ic_comments:
                        Intent intent1 = new Intent(context, CommentaireActivity.class); //ACTIVITY_NUM = 1
                        context.startActivity(intent1);
                        break;
                    case R.id.ic_map:
                        Intent intent2 = new Intent(context, HomeActivity.class); //ACTIVITY_NUM = 2
                        if (context instanceof ListeActivity) {
                            LatLng latLng = ((ListeActivity) context).getLocFromList();
                            if (latLng != null) {
                                intent2.putExtra(HomeActivity.EXTRA_LOCATION_FROM_LIST, latLng);
                            }
                        }
                        context.startActivity(intent2);
                        break;
                    case R.id.ic_list:
                        Intent intent3 = new Intent(context, ListeActivity.class); //ACTIVITY_NUM = 3
                        if (context instanceof HomeActivity) {
                            LatLng latLng = ((HomeActivity) context).getLocFromMap();
                            if (latLng != null) {
                                intent3.putExtra(ListeActivity.EXTRA_LOCATION_FROM_MAP, latLng);
                            }                        }
                        context.startActivity(intent3);
                        break;
                    case R.id.ic_add:
                        Intent intent4 = new Intent(context, SignalementActivity.class); //ACTIVITY_NUM = 4
                        context.startActivity(intent4);
                        break;
                }
                return false;
            }
        });


    }
}
