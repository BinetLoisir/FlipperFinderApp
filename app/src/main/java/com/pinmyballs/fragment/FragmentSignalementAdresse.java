package com.pinmyballs.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.pinmyballs.R;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.utils.LocationUtil;

import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentSignalementAdresse extends SignalementWizardFragment {

    private static final String TAG = "FragmentSignalementAdre";

    @BindView(R.id.place_attribution_wizard)
    TextView mPlaceAttribution;
    @BindView(R.id.champNomEnseigne)
    TextView champNomEnseigne;
    @BindView(R.id.champAdresse)
    TextView champAdresse;
    @BindView(R.id.champCodePostal)
    TextView champCodePostal;
    @BindView(R.id.champVille)
    TextView champVille;
    @BindView(R.id.champPays)
    TextView champPays;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wizard_adresse, container, false);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this, rootView);
        setupPlaceAutocomplete();
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
