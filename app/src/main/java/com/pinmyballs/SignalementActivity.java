package com.pinmyballs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.fragment.SignalementPagerAdapter;
import com.pinmyballs.fragment.SignalementWizardFragment;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.ParseFactory;
import com.pinmyballs.service.parse.ParseModeleService;
import com.pinmyballs.utils.AsyncTaskMajDatabaseBackground;
import com.pinmyballs.utils.MyLocation.LocationResult;
import com.pinmyballs.utils.NetworkUtil;
import com.pinmyballs.utils.ProgressBarHandler;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignalementActivity extends AppCompatActivity {

    LatLng currentLocation = null;
    LatLng newLocation = null;
    ProgressBarHandler mprogressBarHandler;

    @BindView(R.id.next_button)
    Button mNextButton;
    @BindView(R.id.prev_button)
    Button mPreviousButton;
    @BindView(R.id.signalementPager)
    ViewPager mPager;

    ActionBar mActionbar;
    ArrayList<Flipper> listeFlipper = null;
    /**
     * Temporary solution for getting ModelObjectID
     */
    ArrayList<String> listModelObjectID = null;
    String pseudo = null;
    Commentaire commentaire = null;
    Enseigne enseigne = null;
    SharedPreferences settings;
    private SignalementPagerAdapter mPagerAdapter;
    private long newId;
    private SimpleOnPageChangeListener pageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            updateBottomBar();
        }
    };
    private OnClickListener NextClickListener = new OnClickListener() {
        public void onClick(View v) {
            SignalementWizardFragment currentFragment = mPagerAdapter.getRegisteredFragment(mPager.getCurrentItem());
            if (currentFragment.mandatoryFieldsComplete()) {
                currentFragment.completeStep();
                mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
            }
        }
    };
    private OnClickListener EnvoyerClickListener = new OnClickListener() {
        public void onClick(View v) {
            // C'est bon, on envoie le mail
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/html");
            Resources resources = getResources();
            String emailsTo = resources.getString(R.string.mailContact);

            i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailsTo});
            i.putExtra(Intent.EXTRA_SUBJECT, "Nouveau Flipper");
            /*
             * i.putExtra(Intent.EXTRA_TEXT,
             * "Adresse : "+adresseUtilisateurTV.getText
             * ()+"\nModèle : "+champModeleFlipper.getText()+
             * "\nNom de l'enseigne : " + champNomEnseigne.getText());
             */
            try {
                startActivity(Intent.createChooser(i, "Envoi du mail"));
            } catch (android.content.ActivityNotFoundException ex) {
                new AlertDialog.Builder(SignalementActivity.this).setTitle("Envoi impossible!")
                        .setMessage("Vous n'avez pas de mail configuré sur votre téléphone.")
                        .setNeutralButton("Fermer", null).setIcon(R.drawable.ic_tristesse).show();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentLocation = new LatLng(48.862731, 2.367354);

        setContentView(R.layout.activity_signalement);
        settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);

        ButterKnife.bind(this);

        mprogressBarHandler = new ProgressBarHandler(this);

        mNextButton.setOnClickListener(NextClickListener);

        TypedValue v = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
        mPreviousButton.setTextAppearance(this, v.resourceId);

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPagerAdapter = new SignalementPagerAdapter(fragmentManager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(pageChangeListener);

        newId = new Date().getTime();

        // Affichage du header
        mActionbar = getSupportActionBar();
        mActionbar.setTitle(R.string.headerSignalementNew);
        mActionbar.setHomeButtonEnabled(true);
        mActionbar.setDisplayHomeAsUpEnabled(true);

        updateBottomBar();
        localiseTelephone();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_signalementnew, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_signalbymail:
                ////EasyTracker.getTracker().sendEvent("ui_action", "button_press", "preferences", 0L);
                Intent intent = new Intent(SignalementActivity.this, SignalementMailActivity.class);
                startActivity(intent);
                break;
            default:
                Log.i("Erreur action bar", "default");
                break;
        }
        return false;
    }

    private void localiseTelephone() {
        LocationResult locationResult = new LocationResult() {
            @Override
            public void gotLocation(Location location) {
                // Méthode appelée lorsque la localisation a fonctionné
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }
        };
    }

    public void completeModele(ArrayList<Flipper> newListeFlipper, Commentaire newCommentaire, String newPseudo) {
        listeFlipper = newListeFlipper;
        commentaire = newCommentaire;
        pseudo = newPseudo;
        // On sauvegarde le pseudo
        Editor editor = settings.edit();
        editor.putString(PreferencesActivity.KEY_PSEUDO_FULL, pseudo);
        editor.apply();
    }

    @OnClick(R.id.prev_button)
    public void previousClickListener(View view) {
        mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == SignalementPagerAdapter.PAGE_COUNT - 1) {
            //mNextButton.setOnClickListener(EnvoyerClickListener);
            mNextButton.setText(R.string.SignalementWizardFinish);
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
            // Hide the keyboard on 3rd view (Geolocalisation)
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            mNextButton.setText(R.string.SignalementWizardNext);
            mNextButton.setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v, true);
            mNextButton.setTextAppearance(this, v.resourceId);
        }
        mPreviousButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    public void envoyer() {
        mprogressBarHandler.show();
        //update Enseigne localisation
        getEnseigne().setLatitude(String.valueOf(newLocation.latitude));
        getEnseigne().setLongitude(String.valueOf(newLocation.longitude));
        Toast toast = Toast.makeText(getApplicationContext(), "Envoi en cours", Toast.LENGTH_SHORT);
        toast.show();

        ParseFactory parseFactory = new ParseFactory();
        //creation d'une liste d'envoi
        ArrayList<ParseObject> objectsToSend = new ArrayList<ParseObject>();

        ParseObject enseignePO = parseFactory.getParseObject(enseigne);
        //objectsToSend.add(parseFactory.getParseObject(getEnseigne()));

        Integer i = 0;
        for (Flipper flipperToAdd : listeFlipper) {
            ParseObject flipPO = parseFactory.getParseObject(flipperToAdd);
            flipPO.put(FlipperDatabaseHandler.FLIPPER_ENS_POINTER,enseignePO);
            String modeleObjectId = new ParseModeleService().getModeleObjectId(flipperToAdd.getIdModele());
            flipPO.put(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER,ParseObject.createWithoutData(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME, modeleObjectId));
            objectsToSend.add(flipPO);

            //on ajoute le commentaire sur le premier flip
            if (i==0) {
                if (getCommentaire() != null) {
                    ParseObject commentairePO = parseFactory.getParseObject(getCommentaire());
                    commentairePO.put(FlipperDatabaseHandler.COMM_FLIP_POINTER,flipPO);
                    objectsToSend.add(commentairePO);
                    i++;
                }
            }
        }

        ParseObject.saveAllInBackground(objectsToSend, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mprogressBarHandler.hide();
                if (e == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Envoi effectué, Merci pour votre contribution.", Toast.LENGTH_LONG);
                    toast.show();
                    updateDBinBackground();
                    finish();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "L'envoi a échoué.", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
    }

    public void updateDBinBackground() {
        if (NetworkUtil.isConnected(getApplicationContext())) {
            if (NetworkUtil.isConnectedFast(getApplicationContext())) {
                new AsyncTaskMajDatabaseBackground(this, settings).execute();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastMajPasPossibleTropLent), Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastMajPasPossible), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public Enseigne getEnseigne() {
        return this.enseigne;
    }

    public void setEnseigne(Enseigne newEnseigne) {
        this.enseigne = newEnseigne;
    }

    public LatLng getCurrentLocation() {
        return this.currentLocation;
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.currentLocation = currentLocation;
    }

    public LatLng getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(LatLng newLocation) {
        this.newLocation = newLocation;
    }

    public long getNewId() {
        return newId;
    }

    public void setNewId(long newId) {
        this.newId = newId;
    }

    public Commentaire getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(Commentaire commentaire) {
        this.commentaire = commentaire;
    }

}
