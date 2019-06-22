package com.pinmyballs.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pinmyballs.R;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.utils.LocationUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentSignalementMap extends SignalementWizardFragment implements OnMapReadyCallback {

    private static final String TAG = "FragmentSignalementMap";
    private GoogleMap mMap;
    private UiSettings mUiSettings;
    View mView;

    @BindView(R.id.geolocaliseSignalement)
    Button boutonGeolocalisation;

    LatLng chosenPosition = null;
    LatLng eiffel = new LatLng(48.859160, 2.294418);

    //GoogleMap gmap;
    MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_wizard_map, container, false);
        ButterKnife.bind(this, mView);

        chosenPosition = getParentActivity().getNewLocation();
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) mView.findViewById(R.id.mapViewSignalement);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
       // geolocaliseEnseigne();
    }



    @OnClick(R.id.geolocaliseSignalement)
    public void geolocaliseEnseigne() {

        LatLng latLng = getParentActivity().getNewLocation();
        if (latLng != null) {
            updateMapPosition(latLng);
            chosenPosition = latLng;
            Toast.makeText(getContext(), "Positionnement sur carte réussie", Toast.LENGTH_SHORT).show();
        } else {

            Enseigne enseigne = getParentActivity().getEnseigne();
            if (enseigne == null) {
                return;
            }

            String adresseTexte = enseigne.getAdresseCompleteAvecPays();
            LatLng currentUserLocation = getCurrentLocation();
            LatLng localisationEnseigne = LocationUtil.getAddressFromText(getActivity(), adresseTexte, currentUserLocation.latitude, currentUserLocation.longitude);
            if (localisationEnseigne == null) {
                adresseTexte = enseigne.getCodePostal() + " " + enseigne.getVille() + " " + enseigne.getPays();
                localisationEnseigne = LocationUtil.getAddressFromText(getActivity(), adresseTexte, currentUserLocation.latitude, currentUserLocation.longitude);
            }
            if (localisationEnseigne != null) {
                updateMapPosition(localisationEnseigne);
                Toast.makeText(getContext(), "Géolocalisation de l'enseigne réussie", Toast.LENGTH_SHORT).show();

                chosenPosition = localisationEnseigne;
            } else {
                new AlertDialog.Builder(getActivity()).setTitle("Géolocalisation impossible!")
                        .setMessage("Impossible de géolocaliser l'adresse, vous devez manuellement cliquer sur la carte pour indiquer la position.").setNeutralButton("Fermer", null)
                        .setIcon(R.drawable.ic_delete).show();
            }
        }
    }

    private void updateMapPosition(LatLng location) {
        // petit marqueur qui va bien
        MarkerOptions markerOpt = new MarkerOptions().position(location);
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(markerOpt);
            addMarkers(location);
            // On centre la carte
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
        }
    }

    private void addMarkers(LatLng latLng){
        List<Flipper> listFlippers = null;
        BaseFlipperService baseFlipperService = new BaseFlipperService();
        String modeleFlipper = "";
        int DISTANCE_MAX = 25; // On cherche les flippers à 25km à la ronde
        int ENSEIGNE_LIST_MAX_SIZE = 50; // On cherche les 50 flippers à la ronde

        try {
            listFlippers = baseFlipperService.rechercheFlipper(getActivity().getApplicationContext(), latLng,
                    DISTANCE_MAX * 1000, ENSEIGNE_LIST_MAX_SIZE, modeleFlipper);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Exception in getting flippers around");
        }

        for(Flipper flipper : listFlippers){

            StringBuilder s = new StringBuilder();
            s.append(flipper.getModele().getNom());

            ArrayList<Flipper> otherFlippers = baseFlipperService.rechercheOtherFlipper(getActivity(), flipper);
            if (otherFlippers.size() > 0) {
                for (Flipper extraflip : otherFlippers) {
                    s.append(", " + extraflip.getModele().getNom());
                }
            }

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(flipper.getEnseigne().getLatitude()), Double.parseDouble(flipper.getEnseigne().getLongitude())))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .title(flipper.getEnseigne().getNom())
                    .snippet(s.toString())
            );
        }


    }


    @Override
    public void completeStep() {
        getParentActivity().setNewLocation(chosenPosition);
        getParentActivity().envoyer();
    }

    @Override
    public boolean mandatoryFieldsComplete() {
        if (chosenPosition == null) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!")
                    .setMessage("Cliquer sur la carte pour renseigner la précision précise de l'enseigne.").setNeutralButton("Fermer", null)
                    .setIcon(R.drawable.ic_delete).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        chosenPosition = getParentActivity().getNewLocation();

        if (mMap!=null) {
            mUiSettings = mMap.getUiSettings();
            mUiSettings.setZoomControlsEnabled(true);

            //Toast.makeText(getContext(), String.valueOf(chosenPosition.latitude), Toast.LENGTH_SHORT).show();
            mMap.addMarker(new MarkerOptions()
                    .position(eiffel)
                    .title("Marker"));
        }
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                android.util.Log.i("onMapClick", "Horray!");
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(arg0));
                addMarkers(arg0);
                chosenPosition = arg0;
                Toast.makeText(getContext(), "Enseigne déplacée manuellement", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
