package com.pinmyballs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.service.GlobalService;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class PreferencesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String PREFERENCES_FILENAME = "FlipperLocPrefs.txt";
    public static final String KEY_PREFERENCES_ADMIN_MODE = "AdminMode";
	public static final String KEY_PSEUDO_FULL = "fullPseudo";
	public static final String KEY_LANGUAGE_SHORT = "en";
	public static final String KEY_PREFERENCES_RAYON = "rayonRecherche";
	public static final String KEY_PREFERENCES_MAX_RESULT = "listeMaxResult";
	public static final String KEY_PREFERENCES_DATE_LAST_UPDATE = "dateLastUpdate";
    public static final String KEY_PREFERENCES_DATABASE_VERSION = "databaseVersion";
    public static final String KEY_PREFERENCES_FAVORITE_LOCATION_LATITUDE = "FavLat";
    public static final String KEY_PREFERENCES_FAVORITE_LOCATION_LONGITUDE = "FavLng";
	public static final String KEY_PREFERENCES_CURRENT_LOCATION_LATITUDE = "CurrLat";
	public static final String KEY_PREFERENCES_CURRENT_LOCATION_LONGITUDE = "CurrLng";
    public static final String KEY_PREFERENCES_LOGIN = "Login";
    public static final String KEY_PREFERENCES_PASSWORD = "Password";
    public static final String KEY_PREFERENCES_REMEMBER = "RememberCredentials";
    //DEFAULT VALUES
    public static final int DEFAULT_VALUE_RAYON = 150;
    public static final int DEFAULT_VALUE_NB_MAX_LISTE = 100;
    public static final Boolean DEFAULT_VALUE_ADMIN_MODE = false;
	public static final String DEFAULT_VALUE_PSEUDO = "AAA";
    public static final String DEFAULT_VALUE_LATITUDE= "0";
    public static final String DEFAULT_VALUE_LONGITUDE= "0";
    public static final String DEFAULT_VALUE_LOGIN = "";
    public static final String DEFAULT_VALUE_PASSWORD = "";
    public static final boolean DEFAULT_VALUE_REMEMBER = false;
    //CONSTANTS
    private static final String TAG = "PreferencesActivity";
    public static final HashMap<String,String> lang_map = new HashMap<>();


    //BINDS
	@BindView(R.id.TVPseudoPref)
    EditText tvPseudo;
	@BindView(R.id.TVRayon)
	TextView tvRayon;
	@BindView(R.id.seekBarRayon)
	SeekBar seekBarRayon;
	@BindView(R.id.TVMaxResult)
	TextView tvNbMaxListe;
	@BindView(R.id.seekBarNbMax)
	SeekBar seekBarNbMaxListe;
	@BindView(R.id.languageSpinner)
	Spinner langageSpinner;

	@BindView(R.id.currentlatlng)
	TextView currentLatLng;
	@BindView(R.id.dbchrono)
	TextView dbchrono;
	@BindView(R.id.datedernieremaj)
	TextView datedernieremaj;
	@BindView(R.id.nbflips)
	TextView nbflips;

    @BindView(R.id.eraseDBbutton)
    Button EraseDB;

	ActionBar mActionbar;
	SharedPreferences settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		ButterKnife.bind(this);

		//spinnersetup
		langageSpinner = findViewById(R.id.languageSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.langugages_array,android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		langageSpinner.setAdapter(adapter);
		langageSpinner.setOnItemSelectedListener(this);

		langageSpinner.setVisibility(View.INVISIBLE);
		TextView text = findViewById(R.id.languageText);
		text.setVisibility(View.INVISIBLE);


		lang_map.put("Français","fr");
		lang_map.put("French","fr");
		lang_map.put("Anglais","en");
		lang_map.put("English","en");



		// Affichage du header
		mActionbar = getSupportActionBar();
		mActionbar.setTitle(R.string.headerPreferences);
		mActionbar.setHomeButtonEnabled(true);
		mActionbar.setDisplayHomeAsUpEnabled(true);

        setupPreferences();
	}

    private TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Editor editor = settings.edit();
            editor.putString(PreferencesActivity.KEY_PSEUDO_FULL, s.toString());
            editor.apply();
            Log.d(TAG, "New pseudo: " + s.toString());
        }
    };

    /**
     * Responsible for loading and displaying Preferences and Info
     */
    private void setupPreferences(){
        settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, MODE_PRIVATE);
        tvPseudo.setText(settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, DEFAULT_VALUE_PSEUDO));
        tvPseudo.addTextChangedListener(textChangedListener);

        int rayon = settings.getInt(KEY_PREFERENCES_RAYON, DEFAULT_VALUE_RAYON);
        seekBarRayon.setProgress(rayon);
		seekBarRayon.setOnSeekBarChangeListener(rayonChangeListener);
		Resources res =getResources();
        tvRayon.setText(String.format(res.getString(R.string.rayonmax), rayon));

        int listeMaxResult = settings.getInt(KEY_PREFERENCES_MAX_RESULT, DEFAULT_VALUE_NB_MAX_LISTE);
        seekBarNbMaxListe.setProgress(listeMaxResult);
		seekBarNbMaxListe.setOnSeekBarChangeListener(nbMaxListeChangeListener);
		tvNbMaxListe.setText(String.format(res.getString(R.string.listemax), listeMaxResult));

		currentLatLng.setText(String.format("(%s, %s)",settings.getString(KEY_PREFERENCES_CURRENT_LOCATION_LATITUDE,""), settings.getString(KEY_PREFERENCES_CURRENT_LOCATION_LONGITUDE,"")));

        dbchrono.setText(settings.getString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, ""));
        datedernieremaj.setText(settings.getString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, ""));
        nbflips.setText(new GlobalService().getNbFlips(getApplicationContext()));

        boolean adminMode = settings.getBoolean(KEY_PREFERENCES_ADMIN_MODE, DEFAULT_VALUE_ADMIN_MODE);
    }

	private OnSeekBarChangeListener rayonChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Resources res =getResources();
            tvRayon.setText(String.format(res.getString(R.string.rayonmax), progress));
        }

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Editor editor = settings.edit();
			editor.putInt(KEY_PREFERENCES_RAYON, seekBar.getProgress());
			editor.apply();
		}
	};

	private OnSeekBarChangeListener nbMaxListeChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Resources res =getResources();
            tvNbMaxListe.setText(String.format(res.getString(R.string.listemax), progress));
        }

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Editor editor = settings.edit();
			editor.putInt(KEY_PREFERENCES_MAX_RESULT, seekBar.getProgress());
			editor.apply();
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			onBackPressed();
			return  true;
		}
		return super.onOptionsItemSelected(item);
	}

	@OnClick(R.id.eraseDBbutton)
	public void EraseDB() {
		getApplicationContext().deleteDatabase(FlipperDatabaseHandler.FLIPPER_BASE_NAME);
		SharedPreferences.Editor editor = settings.edit();
        editor.putString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, "");
        editor.putString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, "0");
        editor.putString(PreferencesActivity.KEY_PSEUDO_FULL, DEFAULT_VALUE_PSEUDO);
        editor.putInt(PreferencesActivity.KEY_PREFERENCES_MAX_RESULT, DEFAULT_VALUE_NB_MAX_LISTE);
        editor.putInt(PreferencesActivity.KEY_PREFERENCES_RAYON, DEFAULT_VALUE_RAYON);
        editor.putString(PreferencesActivity.KEY_PREFERENCES_FAVORITE_LOCATION_LATITUDE, DEFAULT_VALUE_LATITUDE);
        editor.putString(PreferencesActivity.KEY_PREFERENCES_FAVORITE_LOCATION_LONGITUDE, DEFAULT_VALUE_LONGITUDE);
        editor.putString(PreferencesActivity.KEY_PREFERENCES_CURRENT_LOCATION_LATITUDE, DEFAULT_VALUE_LATITUDE);
        editor.putString(PreferencesActivity.KEY_PREFERENCES_CURRENT_LOCATION_LONGITUDE, DEFAULT_VALUE_LONGITUDE);
        editor.putBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, DEFAULT_VALUE_ADMIN_MODE);
        editor.apply();
		setupPreferences();
	}

    @OnLongClick(R.id.eraseDBbutton)
    public void ToggleAdminMode() {

        boolean adminMode = settings.getBoolean(KEY_PREFERENCES_ADMIN_MODE, DEFAULT_VALUE_ADMIN_MODE);

        Editor editor = settings.edit();
        editor.putBoolean(KEY_PREFERENCES_ADMIN_MODE, !adminMode);
        editor.apply();

        String messageSnack = !adminMode ? "AdminMode activated " : "AdminMode deactivated";

        View contextView = findViewById(R.id.eraseDBbutton);
        Snackbar.make(contextView, messageSnack, Snackbar.LENGTH_LONG)
                .show();
    }

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		String lang_long = adapterView.getItemAtPosition(i).toString();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PreferencesActivity.KEY_LANGUAGE_SHORT, lang_map.get(lang_long));
		editor.apply();
		Toast.makeText(adapterView.getContext(),settings.getString(KEY_LANGUAGE_SHORT,"default"),Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}
}
