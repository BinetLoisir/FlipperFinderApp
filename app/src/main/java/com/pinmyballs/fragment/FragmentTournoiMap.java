package com.pinmyballs.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pinmyballs.R;
import com.pinmyballs.metier.Tournoi;
import com.pinmyballs.service.base.BaseTournoiService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FragmentTournoiMap extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "FragmentTournoiMap";
    private Switch mSwitchTournois;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LatLngBounds mMapBounds;
    ArrayList<Marker> markersListPassed;
    ArrayList<Marker> markersListComplete;


    final Map<String, Tournoi> markerTournoiObjMap = new HashMap<String, Tournoi>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tournoi_map, container, false);
        mSwitchTournois =  (Switch) view.findViewById(R.id.switchTournoi);
        mSwitchTournois.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Iterator iterator = markerTournoiObjMap.entrySet().iterator();
                if (isChecked) {
                    // The toggle is enabled
                    for(Marker marker : markersListPassed){
                        marker.setVisible(true);
                    }

                } else {
                    // The toggle is disabled
                    for(Marker marker : markersListPassed) {
                        if (marker.getTag().equals(0)) {
                            marker.setVisible(false);

                        }
                    }
                }
            }
        });
        //mMapBounds = new LatLngBounds(new LatLng(56,-9), new LatLng(42,14));

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }
        // R.id.maptournoi is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.maptournoi, mapFragment).commit();
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                // inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                //View infoWindow = inflater.inflate(R.layout.custom_infowindow_tournoi,null);

                //View infoWindow = getLayoutInflater().inflate(R.layout.custom_infowindow_tournoi,
                       // (FrameLayout) findViewById(R.id.maptournoi), false);

                View infoWindow = getLayoutInflater().inflate(R.layout.custom_infowindow_tournoi,
                        null);


                TextView title = ((TextView) infoWindow.findViewById(R.id.Tournoititle));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.Tournoisnippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        populateMapTournois(googleMap);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(BoundsFromMarkers(markersListComplete),200));

    }

    public void populateMapTournois(GoogleMap googleMap) {
        ArrayList<Tournoi> Tournois;
        markersListPassed = new ArrayList<Marker>();
        markersListComplete = new ArrayList<Marker>();

        // Récupère la liste des tournois
        BaseTournoiService baseTournoiService = new BaseTournoiService();
        Tournois = baseTournoiService.getAllTournoi(getActivity().getBaseContext()); //IMPORTANT

        for (Tournoi tournoi : Tournois) {
            LatLng latLng = new LatLng(Double.parseDouble(tournoi.getLatitude()), Double.parseDouble(tournoi.getLongitude()));
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(tournoi.getNameAndDate())
                    .icon(BitmapDescriptorFactory.defaultMarker(MarkerColor(tournoi)))
                    .snippet(tournoi.getTournoiDetails())
            );
            markerTournoiObjMap.put(marker.getId(), tournoi);
            markersListComplete.add(marker);

            try {
                if (new SimpleDateFormat("yyyy/MM/dd").parse(tournoi.getDate()).before(new Date())) {
                    markersListPassed.add(marker);
                    marker.setTag(0);
                    marker.setVisible(false);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, marker.getId());
        Tournoi tournoi = markerTournoiObjMap.get(marker.getId());
        if (tournoi == null) {
            return;
        }
        Uri uri = Uri.parse(tournoi.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private float MarkerColor(Tournoi tournoi) {
        try {
            if (new SimpleDateFormat("yyyy/MM/dd").parse(tournoi.getDate()).before(new Date())) {
                return BitmapDescriptorFactory.HUE_ORANGE;
            } else {
                return BitmapDescriptorFactory.HUE_GREEN;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return BitmapDescriptorFactory.HUE_GREEN;
    }

    private LatLngBounds BoundsFromMarkers(ArrayList<Marker> ListMarkers){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Marker marker : ListMarkers){
            builder.include(marker.getPosition());
        }
        return builder.build();
    }
}
