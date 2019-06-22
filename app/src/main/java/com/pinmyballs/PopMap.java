package com.pinmyballs;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pinmyballs.fragment.FragmentTournoiNew;

public class PopMap extends AppCompatActivity implements OnMapReadyCallback{

    Double latitude,longitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_map);

        //Adjust Popup size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8),(int)(height*0.6));
        //InitMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.checkmap);
        mapFragment.getMapAsync(this);
        //Get Intent
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extra = intent.getExtras();
            if (extra != null) {
                latitude = intent.getDoubleExtra(FragmentTournoiNew.INTENT_LATITUDE,0);
                longitude = intent.getDoubleExtra(FragmentTournoiNew.INTENT_LONGITUDE,0);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        LatLng tournoiLatLng = new LatLng(latitude, longitude);
        Intent intent = getIntent();
        Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(tournoiLatLng)
                    .title(intent.getStringExtra(FragmentTournoiNew.INTENT_NOMTOURNOI))
                    .snippet(intent.getStringExtra(FragmentTournoiNew.INTENT_ADDRESSTEXT)));
        marker.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tournoiLatLng,15));
        }


}
