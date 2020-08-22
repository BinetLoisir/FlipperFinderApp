package com.pinmyballs;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.utils.ModelAdapter;

import java.util.ArrayList;
import java.util.Collections;

public class ListeModelsActivity extends AppCompatActivity {

    private static final String TAG = "ListeModelsActivity";
    ActionBar mActionbar;
    private ModelAdapter modelAdapter;
    private ArrayList<ModeleFlipper> listModels;
    private boolean sortedAZ;
    private boolean sortedChrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_models);
        initActionBar();
        initView();
        initFAB();
        initFAB2();
    }

    private void initActionBar() {
        mActionbar = getSupportActionBar();
        mActionbar.setTitle(getString(R.string.liste_modeles));
        mActionbar.setHomeButtonEnabled(true);
        mActionbar.setDisplayHomeAsUpEnabled(true);
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        listModels = new ArrayList<>();
        listModels.addAll(new BaseModeleService().getAllModeleFlipper(getApplicationContext()));
        Collections.sort(listModels, (m1, m2) -> m1.getNom().compareTo(m2.getNom()));
        sortedAZ = true;

        modelAdapter = new ModelAdapter(this, listModels);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(modelAdapter);

    }

    private void initFAB() {
        FloatingActionButton fab = findViewById(R.id.fabmodel);
        fab.setOnClickListener(view -> {
            Intent intentNewModel = new Intent(this, PopNewModel.class);
            startActivity(intentNewModel);
        });
        //Hide or show FAB
        SharedPreferences settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
        boolean adminMode = settings.getBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, PreferencesActivity.DEFAULT_VALUE_ADMIN_MODE);
        if (adminMode) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    private void initFAB2() {
        FloatingActionButton fab = findViewById(R.id.fabmodel2);
        fab.setOnClickListener(view -> {
            Log.d(TAG, "onMenuItemClick: Launching email for new machine");
            Resources resources = getApplicationContext().getResources();
            String emailsTo = resources.getString(R.string.mailContact);
            String emailSubject = resources.getString(R.string.mail_subject_new_model);
            Intent intent2 = new Intent(Intent.ACTION_SEND);
            intent2.setType("message/html");
            intent2.putExtra(Intent.EXTRA_EMAIL, new String[]{emailsTo});
            intent2.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            try {
                startActivity(Intent.createChooser(intent2, "Envoi du mail"));
            } catch (android.content.ActivityNotFoundException ex) {
                new AlertDialog.Builder(ListeModelsActivity.this).setTitle("Envoi impossible!").setMessage("Vous n'avez pas de mail configuré sur votre téléphone.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_tristesse).show();
            }
        });
        //Hide or show FAB
        SharedPreferences settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
        boolean adminMode = settings.getBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, PreferencesActivity.DEFAULT_VALUE_ADMIN_MODE);
        if (adminMode) {
            fab.hide();
        } else {
            fab.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_liste_models, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                modelAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sortAZ:
                if (sortedAZ) {
                    Collections.sort(listModels, (m1, m2) -> m2.getNom().compareTo(m1.getNom()));
                    sortedAZ = false;
                } else {
                    Collections.sort(listModels, (m1, m2) -> m1.getNom().compareTo(m2.getNom()));
                    sortedAZ = true;
                }
                modelAdapter.notifyDataSetChanged();
                break;
            case R.id.action_sortChrono:
                if (sortedChrono) {
                    Collections.sort(listModels, (m1, m2) -> String.valueOf(m2.getAnneeLancement()).compareTo(String.valueOf(m1.getAnneeLancement())));
                    sortedChrono = false;
                } else {
                    Collections.sort(listModels, (m1, m2) -> String.valueOf(m1.getAnneeLancement()).compareTo(String.valueOf(m2.getAnneeLancement())));
                    sortedChrono = true;
                }
                modelAdapter.notifyDataSetChanged();
                break;
            default:
                Log.i("Erreur action bar", "default");
                break;
        }
        return false;
    }
}


