package com.pinmyballs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.pinmyballs.fragment.FragmentActionsFlipper;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.FlipperService;
import com.pinmyballs.service.GlobalService;
import com.pinmyballs.utils.NetworkUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.widget.Toast.LENGTH_SHORT;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = AdminActivity.class.getSimpleName();

    @BindView(R.id.searchByNumberEditText)
    TextView searchBynumberInput;
    @BindView(R.id.boutonClearNumber)
    ImageButton clearNumberButton;
    @BindView(R.id.pasteClipboardImagebutton)
    ImageButton PasteClipBoardImageButton;
    @BindView(R.id.searchbynumberImagebutton)
    ImageButton searchBynumberImageButton;
    @BindView(R.id.saveButton)
    Button saveButton;
    @BindView(R.id.actifToggle)
    SwitchCompat actifToggle;
    @BindView(R.id.actifState)
    TextView actifState;
    @BindView(R.id.searchbynumberResultFlipID)
    TextView R_flipID;
    @BindView(R.id.searchbynumberResultFlipEnseigneID)
    TextView R_flipEnseigneId;
    @BindView(R.id.searchbynumberResultFlipEnseigne)
    TextView R_flipEnseigne;
    @BindView(R.id.searchbynumberResultFlipEnseigneAdresse)
    TextView R_flipEnseigneAdresse;
    @BindView(R.id.searchbynumberResultFlipModeleID)
    TextView R_flipModeleId;
    @BindView(R.id.searchbynumberResultFlipModele)
    TextView R_flipModele;
    @BindView(R.id.MyAction)
    Button myAction;
    @BindView(R.id.MyAction2)
    Button MyAction2;

    Flipper flipper;

    ActionBar mActionbar;
    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        ButterKnife.bind(this);

        settings = getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);

        // Affichage du header
        mActionbar = getSupportActionBar();
        mActionbar.setTitle(R.string.headerAdmin);

        actifToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //On passe le flip Inactif
                    actifState.setText("Now Actif");
                } else {
                    actifState.setText("Now Inactif");
                }
            }

        });

        R_flipID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Flipper p = flipper;
                if(p!= null) {
                    Intent infoActivite = new Intent(getApplicationContext(), PageInfoFlipperPager.class);
                    infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO, p);
                    infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_ONGLET_DEFAUT, 0);
                    getApplicationContext().startActivity(infoActivite);
                }
            }
        });


        //Rating exemple
        /*myRatingBar.setRating(1);
        myRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingBar.setRating(1);
            }
        });*/


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admintools, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences:
                ////EasyTracker.getTracker().sendEvent("ui_action", "button_press", "preferences", 0L);
                Intent intent4 = new Intent(AdminActivity.this, PreferencesActivity.class);
                startActivity(intent4);
                break;


            default:
                Log.i("Erreur action bar", "default");
                break;
        }
        return false;
    }

    @OnClick(R.id.pasteClipboardImagebutton)
    public void paste() {
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = clipboardManager.getPrimaryClip();
        // Get item count.
        if (clipData != null) {
            int itemCount = clipData.getItemCount();
            if (itemCount > 0) {
                // Get source text.
                ClipData.Item item = clipData.getItemAt(0);
                String text = item.getText().toString();
                // Set the text to target textview.
                searchBynumberInput.setText(text);
                searchbynumber();
            }
        }
    }




    @OnClick(R.id.searchbynumberImagebutton)
    public void searchbynumber() {

        String inputString = searchBynumberInput.getText().toString();

        if (searchBynumberInput.getText().length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Entrer un numéro", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        try {
            long flipnumberInput = Long.parseLong(inputString.trim());
            Flipper flip;
            Enseigne flipEnseigne;
            String flipModele;
            GlobalService globalService = new GlobalService();
            flip = globalService.getFlip(getApplicationContext(), flipnumberInput);
            if (flip != null) {
                flipper = flip;
                flipEnseigne = flip.getEnseigne();
                flipModele = flip.getModele().getNom();
                Toast toast = Toast.makeText(getApplicationContext(), "Flip trouvé", Toast.LENGTH_SHORT);
                toast.show();

                R_flipID.setText(Long.toString(flip.getId()));
                R_flipEnseigneId.setText(Long.toString(flip.getIdEnseigne()));
                R_flipEnseigne.setText(flipEnseigne.getNom());
                R_flipEnseigneAdresse.setText(flipEnseigne.getAdresseCompleteAvecPays());
                R_flipModeleId.setText(Long.toString(flip.getIdModele()));
                R_flipModele.setText(flipModele);
                actifToggle.setChecked(flip.isActif());
                if (flip.isActif()) {
                    actifState.setText("Actif");
                } else {
                    actifState.setText("Inactif");
                }
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Flip non trouvé", LENGTH_SHORT);
                toast.show();
                Log.d(TAG, "searchbynumber: Flip non trouvé");

            }

        } catch (NumberFormatException e) {
            Log.i("", inputString + " is not a number " + e);

        }


    }

    @OnClick(R.id.saveButton)
    public void saveState() {
        String flipflop = searchBynumberInput.getText().toString();
        Boolean flipactifinDB;
        Flipper flip;
        GlobalService globalService = new GlobalService();
        flip = globalService.getFlip(getApplicationContext(), Long.parseLong(flipflop));
        flipactifinDB = flip.isActif();

        //On vérifie qu'on a la connection
        if (NetworkUtil.isConnected(getApplicationContext())) {
            FlipperService flipperService = new FlipperService(new FragmentActionsFlipper.FragmentActionCallback() {
                @Override
                public void onTaskDone() {
                    //finish();  uncomment pour fermer la fenetre
                }
            });
            //On vérifie que l'état du flip a été changé
            if (!actifToggle.isChecked() == flipactifinDB) {
                //On modifie l'état du flip dans la base et online
                flipperService.modifieEtatFlip(this, flip);

            } else {
                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastPasdeChangement), LENGTH_SHORT);
                toast.show();
            }

        } else {
            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.toastChangeModelePasPossibleReseau), LENGTH_SHORT);
            toast.show();
        }
    }


    @OnClick(R.id.boutonClearNumber)
    public void clearNumber() {
        searchBynumberInput.setText("");
    }

    @OnClick(R.id.MyAction2)
    protected void NewModel() {

    }

    @OnClick(R.id.MyAction)
    protected void MyAction() {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);


        /*ParseObject parseObject;
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        try {
            query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ID, Long.parseLong("1535701556570"));
            parseObject = query.getFirst();
            Flipper flip = new ParseFactory().getFlipper(parseObject);
            Log.d("Trouvé","Flip"+ flip.getId());
        } catch (ParseException e1) {
            e1.printStackTrace();
        }*/


        /*
        BaseFlipperService baseFlipperService = new BaseFlipperService();
        BaseModeleService baseModeleService = new BaseModeleService();
        Flipper flipper = baseFlipperService.getFlipperById(getApplicationContext(), Long.parseLong( "1521283874065"));
        Flipper newflipper = flipper;
        newflipper.setId(Long.parseLong("999909999"));
        ModeleFlipper playboy = baseModeleService.getModeleById(getApplicationContext(),Long.parseLong( "69"));
        newflipper.setModele(playboy);

        ParseFactory parseFactory = new ParseFactory();
        //creation d'une liste d'envoi
        ArrayList<ParseObject> objectsToSend = new ArrayList<ParseObject>();

        // On créé l'objet du nouveau flipper et on l'ajoute à la liste d'envoi
        //objectsToSend.add(parseFactory.getParseObjectWithPointers(flipper));
        //objectsToSend.add(parseFactory.getParseObjectWithModel(newflipper));

        ParseObject.saveAllInBackground(objectsToSend, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast toast = Toast.makeText(getApplicationContext(), "Envoi effectué, Merci pour votre contribution :)", Toast.LENGTH_LONG);
                toast.show();
            }
        });

    */


    }


}