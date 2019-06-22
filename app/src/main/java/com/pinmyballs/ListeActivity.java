package com.pinmyballs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.utils.BottomNavigationViewHelper;
import com.pinmyballs.utils.ListeFlipperAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListeActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";
    public static final String EXTRA_LOCATION_FROM_MAP ="com.pinmyballs.ListActivity.EXTRA_LOCATION_FROM_MAP";
    private static final int ACTIVITY_NUM = 3;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final LatLng mDefaultLocation = new LatLng(48.858250, 2.294577); //Tour Eiffel

    LatLng latLngListe = mDefaultLocation;
    int DISTANCE_MAX, ENSEIGNE_LIST_MAX_SIZE;
    ArrayList<Flipper> listeFlipper = new ArrayList<Flipper>();

    @BindView(R.id.buttonMyLocation)
    ImageButton buttonMyLocation;
    @BindView(R.id.buttonClearModeleFlipper)
    ImageButton buttonClearModeleFlipper;
    @BindView(R.id.autocompleteModeleFlipper)
    AutoCompleteTextView autocompleteModeleFlipper;
    @BindView(R.id.listViewFlippers)
    ListView listViewFlippers;

    private Context mContext = ListeActivity.this;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private AutocompleteSupportFragment autocompleteFragment;

    SharedPreferences settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        setupBottomNavigationView();
        setupToolBar();
        //setupViewPager(); not used here
        setupSharedPreferences();
        setupPlaceAutocomplete();
        setupAutocompleteChampFlipper();
        setupLocation();
        getLocationPermission();
        searchOnLoad();
    }

    /**
     * Responsible for retrieving SharedPreferences settings
     */
    private void setupSharedPreferences() {
        settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
        DISTANCE_MAX = settings.getInt(PreferencesActivity.KEY_PREFERENCES_RAYON, PreferencesActivity.DEFAULT_VALUE_RAYON);

        ENSEIGNE_LIST_MAX_SIZE = settings.getInt(PreferencesActivity.KEY_PREFERENCES_MAX_RESULT,
                PreferencesActivity.DEFAULT_VALUE_NB_MAX_LISTE);
        Log.d(TAG, "setupSharedPreferences: DISTANCE :" + DISTANCE_MAX +" km");
        Log.d(TAG, "setupSharedPreferences: MAX RESULTS :" + ENSEIGNE_LIST_MAX_SIZE);
    }

    /**
     * Responsible for locating the phone
     */
    private void setupLocation() {
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Responsible for getting the location from the map or around current location
     */
    private void searchOnLoad() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extra = intent.getExtras();
            if (extra != null) {
                LatLng latLng = extra.getParcelable(EXTRA_LOCATION_FROM_MAP);
                if (latLng != null) {
                    searchFlip(latLng);
                }
            }
            else {
                getDeviceLocation();
            }
        }
    }


    /**
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);

        menuItem.setChecked(true);
    }

    /**
     * ToolBar Setup
     */
    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.listToolBar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "onMenuItemClick: " + item);
                switch (item.getItemId()) {
                    case R.id.action_pref:
                        Log.d(TAG, "onMenuItemClick: Navigating to Preference page");
                        Intent intentPref = new Intent(mContext, PreferencesActivity.class);
                        startActivity(intentPref);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_menu, menu);
        return true;
    }

    /**
     * Setting up the Place Autocomplete
     */
    private void setupPlaceAutocomplete() {

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.apiKey));
            Log.d(TAG, "setupPlaceAutocomplete: " + "Places API initialized");
        }

        //Autocomplete
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Limit results to Europe
        assert autocompleteFragment != null;
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(36.748837, -11.204687),
                new LatLng(52.275758, 24.654688)));

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint("Ville, lieu, adresse...");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                latLngListe = place.getLatLng();
                //buttonMyLocation.setBackgroundResource(R.drawable.ic_my_location_blue_24dp);
                if (latLngListe != null) {
                    searchFlip(latLngListe);
                }
                Log.i(TAG, "Listing around : " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     * Setting up the Autocomplete Text Field for Modele Flipper
     */
    private void setupAutocompleteChampFlipper() {
        BaseModeleService modeleFlipperService = new BaseModeleService();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                modeleFlipperService.getAllNomModeleFlipper(getApplicationContext()));
        autocompleteModeleFlipper.setAdapter(adapter);
        autocompleteModeleFlipper.setImeOptions(EditorInfo.IME_ACTION_DONE);
        autocompleteModeleFlipper.setDropDownAnchor(R.id.autocompleteModeleFlipper);
        autocompleteModeleFlipper.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autocompleteModeleFlipper.getWindowToken(), 0);
                searchFlip(latLngListe);
            }
        });

        autocompleteModeleFlipper.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(autocompleteModeleFlipper.getWindowToken(), 0);
                searchFlip(latLngListe);
                return false;
            }
        });
    }

    private void searchFlip(LatLng latLng) {
        // Récupère la liste des flippers les plus proches
        BaseFlipperService rechercheService = new BaseFlipperService();
        latLngListe = latLng;

        listeFlipper = rechercheService.rechercheFlipper(getApplicationContext(), latLng.latitude, latLng.longitude,
                DISTANCE_MAX * 1000, ENSEIGNE_LIST_MAX_SIZE, autocompleteModeleFlipper.getText().toString());
        Log.d(TAG, "searchFlip: results : "+ listeFlipper.size()+ " flippers");

        ListeFlipperAdapter customAdapter = new ListeFlipperAdapter(this, R.layout.simple_list_item_flipper, listeFlipper, latLng.latitude, latLng.longitude);
        listViewFlippers.setAdapter(customAdapter);

        //troubleshooting
        if (listeFlipper.size() == 0) {
            if (autocompleteModeleFlipper.getText() == null || autocompleteModeleFlipper.getText().length() == 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Aucun résultat!")
                        .setMessage("Pas de flippers à " + String.valueOf(DISTANCE_MAX) + "km à la ronde!")
                        .setNeutralButton("Fermer", null)
                        .setIcon(R.drawable.ic_delete).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Aucun résultat!")
                        .setMessage("Le flipper recherché n'a pas été trouvé à " + String.valueOf(DISTANCE_MAX) + "km à la ronde!")
                        .setNeutralButton("Fermer", null)
                        .setIcon(R.drawable.ic_delete).show();
            }
        }
    }


    /**
     * Gets the current location of the device and search for pinballs
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
                                    // Set the map's camera position to the current location of the device.
                                    mLastKnownLocation = location;
                                    latLngListe = new LatLng(location.getLatitude(), location.getLongitude());
                                    //buttonMyLocation.setBackgroundResource(R.drawable.ic_my_location_blue_24dp);
                                    autocompleteFragment.setText("");
                                    Log.d(TAG, "onSuccess: Updating mLastKnownLocation to: (" + latLngListe.latitude + ", "+ latLngListe.longitude+")");
                                    searchFlip(latLngListe);

                                } else {
                                    Log.d(TAG, "Current location is null. Using defaults.");

                                    Toast.makeText(mContext,"Could not find location",Toast.LENGTH_SHORT).show();
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
                                           @NonNull String permissions[],
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

    public LatLng getLocFromList(){
        return latLngListe;
    }

    @OnClick(R.id.buttonMyLocation)
    public void findMyLocation() {
        getLocationPermission();
        getDeviceLocation();
    }

    @OnClick(R.id.buttonClearModeleFlipper)
    public void clearModeleFlipper() {
        autocompleteModeleFlipper.setText("");
        searchFlip(latLngListe);
    }
}
