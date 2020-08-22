package com.pinmyballs.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pinmyballs.R;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.utils.OtherFlipperAdapter;

import java.util.ArrayList;

public class FragmentCarteFlipper extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "FragmentCarteFlipper";

    private Flipper flipper;
    private SupportMapFragment mapFragment;

    public FragmentCarteFlipper() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_flipper, container, false);
        flipper = (Flipper) getArguments().getSerializable("flip");

        // don't recreate fragment everytime ensure last map location/state are maintained
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(this);
        }
        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.mapframe, mapFragment)
                .commit();

        //Get listview
        ListView listView = rootView.findViewById(R.id.otherflipsListView);
        populateList(listView);

        return rootView;
    }


    private void populateList(ListView listeView) {
        // Récupère la liste des flippers de l'enseigne
        BaseFlipperService baseFlipperService = new BaseFlipperService();
        ArrayList<Flipper> otherFlippers = baseFlipperService.rechercheOtherFlipper(getActivity(), flipper);

        if (otherFlippers.size() > 0) {
            OtherFlipperAdapter otherFlipperAdapter = new OtherFlipperAdapter(requireActivity(), R.layout.simple_list_item_modele, otherFlippers);
            listeView.setAdapter(otherFlipperAdapter);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setZoomControlsEnabled(true);

        if (flipper != null && flipper.getEnseigne() != null) {
            LatLng latlng = new LatLng(Double.parseDouble(flipper.getEnseigne().getLatitude()),
                    Double.parseDouble(flipper.getEnseigne().getLongitude()));

            MarkerOptions markerOpt = new MarkerOptions()
                    .position(latlng)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_flipmarker_red));

            map.addMarker(markerOpt);

            // On centre la carte
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(latlng);
            LatLngBounds bounds = builder.build();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.northeast, 15));
        }
    }
}

