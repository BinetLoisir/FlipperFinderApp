package com.pinmyballs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.utils.LocationUtil;

import org.parceler.Parcels;

import java.util.ArrayList;

public class PopMapLarge extends AppCompatActivity implements OnMapReadyCallback {

    public static final String KEY_LISTFLIP = "KEY_LISTFLIP";
    public static final String KEY_MODELNAME = "KEY_MODELNAME";

    ArrayList<Flipper> listFlipper;
    ArrayList<Marker> listMarker;
    LatLngBounds latLngBounds;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_map_large);

        //Widgets
        TextView modelTextView = findViewById(R.id.textmodele);

        //Adjust Popup size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.7));
        //InitMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.checkmap);
        mapFragment.getMapAsync(this);
        //Get Intent
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extra = intent.getExtras();
            if (extra != null) {
                modelTextView.setText(intent.getStringExtra(PopMapLarge.KEY_MODELNAME));
                listFlipper = new ArrayList<>();
                ArrayList<Parcelable> listParcel;
                listParcel = intent.getParcelableArrayListExtra(PopMapLarge.KEY_LISTFLIP);
                if (listParcel != null) {
                    for (Parcelable parcelable : listParcel) {
                        listFlipper.add(Parcels.unwrap(parcelable));
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_enseigne, null);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });




        addMarkers(googleMap);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.227638, 2.13749), 5));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,150));

    }

    public void addMarkers(GoogleMap googleMap) {
        if (listFlipper == null) {
            return;
        }
        listMarker = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Flipper flipper : listFlipper) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(flipper.getEnseigne().getLatitude()), Double.parseDouble(flipper.getEnseigne().getLongitude())))
                    .icon(BitmapDescriptorFactory.fromResource(MarkerChoice(flipper, false)))
                    .anchor((float) 0.5, (float) 1)
                    .title(flipper.getEnseigne().getNom())
                    .snippet(flipper.getEnseigne().getAdresse() + "\n" + flipper.getEnseigne().getCodePostal() + " " + flipper.getEnseigne().getVille()));
            listMarker.add(marker);
            builder.include(marker.getPosition());
        }
        if (listMarker.size() > 0) {
            latLngBounds = builder.build();
        }
    }

    private int MarkerChoice(Flipper flipper, Boolean morethanone) {
        int nbJours = LocationUtil.getDaysSinceMajFlip(flipper);
        if (nbJours < 8) {
            return morethanone ? R.mipmap.ic_flipsmarker_new : R.mipmap.ic_flipmarker_new;
        }
        if (nbJours < 60) {
            return morethanone ? R.mipmap.ic_flipsmarker_blue : R.mipmap.ic_flipmarker_blue;
        }
        if (nbJours < 365) {
            return morethanone ? R.mipmap.ic_flipsmarker_lightblue : R.mipmap.ic_flipmarker_lightblue;
        }
        if (nbJours > 365) {
            return morethanone ? R.mipmap.ic_flipsmarker_grey : R.mipmap.ic_flipmarker_grey;
        }
        return morethanone ? R.mipmap.ic_flipsmarker_grey : R.mipmap.ic_flipmarker_grey;
    }
}
