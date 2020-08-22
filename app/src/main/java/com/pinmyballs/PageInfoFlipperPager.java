package com.pinmyballs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pinmyballs.fragment.FragmentActionsFlipper;
import com.pinmyballs.fragment.FragmentCarteFlipper;
import com.pinmyballs.fragment.FragmentCommentaireFlipper;
import com.pinmyballs.fragment.FragmentDialogScore;
import com.pinmyballs.fragment.FragmentHiScoreFlipper;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.service.FlipperService;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.utils.NetworkUtil;
import com.pinmyballs.utils.SectionsPagerAdapter;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;

public class PageInfoFlipperPager extends AppCompatActivity implements FragmentDialogScore.OnScoreSubmittedListener {

    public final static String INTENT_FLIPPER_ONGLET_DEFAUT = "com.pinmyballs.PageInfoFlipperPager.INTENT_FLIPPER_ONGLET_DEFAUT";
    public final static String INTENT_FLIPPER_POUR_INFO = "com.pinmyballs.PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO";
    ActionBar mActionbar;
    Flipper flipper;
    String nbflippers;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
    private DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
    private SharedPreferences settings;


    //AJOUT INTERFACE TEST
    private FragmentHiScoreFlipper fragmentHiScoreFlipper;


    String pseudo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_flipper);

        setupPage();
        setupUI();
        setupViewPager();
    }

    private void setupPage() {
        // On récupère le flipper concerné
        Intent i = getIntent();
        flipper = (Flipper) i.getSerializableExtra(INTENT_FLIPPER_POUR_INFO);

        // On récupère le nombre de flippers de l'enseigne
        BaseFlipperService baseFlipperService = new BaseFlipperService();
        nbflippers = baseFlipperService.NombreFlipperActifs(getApplicationContext(), flipper.getEnseigne());

        // SharedPreferences & Pseudo
        settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, MODE_PRIVATE);
        pseudo = settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, "");
    }

    private void setupUI() {
        //Widgets
        TextView nbflippercircle = findViewById(R.id.nbflips);
        TextView adresseEnseigne = findViewById(R.id.adresseEnseigne);
        TextView nomEnseigne = findViewById(R.id.nomEnseigne);
        TextView dateMajFlip = findViewById(R.id.dateMajFlip);
        nbflippercircle.setText(nbflippers);
        nomEnseigne.setText(flipper.getEnseigne().getNom());
        adresseEnseigne.setText(flipper.getEnseigne().getAdresseCompleteSansPays());

        //Si la date de mise à jour est nulle, on affiche la valeur par défaut.
        if (flipper.getDateMaj() != null && flipper.getDateMaj().length() != 0) {
            String strdate = null;
            try {
                strdate = df.format(dateFormat.parse(flipper.getDateMaj()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dateMajFlip.setText(getResources().getString(R.string.dateMaj) + " " + strdate);
        } else {
            dateMajFlip.setText(getResources().getString(R.string.dateMajDefault));
        }

        //Title of the Activity
        mActionbar = getSupportActionBar();
        mActionbar.setTitle(flipper.getModele().getNom());
        //mActionbar.setHomeButtonEnabled(true);
        //mActionbar.setDisplayHomeAsUpEnabled(true);
    }


    private void setupViewPager() {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        Bundle bundle = new Bundle();
        bundle.putSerializable("flip",flipper);
        bundle.putString("pseudo", pseudo);
        bundle.putParcelable("flipParceable", Parcels.wrap(flipper));


        FragmentCarteFlipper fragmentCarteFlipper = new FragmentCarteFlipper();
        fragmentCarteFlipper.setArguments(bundle);
        FragmentActionsFlipper fragmentActionsFlipper = new FragmentActionsFlipper();
        fragmentActionsFlipper.setArguments(bundle);
        FragmentCommentaireFlipper fragmentCommentaireFlipper = new FragmentCommentaireFlipper();
        fragmentCommentaireFlipper.setArguments(bundle);
        fragmentHiScoreFlipper = new FragmentHiScoreFlipper();
        fragmentHiScoreFlipper.setArguments(bundle);

        adapter.addFragment(fragmentCarteFlipper);
        adapter.addFragment(fragmentActionsFlipper);
        adapter.addFragment(fragmentCommentaireFlipper);
        adapter.addFragment(fragmentHiScoreFlipper);

        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText(R.string.tab_carte);
        tabLayout.getTabAt(1).setText(R.string.tab_actions);
        tabLayout.getTabAt(2).setText(R.string.tab_avis);
        tabLayout.getTabAt(3).setText(R.string.tab_hiscore);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_page_info_flipper, menu);
        MenuItem item = menu.findItem(R.id.action_delete);
        boolean adminMode = settings.getBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, PreferencesActivity.DEFAULT_VALUE_ADMIN_MODE);
        item.setVisible(adminMode);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Infos Flipper").setMessage(String.valueOf(flipper.getModele().getNomComplet())).show();
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("FlipID", String.valueOf(flipper.getId()));
                clipboard.setPrimaryClip(clip);
                break;
            case R.id.action_delete:
                //On vérifie qu'on a la connection
                if (NetworkUtil.isConnected(getApplicationContext())) {
                    FlipperService flipperService = new FlipperService(new FragmentActionsFlipper.FragmentActionCallback() {
                        @Override
                        public void onTaskDone() {
                            //finish();  uncomment pour fermer la fenetre
                        }
                    });
                    //On vérifie que l'état du flip a été changé
                    //On modifie l'état du flip dans la base et online
                    flipperService.modifieEtatFlip(PageInfoFlipperPager.this, flipper);

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastChangeModelePasPossibleReseau), LENGTH_SHORT);
                    toast.show();
                }
                break;

            default:
                Log.i("Erreur action bar", "default");
                break;
        }
        return false;
    }


    //AJOUT INTERFACE TEST
    @Override
    public void updateList(Score score) {
        fragmentHiScoreFlipper.receivedScore(score);

    }
}
