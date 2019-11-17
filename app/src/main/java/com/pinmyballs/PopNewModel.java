package com.pinmyballs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.service.ModeleService;
import com.pinmyballs.service.parse.ParseModeleService;

public class PopNewModel extends AppCompatActivity {

    private static final String TAG = "PopNewModel";
    ProgressBar progressBar;
    TextInputEditText ET_Model;
    TextInputEditText ET_Brand;
    TextInputEditText ET_Year;
    Button Submit;
    Button Cancel;

    String Model, Brand;
    Long Year;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.pop_newmodel);
        progressBar = findViewById(R.id.progressbarPop);
        ET_Model = findViewById(R.id.ET_model);
        ET_Brand = findViewById(R.id.ET_brand);
        ET_Year = findViewById(R.id.ET_year);
        Submit = findViewById(R.id.btn_submit);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (allFieldsFilled()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    Submit.setEnabled(false);
                    submitNewModel();
                }
            }
        });
        Cancel = findViewById(R.id.btn_cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Adjust Popup size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.5));

    }

    private void submitNewModel() {
        progressBar.setVisibility(View.VISIBLE);
        ModeleService modeleService = new ModeleService();
        ParseModeleService parseModeleService = new ParseModeleService();
        Long maxId;
        maxId = modeleService.getMaxIdModeleFlipper(getApplicationContext());
        if (maxId == 0) {
            progressBar.setVisibility(View.INVISIBLE);
            Submit.setEnabled(true);
            new AlertDialog.Builder(this).setTitle("Erreur").setMessage("Max Id nulle").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            return;
        }
        String modelObjectId = parseModeleService.getModeleObjectId(maxId + 1);
        if (modelObjectId != null) {
            progressBar.setVisibility(View.INVISIBLE);
            Submit.setEnabled(true);
            new AlertDialog.Builder(this).setTitle("Erreur!").setMessage("Id " + maxId + " déjà prise, refresh local database").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            return;
        } else {
            ModeleFlipper modeleFlipper = new ModeleFlipper(maxId + 1, Model, Brand, Year);
            modeleService.ajouteModele(getApplicationContext(), modeleFlipper);
            progressBar.setVisibility(View.INVISIBLE);
            finish();
        }
    }

    private boolean allFieldsFilled() {
        boolean isError = false;
        if (ET_Model.getText().length() == 0) {
            new AlertDialog.Builder(this).setTitle("Envoi impossible!").setMessage("Vous devez renseigner le modèle du flipper.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
            isError = true;
        } else {
            Model = ET_Model.getText().toString();
            if (ET_Brand.getText().length() == 0) {
                new AlertDialog.Builder(this).setTitle("Envoi impossible!").setMessage("Vous devez renseigner la marque du modèle de flipper.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
                isError = true;
            } else {
                Brand = ET_Brand.getText().toString();
                if (ET_Year.getText().length() == 0) {
                    new AlertDialog.Builder(this).setTitle("Envoi impossible!").setMessage("Vous devez renseigner l'année de sortie du modèle.").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
                    isError = true;
                } else
                    Year = Long.parseLong(ET_Year.getText().toString());
            }
        }
        return !isError;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }
}





