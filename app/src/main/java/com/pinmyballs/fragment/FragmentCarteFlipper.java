package com.pinmyballs.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.utils.OtherFlipperAdapter;

import java.util.ArrayList;

public class FragmentCarteFlipper extends Fragment implements LocationListener, LocationSource, OnMapReadyCallback {

    public final static String INTENT_FLIPPER_INFO_TABMAP = "com.pinmyballs.fragment.FragmentCarteFlipper.INTENT_FLIPPER_INFO_TABMAP";

    private OnLocationChangedListener mListener;
    private LocationManager locationManager;

    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    LatLngBounds.Builder builder = null;
    private Flipper flipperToDisplay = new Flipper();
    public void setFlipperToDisplay(Flipper flip) {
        flipperToDisplay = flip;
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_flipper, container, false);
        // Remove old code
        // SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        // don't recreate fragment everytime ensure last map location/state are maintained
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }
        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.mapframe, mapFragment).commit();

        //Get listview
        ListView listView = rootView.findViewById(R.id.otherflipsListView);
        populateList(listView);


        return rootView;
    }


    public void populateList(ListView listeView){
        ArrayList<Flipper> otherFlippers = new ArrayList<Flipper>();

        // Récupère la liste des flippers de l'enseigne
        BaseFlipperService baseFlipperService = new BaseFlipperService();
        otherFlippers = baseFlipperService.rechercheOtherFlipper(getActivity(),flipperToDisplay);

        if (otherFlippers.size() > 0) {
            OtherFlipperAdapter otherFlipperAdapter = new OtherFlipperAdapter(getActivity(), R.layout.simple_list_item_modele, otherFlippers);
            listeView.setAdapter(otherFlipperAdapter);

            final ArrayList<Flipper> finalotherFlippers = otherFlippers;
        }

    }


    @Override
    public void onPause() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        super.onPause();
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        builder = new LatLngBounds.Builder();

        //final Map<String, Flipper> markerObjMap = new HashMap<String, Flipper>();
        // On parcourt la liste des flippers pour les afficher avec la magnifique icone
        if (flipperToDisplay != null && flipperToDisplay.getEnseigne() != null) {
            LatLng latlng = new LatLng(Double.valueOf(flipperToDisplay.getEnseigne().getLatitude()),
                    Double.valueOf(flipperToDisplay.getEnseigne().getLongitude()));

            MarkerOptions markerOpt = new MarkerOptions()
                    .position(latlng)
                    //.icon(getBitmapDescriptor(R.drawable.ic_flip));
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_flipmarker_red));


            mMap.addMarker(markerOpt);
            //markerObjMap.put(marker.getId(), flipperToDisplay);

            // On centre la carte
            builder.include(latlng);
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.northeast, 15));
        }
    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //VectorDrawable vectorDrawable = (VectorDrawable) getDrawable(id);
            VectorDrawable vectorDrawable = (VectorDrawable) ContextCompat.getDrawable(getActivity(),id);


            int h = vectorDrawable.getIntrinsicHeight();
            int w = vectorDrawable.getIntrinsicWidth();

            vectorDrawable.setBounds(0, 0, w, h);

            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bm);

        } else {
            return BitmapDescriptorFactory.fromResource(id);
        }
    }


}

