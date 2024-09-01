package com.pinmyballs.fragment;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.pinmyballs.BuildConfig;
import com.pinmyballs.R;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.utils.LocationUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;




import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.parse.Parse.getApplicationContext;

public class FragmentSignalementAdresse extends SignalementWizardFragment {

    private static final String TAG = "FragmentSignalementAdre";
    private PlacesClient placesClient;


    TextView mPlaceAttribution;

    ImageButton mLocationButton;

    TextView champNomEnseigne;

    TextView champAdresse;

    TextView champCodePostal;

    TextView champVille;

    TextView champPays;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wizard_adresse, container, false);
        mPlaceAttribution = (TextView) rootView.findViewById(R.id.place_attribution_wizard);
        mLocationButton = (ImageButton) rootView.findViewById(R.id.buttonMyLocation);
        champNomEnseigne = (TextView) rootView.findViewById(R.id.champNomEnseigne);
        champAdresse = (TextView) rootView.findViewById(R.id.champAdresse);
        champCodePostal = (TextView) rootView.findViewById(R.id.champCodePostal);
        champVille = (TextView) rootView.findViewById(R.id.champVille);
        champPays = (TextView) rootView.findViewById(R.id.champPays);
        super.onCreate(savedInstanceState);

        setupPlaceAutocomplete();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.ApiKey);
        }
        // Create a new PlacesClient instance
        placesClient = Places.createClient(getParentActivity());
        setupMyLocationButton();

        return rootView;
    }

    private void setupPlaceAutocomplete() {
        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());

                getParentActivity().setNewLocation(place.getLatLng());
                champNomEnseigne.setText(place.getName());
                HashMap HM = LocationUtil.getDetailsfromLatLng(getContext(), place.getLatLng());
                champAdresse.setText(String.valueOf(HM.get("address")));
                champCodePostal.setText(String.valueOf(HM.get("postalcode")));
                champVille.setText(String.valueOf(HM.get("city")));
                champPays.setText(String.valueOf(HM.get("country")));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void setupMyLocationButton() {
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Use fields to define the data types to return.
                //List<Place.Field> placeFields = Collections.singletonList(Place.Field.NAME);
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

                // Use the builder to create a FindCurrentPlaceRequest.
                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

                // Call findCurrentPlace and handle the response (first check that the user has granted permission).
                if (ContextCompat.checkSelfPermission(getParentActivity(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                    placeResponse.addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FindCurrentPlaceResponse response = task.getResult();
                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                //Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                  //      placeLikelihood.getPlace().getName(),
                                    //    placeLikelihood.getLikelihood()));
                            }
                            //Action
                            Place place = response.getPlaceLikelihoods().get(0).getPlace();
                            getParentActivity().setNewLocation(place.getLatLng());
                            champNomEnseigne.setText(place.getName());
                            HashMap HM = LocationUtil.getDetailsfromLatLng(getContext(), place.getLatLng());
                            champAdresse.setText(String.valueOf(HM.get("address")));
                            champCodePostal.setText(String.valueOf(HM.get("postalcode")));
                            champVille.setText(String.valueOf(HM.get("city")));
                            champPays.setText(String.valueOf(HM.get("country")));

                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                            }
                        }
                    });
                } else {
                    // A local method to request required permissions;
                    // See https://developer.android.com/training/permissions/requesting
                    //getLocationPermission();
                }


            }
        });
    }

    public boolean mandatoryFieldsComplete() {
        boolean isError = false;
        if (champAdresse.getText().length() == 0) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Vous devez renseigner l'adresse du flipper.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }
        if (champCodePostal.getText().length() == 0) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Vous devez renseigner le code postal du flipper.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }
        if (champVille.getText().length() == 0) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Vous devez renseigner la ville.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }
        if (champPays.getText().length() == 0) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Vous devez renseigner le pays.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }
        if (champNomEnseigne.getText().length() == 0) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Vous devez renseigner le nom de l'enseigne.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }
        return !isError;
    }

    public void completeStep() {
        Enseigne newEnseigne = new Enseigne();
        newEnseigne.setAdresse(champAdresse.getText().toString());
        newEnseigne.setCodePostal(champCodePostal.getText().toString());
        newEnseigne.setDateMaj(getFormattedDate());
        newEnseigne.setId(getNewEnseigneId());
        newEnseigne.setLatitude(String.valueOf(getCurrentLocation().latitude));
        newEnseigne.setLongitude(String.valueOf(getCurrentLocation().longitude));
        newEnseigne.setNom(champNomEnseigne.getText().toString());
        newEnseigne.setPays(champPays.getText().toString());
        newEnseigne.setVille(champVille.getText().toString());
        getParentActivity().setEnseigne(newEnseigne);
    }

}
