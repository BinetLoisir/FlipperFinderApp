package com.pinmyballs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.metier.ModeleOPDB;
import com.pinmyballs.service.ModeleService;
import com.pinmyballs.service.parse.ParseModeleService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PopNewModel2 extends AppCompatActivity {

    private static final String TAG = "PopNewModel2";

    private static final String[] MARQUES = new String[]{
            "Stern",
            "Bally",
            "Williams",
            "Gottlieb",
            "Jersey Jack",
            "Spooky Pinball",
            "Heighway Pinball",
            "Sega",
            "Team Pinball",
            "Suncoast Pinball",
            "Chicago Gaming",
            "Data East",
            "Capcom",
            "American Pinball",
            "Alvin G."
    };


    ProgressBar progressBar;
    TextInputEditText ET_Model;
    AutoCompleteTextView ET_Brand;
    TextInputEditText ET_Year;
    Button Search;
    Button Submit;
    Button Cancel;
    ListView LV_Results;
    String Model, Brand;
    Long Year;
    private String modelName;
    private String brandName;
    private String yearManufactured;
    private ArrayList<ModeleOPDB> listOPDB;
    private RequestQueue mQueue;

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
        setContentView(R.layout.pop_newmodel2);
        progressBar = findViewById(R.id.progressbarPop);
        ET_Model = findViewById(R.id.ET_model);
        ET_Brand = findViewById(R.id.ET_brand);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, MARQUES);
        ET_Brand.setAdapter(adapter);

        ET_Year = findViewById(R.id.ET_year);
        Search = findViewById(R.id.btn_searchOPDB);
        Search.setOnClickListener(v -> loadlist());
        LV_Results = findViewById(R.id.search_results);
        mQueue = Volley.newRequestQueue(this);


        Submit = findViewById(R.id.btn_submit);
        Submit.setOnClickListener(v -> {
            if (allFieldsFilled()) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                Submit.setEnabled(false);
                submitNewModel();
            }
        });
        Cancel = findViewById(R.id.btn_cancel);
        Cancel.setOnClickListener(v -> finish());

        //Adjust Popup size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.9), (int) (height * 0.8));

    }

    private void loadlist(){
        ET_Brand.setText("");
        ET_Year.setText("");

        listOPDB = searchOPDB(ET_Model.getText().toString());
        final ModelOPDBArrayAdapter adapter= new ModelOPDBArrayAdapter(this, R.layout.simple_list_item_modele_opdb, listOPDB);
        LV_Results.setAdapter(adapter);
        LV_Results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ModeleOPDB item = (ModeleOPDB) parent.getItemAtPosition(position);
                populate(item);
            }
        });
        
    }

    private void submitNewModel() {
        progressBar.setVisibility(View.VISIBLE);
        ModeleService modeleService = new ModeleService();
        ParseModeleService parseModeleService = new ParseModeleService();
        long maxId;
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


    private ArrayList<ModeleOPDB> searchOPDB(String queryString) {
        ArrayList<ModeleOPDB> listeRetour = new ArrayList<ModeleOPDB>();
        //String queryString = ET_Model.getText().toString();
        if (!queryString.isEmpty()) {
            //Build URL query
            String api_token = "IuBO3tLKv5giXQ3OqR5BHogsQVAEfgN2kXEORqtLz8p4bZMcrKn65Y3PUKx1";
            String endpoint = "https://opdb.org/api/search";
            Uri.Builder builder = new Uri.Builder();
            builder.encodedPath(endpoint);
            builder.appendQueryParameter("api_token", api_token);
            builder.appendQueryParameter("q", queryString);
            //Whether to only search machines with OPDB ids. Defaults to 1 (limit searches to machines with OPDB ids)
            builder.appendQueryParameter("require_opdb", "1");
            //Set to 1 to search groups. Defaults to 0 (don't search groups)
            builder.appendQueryParameter("include_groups", "0");
            //Set to 0 to avoid searching aliases. Defaults to 1 (do search aliases)
            builder.appendQueryParameter("include_aliases", "0");
            //Set to 1 to searching grouping entries that do not represent physical machines. Defaults to 0 (don't search grouping entries)
            builder.appendQueryParameter("include_grouping_entries", "0");

            String url = builder.build().toString();
            Log.d(TAG, "jsonParse url : " + url);

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject pin = response.getJSONObject(i);
                                    String opdb_id = pin.getString("opdb_id");
                                    String name = pin.getString("name");
                                    String manufacturer = pin.getJSONObject("manufacturer").getString("name");
                                    Log.d(TAG, "searchOPDB: year "+pin.getString("manufacture_date"));
                                    String year = pin.getString("manufacture_date") == "" ? "" : pin.getString("manufacture_date").substring(0, 4);

                                    listeRetour.add(new ModeleOPDB(opdb_id, name, manufacturer, year));
                                    //TV_Results.append(opdb_id + "\n" + name + ", " + manufacturer + " (" + year + ")" + "\n");

                                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                    inputMethodManager.hideSoftInputFromWindow(Search.getApplicationWindowToken(), 0);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    },
                    Throwable::printStackTrace);

            mQueue.add(request);
        }
        return listeRetour;
    }

    private void populate(ModeleOPDB modeleOPDB) {
        if (modeleOPDB != null) {
            ET_Model.setText(modeleOPDB.getModel());
            ET_Brand.setText(modeleOPDB.getManufacturer());
            ET_Year.setText(modeleOPDB.getYear());
        }
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


    private static class ModelOPDBArrayAdapter extends ArrayAdapter<ModeleOPDB> {
        private final List<ModeleOPDB> list;

        public ModelOPDBArrayAdapter(Context context, int textViewResourceId, List<ModeleOPDB> items){
            super(context,textViewResourceId,items);
            this.list = items;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            //return super.getView(position, convertView, parent);
            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.simple_list_item_modele_opdb, null);
            }

            // On set les tags pour pouvoir retrouver sur quelle ligne on a cliqué.
            //v.setTag(position);
            //v.setOnClickListener(InfoFlipperClickListener);

            ModeleOPDB modeleOPDB = list.get(position);

            if (modeleOPDB != null) {
                TextView modeleTV = v.findViewById(R.id.opdb_model);
                TextView manufacturerTV = v.findViewById(R.id.opdb_manufacturer);
                TextView yearTV = v.findViewById(R.id.opdb_year);


                if (modeleTV != null) {
                    modeleTV.setText(modeleOPDB.getModel());
                }
                if (manufacturerTV != null) {
                    manufacturerTV.setText(modeleOPDB.getManufacturer());
                }
                if (yearTV != null) {
                    yearTV.setText(modeleOPDB.getYear());
                }
            }

            return v;
        }
    }
}




