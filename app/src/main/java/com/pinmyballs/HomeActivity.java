package com.pinmyballs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.fragment.FragmentTournoiNew;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.utils.AsyncTaskMajDatabase;
import com.pinmyballs.utils.AsyncTaskMajDatabaseBackground;
import com.pinmyballs.utils.BottomNavigationViewHelper;
import com.pinmyballs.utils.LocationUtil;
import com.pinmyballs.utils.NetworkUtil;
import com.pinmyballs.utils.SectionsPagerAdapter;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraIdleListener {
    public static final String EXTRA_LOCATION_FROM_LIST = "com.pinmyballs.HomeActivity.EXTRA_LOCATION_FROM_LIST";
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 2;
    private static final int DEFAULT_ZOOM = 15;
    private static final int SEARCH_ZOOM = 13;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    final Map<String, Flipper> markerObjMap = new HashMap<>();
    private final LatLng mDefaultLocation = new LatLng(48.858250, 2.294577); //Tour Eiffel
    SharedPreferences settings;
    private Context mContext = HomeActivity.this;
    //FOR THE MAP--------------------------------------------------------
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    //FOR THE MAP--------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);

        setContentView(R.layout.activity_home);
        setupSharedPreferences();
        setupBottomNavigationView();
        setupToolBar();
        //setupViewPager(); not used here
        getLocationPermission();
        setupLocation();
        setupMap();
        setupPlaceAutocomplete();
        //updateDBinBackground();
        checkIfMajNeeded();
    }

    /**
     * Responsible for retrieving SharedPreferences settings
     */
    private void setupSharedPreferences() {
        settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        //First we check if PREFERENCES file is set up with values for DATABASE VERSION and DATE_LAST_UPDATE
        // if not we give it the values from the SQLite database
        if (settings.getString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, "0").equals("0")) {
            Log.d(TAG, "setupSharedPreferences: Key Pref Database Version not set => InitDB");
            editor.putString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, FlipperDatabaseHandler.DATABASE_DATE_MAJ);
            editor.putString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, String.valueOf(FlipperDatabaseHandler.DATABASE_VERSION));
            editor.apply();
        }
        //If KEY_PREFERENCES_DATABASE_VERSION is already set-up, we check if a DATABASE VERSION update has been done by the developer (used the reset the SQLite DB)
        // in which case we reset the DATE_LAST_UPDATE to the default value 2011/01/01
        else {
            if (Integer.parseInt(settings.getString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, "0"))
                    < FlipperDatabaseHandler.DATABASE_VERSION) {
                Log.d(TAG, "setupSharedPreferences: New Database Version => Reset DATE_LAST_UPDATE");
                editor.putString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, FlipperDatabaseHandler.DATABASE_DATE_MAJ);
                editor.putString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, String.valueOf(FlipperDatabaseHandler.DATABASE_VERSION));
                editor.apply();
            }
        }

        if (settings.getBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, PreferencesActivity.DEFAULT_VALUE_ADMIN_MODE)) {
            Log.d(TAG, "setupSharedPreferences: AdminMode : already set to true");
        } else {
            Log.d(TAG, "setupSharedPreferences: AdminMode : set to false");
            editor.putBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, false);
            editor.apply();
        }

    }


    /**
     * Does the update of the database
     */
    private void checkIfMajNeeded() {
        String dateDerniereMajString = settings.getString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, FlipperDatabaseHandler.DATABASE_DATE_MAJ);
        int nbJours;
        try {
            Date dateDerniereMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(dateDerniereMajString);
            nbJours = Days.daysBetween(new DateTime(dateDerniereMaj), new DateTime(new Date())).getDays();
            if (nbJours > 365) {
                new AlertDialog.Builder(this).setTitle(R.string.dialogMajDBNeededTitle)
                        .setMessage(getResources().getString(R.string.dialogMajDBNeeded2))
                        .setPositiveButton(R.string.dialogMajDBNeededOK, (dialog, which) -> updateDB())
                        .setNegativeButton(R.string.dialogMajDBLater, (dialog, which) -> dialog.dismiss())
                        .show();
            } else if (nbJours > 3) {
                new AlertDialog.Builder(this).setTitle(R.string.dialogMajDBNeededTitle)
                        .setMessage(getResources().getString(R.string.dialogMajDBNeeded, nbJours))
                        .setPositiveButton(R.string.dialogMajDBNeededOK, (dialog, which) -> updateDB())
                        .setNegativeButton(R.string.dialogMajDBLater, (dialog, which) -> dialog.dismiss())
                        .show();
            } else if (nbJours > 1) {
                new AsyncTaskMajDatabaseBackground(HomeActivity.this, settings).execute();
                Log.d(TAG, "checkIfMajNeeded: Updated in background");
            }
        } catch (ParseException ignored) {
        }
    }

    public void updateDB() {
        if (NetworkUtil.isConnected(getApplicationContext())) {
            if (NetworkUtil.isConnectedFast(getApplicationContext())) {
                new AsyncTaskMajDatabase(HomeActivity.this, settings).execute();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastMajPasPossibleTropLent), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastMajPasPossible), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void updateDBinBackground() {
        if (NetworkUtil.isConnected(getApplicationContext())) {
            if (NetworkUtil.isConnectedFast(getApplicationContext())) {
                new AsyncTaskMajDatabaseBackground(HomeActivity.this, settings).execute();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastMajPasPossibleTropLent), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastMajPasPossible), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Responsible for locating the phone
     */
    private void setupLocation() {
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Responsible for adding the map
     */
    private void setupMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_home);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Responsible for adding the tabs
     */
    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentTournoiNew());
        adapter.addFragment(new FragmentTournoiNew());
        adapter.addFragment(new FragmentTournoiNew());
        ViewPager viewPager = findViewById(R.id.container2);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("Info");
        tabLayout.getTabAt(1).setText("Actions");
        tabLayout.getTabAt(2).setText("Avis");
    }

    /**
     * Bottom Navigation View Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);
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
        Toolbar toolbar = findViewById(R.id.homeToolBar);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, "onMenuItemClick: " + item);
                switch (item.getItemId()) {
                    case R.id.action_legend:
                        Log.d(TAG, "onMenuItemClick: Popping Legend");
                        Intent intentPopLegend = new Intent(mContext, PopLegend.class);
                        startActivity(intentPopLegend);
                        return true;
                    case R.id.action_admin:
                        Log.d(TAG, "onMenuItemClick: Navigating to Admin Page");
                        Intent intentAdmin = new Intent(mContext, AdminActivity.class);
                        startActivity(intentAdmin);
                        return true;
                    case R.id.action_dbupdate:
                        Log.d(TAG, "onMenuItemClick: Updating database");
                        updateDB();
                        return true;
                    case R.id.action_pref:
                        Log.d(TAG, "onMenuItemClick: Navigating to Preference page");
                        Intent intentPref = new Intent(mContext, PreferencesActivity.class);
                        startActivity(intentPref);
                        return true;
                    case R.id.action_email:
                        Log.d(TAG, "onMenuItemClick: Launching email for comment");
                        Resources resources = getApplicationContext().getResources();
                        String emailsTo = resources.getString(R.string.mailContact);
                        String emailsSubject = resources.getString(R.string.mail_subject_new_comment);
                        Intent intent2 = new Intent(Intent.ACTION_SEND);
                        intent2.setType("message/html");
                        intent2.putExtra(Intent.EXTRA_EMAIL, new String[]{emailsTo});
                        intent2.putExtra(Intent.EXTRA_SUBJECT, emailsSubject);
                        try {
                            startActivity(Intent.createChooser(intent2, "Envoi du mail"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            new AlertDialog.Builder(HomeActivity.this).setTitle("Envoi impossible!").setMessage("Vous n'avez pas de mail configuré sur votre téléphone.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_tristesse).show();
                        }
                        return true;
                    case R.id.action_bug:
                        Log.d(TAG, "onMenuItemClick: Launching email for bug report");
                        Resources resources1 = getResources();
                        String emailsTo1 = resources1.getString(R.string.mailContact);
                        String emailsSubject2 = resources1.getString(R.string.mail_subject_new_bug);
                        Intent intent3 = new Intent(Intent.ACTION_SEND);
                        intent3.setType("message/html");
                        intent3.putExtra(Intent.EXTRA_EMAIL, new String[]{emailsTo1});
                        intent3.putExtra(Intent.EXTRA_SUBJECT, emailsSubject2);
                        try {
                            startActivity(Intent.createChooser(intent3, "Envoi du mail"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            new AlertDialog.Builder(HomeActivity.this).setTitle("Envoi impossible!").setMessage("Vous n'avez pas de mail configuré sur votre téléphone.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_tristesse).show();
                        }
                        return true;

                    case R.id.action_login:
                        Log.d(TAG, "onMenuItemClick: Starting Login Activity");
                        Intent intentLogin = new Intent(mContext, LoginActivity.class);
                        startActivity(intentLogin);
                        return true;

                    case R.id.action_liste_modele:
                        Log.d(TAG, "onMenuItemClick: Starting Modele List Activity");
                        Intent intent5 = new Intent(HomeActivity.this, ListeModelsActivity.class);
                        startActivity(intent5);
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
        inflater.inflate(R.menu.home_menu, menu);

        MenuItem item = menu.findItem(R.id.action_admin);
        boolean adminMode = settings.getBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, PreferencesActivity.DEFAULT_VALUE_ADMIN_MODE);
        item.setVisible(adminMode);

        MenuItem item1 = menu.findItem(R.id.action_login);
        item1.setVisible(adminMode);

        return true;
    }

    private void setupPlaceAutocomplete() {

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), BuildConfig.ApiKey);
        }

        //Autocomplete
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Limit results to Europe
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(36.748837, -11.204687),
                new LatLng(52.275758, 24.654688));

        assert autocompleteFragment != null;
        autocompleteFragment.setLocationBias(bounds);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint(getString(R.string.searchHint));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), SEARCH_ZOOM));
                Log.i(TAG, "Map centered around : " + place.getName());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraIdleListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });


        // Do other setup activities here too, as described elsewhere in this tutorial.


        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        //Check if there is an intent, otherwise search around device location.
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extra = intent.getExtras();
            if (extra != null) {
                LatLng latLng = extra.getParcelable(EXTRA_LOCATION_FROM_LIST);
                if (latLng != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                    Log.i(TAG, "Searching around location from list");
                }
            } else {
                getDeviceLocation();
            }
        }
    }


    @Override
    public void onCameraIdle() {
        HomeActivity.FlipperSearchTask flipperSearchTask = new HomeActivity.FlipperSearchTask();
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        flipperSearchTask.execute(bounds);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, marker.getId());
        Flipper flipper = markerObjMap.get(marker.getId());
        if (flipper == null) {
            return;
        }

        Intent infoActivite = new Intent(mContext, PageInfoFlipperPager.class);
        // On va sur l'onglet des actions
        infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_ONGLET_DEFAUT, 1);
        infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO, flipper);
        startActivity(infoActivite);
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
                                    // Set the map's camera position to the current location of the device.
                                    mLastKnownLocation = location;

                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(mLastKnownLocation.getLatitude(),
                                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                } else {
                                    Log.d(TAG, "Current location is null. Using defaults.");
                                    mMap.moveCamera(CameraUpdateFactory
                                            .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
            Log.d(TAG, "getLocationPermission: Permission granted");
        } else {
            Log.d(TAG, "getLocationPermission: Permission denied");
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
        Log.d(TAG, "onRequestPermissionsResult: Result received");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: Result : permission granted");
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public LatLng getLocFromMap() {
        if (mMap == null) {
            return mDefaultLocation;
        } else {
            return mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
        }
    }

    /**
     * Searches and adds pinball locations on map.
     */
    class FlipperSearchTask extends AsyncTask<LatLngBounds, Integer, List<Flipper>> {

        @Override
        protected void onPostExecute(List<Flipper> listFlippers) {
            //TODO crash Ici quand pas de connection : Attempt to invoke interface method 'boolean java.util.List.isEmpty()' on a null object reference
            if (listFlippers == null) {
                return;
            }
            for (Flipper flipper : listFlippers) {
                boolean morethanone = false;
                StringBuilder s = new StringBuilder();
                s.append(flipper.getModele().getNom());
                BaseFlipperService baseFlipperService = new BaseFlipperService();
                ArrayList<Flipper> otherFlippers = baseFlipperService.rechercheOtherFlipper(getApplicationContext(), flipper);

                if (otherFlippers.size() > 0) {
                    morethanone = true;
                    for (Flipper extraflip : otherFlippers) {
                        s.append("\n").append(extraflip.getModele().getNom());
                    }
                }

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(Double.parseDouble(flipper.getEnseigne().getLatitude()), Double.parseDouble(flipper.getEnseigne().getLongitude())))
                        .icon(BitmapDescriptorFactory.fromResource(MarkerChoice(flipper, morethanone)))
                        .anchor((float) 0.5, (float) 1)
                        .title(s.toString())
                        .snippet(flipper.getEnseigne().getNom() + "\n" + flipper.getEnseigne().getAdresse() + "\n" + flipper.getEnseigne().getCodePostal() + " " + flipper.getEnseigne().getVille()));
                markerObjMap.put(marker.getId(), flipper);
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


        private int getDiagonalKM(LatLngBounds latLngbounds) {

            Location loc1 = new Location(LocationManager.GPS_PROVIDER);
            Location loc2 = new Location(LocationManager.GPS_PROVIDER);

            LatLng latLng1 = latLngbounds.southwest;
            LatLng latLng2 = latLngbounds.northeast;

            loc1.setLatitude(latLng1.latitude);
            loc1.setLongitude(latLng1.longitude);

            loc2.setLatitude(latLng2.latitude);
            loc2.setLongitude(latLng2.longitude);
            float distancemeter = loc1.distanceTo(loc2);

            return Math.round(distancemeter / 1000);
        }


        @Override
        protected List<Flipper> doInBackground(LatLngBounds... params) {
            //Log.d("FlipperSearchTask","Background Task Started");
            List<Flipper> listFlippers = null;

            /*
            //Requête Parse
            ParseCloudService parseCloudService = new ParseCloudService();
            try {
                //Log.d("FlipperSearchTask","execute Fetch on " + params[0]);
                listFlippers = parseCloudService.FetchNearbyFlippersBounds(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("FlipperSearchTask","Exception");
            }
            */

            //Requête Locale Sqlite
            BaseFlipperService baseFlipperService = new BaseFlipperService();
            String modeleFlipper = "";
            SharedPreferences settings;
            settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
            //DISTANCE_MAX = settings.getInt(PreferencesActivity.KEY_PREFERENCES_RAYON, PreferencesActivity.DEFAULT_VALUE_RAYON);
            int DISTANCE_MAX = getDiagonalKM(params[0]);
            int ENSEIGNE_LIST_MAX_SIZE = settings.getInt(PreferencesActivity.KEY_PREFERENCES_MAX_RESULT,
                    PreferencesActivity.DEFAULT_VALUE_NB_MAX_LISTE);

            try {
                listFlippers = baseFlipperService.rechercheFlipper(getApplicationContext(), params[0].getCenter(),
                        DISTANCE_MAX * 1000, ENSEIGNE_LIST_MAX_SIZE, modeleFlipper);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("FlipperSearchTask", "Exception");
            }

            return listFlippers;
        }
    }
}








