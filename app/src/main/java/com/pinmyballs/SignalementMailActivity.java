package com.pinmyballs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.utils.LocationUtil;
import com.pinmyballs.utils.MyLocation;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//Signalement par mail
public class SignalementMailActivity extends AppCompatActivity {
    private static final String TAG = "SignalementMailActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    MyLocation myLocation = new MyLocation();
    double latitude = 48.862731; // bar à côté du cirque d'hiver
    double longitude = 2.367354; // bar à côté du cirque d'hiver

    @BindView(R.id.champAdresseLocalisation)
    EditText adresseUtilisateurTV;
    @BindView(R.id.champNomEnseigne)
    EditText champNomEnseigne = null;
    @BindView(R.id.boutonClearAdresse)
    ImageButton boutonClear;
    @BindView(R.id.BoutonEnvoiInfosFlipper)
    Button boutonEnvoi;
    @BindView(R.id.boutonLocalisation)
    ImageButton boutonLocalisation;
    @BindView(R.id.autocompletionModeleFlipper)
    AutoCompleteTextView champModeleFlipper;

    ActionBar mActionbar;

    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private OnItemClickListener itemSelectionneListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(champModeleFlipper.getWindowToken(), 0);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signalement_mail);
        ButterKnife.bind(this);

        // Affichage du header
        mActionbar = getSupportActionBar();
        mActionbar.setTitle(R.string.headerSignalement);
        mActionbar.setHomeButtonEnabled(true);
        mActionbar.setDisplayHomeAsUpEnabled(true);

        adresseUtilisateurTV.setImeOptions(EditorInfo.IME_ACTION_DONE);

        BaseModeleService modeleFlipperService = new BaseModeleService();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, modeleFlipperService.getAllNomModeleFlipper(getApplicationContext()));
        champModeleFlipper.setAdapter(adapter);
        champModeleFlipper.setImeOptions(EditorInfo.IME_ACTION_DONE);
        champModeleFlipper.setDropDownAnchor(R.id.autocompletionModeleFlipper);
        champModeleFlipper.setOnItemClickListener(itemSelectionneListener);

        setupPhoneLocation();
    }

    private void setupPhoneLocation() {
        setupLocation();
        getLocationPermission();
        getDeviceLocation();
    }

    @OnClick(R.id.boutonClearAdresse)
    public void onClick(View v) {
        adresseUtilisateurTV.setText("");
    }

    @OnClick(R.id.BoutonEnvoiInfosFlipper)
    public void EnvoiParMail() {
        // On regarde d'abord si les champs sont renseignés
        boolean isError = false;
        if (adresseUtilisateurTV.getText().length() == 0) {
            new AlertDialog.Builder(SignalementMailActivity.this).setTitle("Envoi impossible!").setMessage("Vous devez renseigner l'adresse du flipper.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }
        if (champModeleFlipper.getText().length() == 0) {
            new AlertDialog.Builder(SignalementMailActivity.this).setTitle("Envoi impossible!").setMessage("Vous devez renseigner le modèle du flipper.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }

        if (champNomEnseigne.getText().length() == 0) {
            new AlertDialog.Builder(SignalementMailActivity.this).setTitle("Envoi impossible!").setMessage("Vous devez renseigner le nom de l'enseigne.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        }

        if (!isError) {
            // C'est bon, on envoie le mail
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/html");
            Resources resources = getResources();
            String emailsTo = resources.getString(R.string.mailContact);

            i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailsTo});
            i.putExtra(Intent.EXTRA_SUBJECT, "Nouveau Flipper");
            i.putExtra(Intent.EXTRA_TEXT, "Adresse : " + adresseUtilisateurTV.getText() + "\nModèle : " + champModeleFlipper.getText() +
                    "\nNom de l'enseigne : " + champNomEnseigne.getText());
            try {
                startActivity(Intent.createChooser(i, "Envoi du mail"));
            } catch (android.content.ActivityNotFoundException ex) {
                new AlertDialog.Builder(SignalementMailActivity.this).setTitle("Envoi impossible!").setMessage("Vous n'avez pas de mail configuré sur votre téléphone.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_tristesse).show();
            }
        }

    }

    @OnClick(R.id.boutonLocalisation)
    public void localiser(View v) {
        adresseUtilisateurTV.setText(GetMyLocation());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private String GetMyLocation() {
        String AdresseComplete = "inconnue";
        getDeviceLocation();
        if (mLastKnownLocation != null) {
            latitude = mLastKnownLocation.getLatitude();
            longitude = mLastKnownLocation.getLongitude();
            AdresseComplete = LocationUtil.getAddress(getApplicationContext(), latitude, longitude);
            //AdresseComplete = LocationUtil.getAdresseFromCoordGPSwCP(getApplicationContext(), latitude, longitude);
            if (AdresseComplete == null || AdresseComplete.length() == 0) {
                new AlertDialog.Builder(SignalementMailActivity.
                        this).setTitle("Argh!").setMessage("Votre adresse n'a pas pu être trouvée! Veuillez activer le GPS ou entrer votre adresse.").setNeutralButton("Fermer", null).setIcon(R.drawable.tete_martiens).show();
            }
        } else {
            new AlertDialog.Builder(SignalementMailActivity.
                    this).setTitle("Argh!").setMessage("Votre adresse n'a pas pu être trouvée! Veuillez activer le GPS et la connexion Wifi sur votre téléphone.").setNeutralButton("Fermer", null).setIcon(R.drawable.tete_martiens).show();
        }
        return AdresseComplete;
    }

    /**
     * Responsible for locating the phone
     */
    private void setupLocation() {
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    mLastKnownLocation = location;
                                } else {
                                    Log.d(TAG, "Current location is null. Using defaults.");
                                }
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }


}
