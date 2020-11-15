package com.pinmyballs.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.parse.ParseObject;
import com.pinmyballs.AdminActivity;
import com.pinmyballs.PageInfoFlipperPager;
import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.service.FlipperService;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.service.parse.ParseFlipperService;
import com.pinmyballs.utils.AsyncTaskMajDatabaseBackground;
import com.pinmyballs.utils.LocationUtil;
import com.pinmyballs.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class FragmentActionsFlipper extends Fragment {
    private static final String TAG = AdminActivity.class.getSimpleName();
    private Boolean ajoutNouveauFlip = false;

    private Button boutonValideChangement;
    private Button boutonAnnuleChangement;
    private EditText pseudoET;
    private EditText commentaire;
    private AutoCompleteTextView champNouveauModeleFlipper;

    private String pseudo = "";
    private Flipper flipper;

    private ScrollView changeModeleLayout;
    private BaseModeleService modeleFlipperService;

    private HashMap hashMapModeles;
    private OnItemClickListener itemSelectionneNouveauModeleListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(champNouveauModeleFlipper.getWindowToken(), 0);
        }
    };
    private OnClickListener ChangerModeleListener = new OnClickListener() {
        public void onClick(View v) {
            boutonValideChangement.setEnabled(true);
            boutonValideChangement.setText(R.string.boutonValideChangementModele);
            ajoutNouveauFlip = false;
            commentaire.setText(getString(R.string.changement_default_comment, flipper.getModele().getNom()));
            Animation slide = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_up);
            changeModeleLayout.setVisibility(View.VISIBLE);
            changeModeleLayout.startAnimation(slide);
        }
    };
    private DialogInterface.OnClickListener ChangerModeleParMailListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String message = "ID : " + flipper.getId() + "\nEnseigne : " + flipper.getEnseigne().getId()
                    + "\nAncien Modèle :" + flipper.getModele().getNom() + "\nNouveau Modèle : " + champNouveauModeleFlipper.getText().toString();
            envoiMail("Changement du flipper " + flipper.getId(), message);
        }
    };
    private final OnClickListener ValideChangementListener = new OnClickListener() {
        public void onClick(View v) {
            Context context = getActivity().getApplicationContext();
            boutonValideChangement.setEnabled(false);
            SharedPreferences settings = getActivity().getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, MODE_PRIVATE);

            if (champNouveauModeleFlipper.getText().length() != 0) {
                //ModeleFlipper modeleChoisi = modeleFlipperService.getModeleFlipperByName(getActivity().getApplicationContext(),champNouveauModeleFlipper.getText().toString());
                ModeleFlipper modeleChoisi = null;
                try {
                    modeleChoisi = modeleFlipperService.getModeleById(context, Long.parseLong(String.valueOf(hashMapModeles.get(champNouveauModeleFlipper.getText().toString()))));
                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                }

                if (modeleChoisi != null) {
                    if (modeleChoisi.getId() != flipper.getModele().getId()) {
                        if (NetworkUtil.isConnected(getActivity().getApplicationContext())) {
                            FlipperService flipperService = new FlipperService(new FragmentActionCallback() {
                                @Override
                                public void onTaskDone() {
                                    if (!ajoutNouveauFlip) {
                                        getActivity().finish();
                                    }
                                    new AsyncTaskMajDatabaseBackground((AppCompatActivity) getActivity(), settings).execute();
                                }
                            });
                            String commentaireString = ajoutNouveauFlip ? "Nouveau" : "Changement";
                            String pseudoCommentaire = getResources().getString(R.string.pseudoCommentaireAnonyme);
                            if (commentaire.getText().length() > 0) {
                                // On sauvegarde le pseudoET
                                Editor editor = settings.edit();
                                editor.putString(PreferencesActivity.KEY_PSEUDO_FULL, pseudoET.getText().toString());
                                editor.apply();
                                pseudoCommentaire = getResources().getString(R.string.pseudoCommentaireAnonyme);
                                if (pseudoET.getText().length() > 0) {
                                    pseudoCommentaire = pseudoET.getText().toString();
                                }
                                commentaireString = Html.toHtml(commentaire.getText());
                            }
                            if (ajoutNouveauFlip) {
                                //Ajout d'un flipper supplémentaire dans le bar
                                flipperService.rajouterFlipperSurFlipper(getActivity(), flipper, modeleChoisi.getId(), commentaireString, pseudoCommentaire);
                                boutonAnnuleChangement.performClick();
                                ajoutNouveauFlip = true;
                                champNouveauModeleFlipper.setText("");
                                boutonValideChangement.setEnabled(true);


                            } else {
                                //Changement du modèle
                                flipperService.remplaceFlipper(getActivity(), flipper, modeleChoisi.getId(), commentaireString, pseudoCommentaire);
                            }
                        } else {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toastChangeModelePasPossibleReseau), Toast.LENGTH_SHORT);
                            toast.show();
                            boutonValideChangement.setEnabled(true);

                        }
                    } else {
                        new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Le modèle est identique, pas la peine de le changer !").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
                        boutonValideChangement.setEnabled(true);
                        boutonAnnuleChangement.performClick();


                    }
                } else {
                    new AlertDialog.Builder(getActivity()).setTitle("Envoi par mail").setMessage("Le modèle que vous avez renseigné est inconnu. Votre notification sera traitée manuellement par mail.").setNeutralButton("OK", ChangerModeleParMailListener).show();
                    boutonValideChangement.setEnabled(true);
                    boutonAnnuleChangement.performClick();

                }
            } else {
                new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Vous n'avez pas rempli de nouveau modèle !").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
                boutonValideChangement.setEnabled(true);

            }
        }
    };
    private OnClickListener NouveauFlipListener = new OnClickListener() {
        public void onClick(View v) {
            boutonValideChangement.setEnabled(true);
            boutonValideChangement.setText(R.string.boutonValideNouveauFlipper);

            ajoutNouveauFlip = true;
            commentaire.setText(getString(R.string.ajout_default_comment));
            Animation slide = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_up);
            changeModeleLayout.setVisibility(View.VISIBLE);
            changeModeleLayout.startAnimation(slide);
        }
    };

    private OnClickListener AnnuleChangementModeleListener = v -> {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        changeModeleLayout.setVisibility(View.GONE);
    };
    private OnClickListener ValidationListener = new OnClickListener() {
        public void onClick(View v) {
            if (NetworkUtil.isConnected(getActivity().getApplicationContext())) {
                FlipperService flipperService = new FlipperService(new FragmentActionCallback() {
                    @Override
                    public void onTaskDone() {
                        //do something
                    }
                });
                flipperService.valideFlipper(getActivity(), flipper);
            } else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toastValidationPasPossibleReseau), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };

    private OnClickListener DisparitionListener = new OnClickListener() {
        public void onClick(View v) {
            //Envoi mail
            String message = "Le " + flipper.getModele().getNom()
                    + "\nID : " + flipper.getId()
                    + "\nAu : " + flipper.getEnseigne().getNom()
                    + "\nsitué : " + flipper.getEnseigne().getAdresseCompleteSansPays()
                    + "\n n'existe plus."
                    + "\n Dernière maj :" + flipper.getDateMaj()
                    + "\n----------Commentaire éventuel----------"
                    + "\n"
                    + "\n"
                    + "\n"

                    + "------------------------------------------";

            envoiMail("Retrait d'un flipper à " + flipper.getEnseigne().getVille(), message);

            //Ajout TrashList
            SharedPreferences settings = getActivity().getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
            String pseudo = settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, "___");

            ParseObject flipperPO = new ParseObject(FlipperDatabaseHandler.FLIPTRASH_TABLE_NAME);
            flipperPO.put(FlipperDatabaseHandler.FLIPTRASH_FLIP_ID,flipper.getId());
            flipperPO.put(FlipperDatabaseHandler.FLIPTRASH_PSEUDO,pseudo);
            flipperPO.put(FlipperDatabaseHandler.FLIPTRASH_PROCESSED,false);
            flipperPO.saveInBackground();
        }
    };

    //TODO Include this as a button on the FragmentCarteFlipper
    private OnClickListener NavigationListener = new OnClickListener() {
        public void onClick(View v) {
            Intent navIntentGoogleNav = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="
                    + flipper.getEnseigne().getAdresseCompleteSansPays()));

            Intent navIntentWaze = new Intent(Intent.ACTION_VIEW, Uri.parse("waze://?q="
                    + flipper.getEnseigne().getAdresseCompleteSansPays()));

            if (LocationUtil.canHandleIntent(getActivity().getApplicationContext(), navIntentGoogleNav)) {
                navIntentGoogleNav.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(navIntentGoogleNav);
            } else if (LocationUtil.canHandleIntent(getActivity().getApplicationContext(), navIntentWaze)) {
                navIntentGoogleNav.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(navIntentWaze);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Merci d'installer Google Navigation ou Waze", Toast.LENGTH_SHORT).show();
            }

        }
    };
    private OnClickListener ExploitantListener = new OnClickListener() {
        String m_Text;
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dial_title_exploitant);

            // Set up the input
            final EditText input = new EditText(getActivity());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            //Bring up the keyboard when opening
            input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    input.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager= (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            input.requestFocus();

            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text = input.getText().toString();
                    Log.d(TAG, "onClick: nouvel Exploitant" + m_Text );
                    flipper.setExploitant(m_Text);
                    ParseFlipperService parseFlipperService = new ParseFlipperService(null);
                    parseFlipperService.updateInfoFlipper(getActivity(),flipper);

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();


        }
    };
    private OnClickListener CreditsListener = new OnClickListener() {
        String m_Text;
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dial_title_prix);

            // Set up the input
            final EditText input = new EditText(getActivity());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            //Bring up the keyboard when opening
            input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    input.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager= (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            input.requestFocus();
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text = input.getText().toString();
                    Log.d(TAG, "onClick: nouveau prix" + m_Text);
                    flipper.setNbCreditsDeuxEuros(m_Text);
                    ParseFlipperService parseFlipperService = new ParseFlipperService(null);
                    parseFlipperService.updateInfoFlipper(getActivity(),flipper);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();


        }
    };

    public FragmentActionsFlipper() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_action_flipper, container, false);
        super.onCreate(savedInstanceState);

        flipper = (Flipper) getArguments().getSerializable("flip");
        pseudo = getArguments().getString("pseudo");

        setupUI(rootView);

        return rootView;

    }

    private void setupUI(View rootView) {
        Button boutonValidation = rootView.findViewById(R.id.boutonValidation);
        Button boutonChangement = rootView.findViewById(R.id.boutonChangement);
        Button boutonDisparition = rootView.findViewById(R.id.boutonDisparition);
        Button boutonNavigation = rootView.findViewById(R.id.boutonNavigation);
        Button boutonNouveauFlip = rootView.findViewById(R.id.boutonNouveauFlip);
        Button boutonExploitant = rootView.findViewById(R.id.boutonExploitant);
        Button boutonCredits = rootView.findViewById(R.id.boutonCredits);

        champNouveauModeleFlipper = rootView.findViewById(R.id.autocompletionNouveauModeleFlipper);
        pseudoET = rootView.findViewById(R.id.champPseudo);
        commentaire = rootView.findViewById(R.id.texteCommentaire);
        changeModeleLayout = rootView.findViewById(R.id.layoutChangeModele);
        boutonValideChangement = rootView.findViewById(R.id.boutonValideChangementModele);
        boutonAnnuleChangement = rootView.findViewById(R.id.boutonCancelChangeModele);

        boutonChangement.setOnClickListener(ChangerModeleListener);
        boutonDisparition.setOnClickListener(DisparitionListener);
        boutonValidation.setOnClickListener(ValidationListener);
        boutonNavigation.setOnClickListener(NavigationListener);
        boutonNouveauFlip.setOnClickListener(NouveauFlipListener);
        boutonExploitant.setOnClickListener(ExploitantListener);
        boutonCredits.setOnClickListener(CreditsListener);


        boutonAnnuleChangement.setOnClickListener(AnnuleChangementModeleListener);
        boutonValideChangement.setOnClickListener(ValideChangementListener);

        setupAutocomplete();
        champNouveauModeleFlipper.setOnItemClickListener(itemSelectionneNouveauModeleListener);

        //Préremplit le champ pseudo si besoin
        if (pseudo != null && !pseudo.isEmpty()) pseudoET.setText(pseudo);

        // On cache le layout qui va servir à renseigner un nouveau modèle
        changeModeleLayout.setVisibility(View.GONE);

    }

    private void setupAutocomplete() {
        //iniatilisation des listes
        ArrayList<String> listeModelesComplet = new ArrayList<>();
        hashMapModeles = new HashMap();

        // Initialisation du champ Modele
        modeleFlipperService = new BaseModeleService();
        ArrayList<ModeleFlipper> listModeleFlipper = new BaseModeleService().getAllModeleFlipper(getActivity());

        for (ModeleFlipper modele : listModeleFlipper) {
            String NomComplet = modele.getNomComplet();
            listeModelesComplet.add(NomComplet);
            hashMapModeles.put(NomComplet, modele.getId());
        }

        // Prépare la liste d'autocomplétion pour les modèle de flipper
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, listeModelesComplet);
        champNouveauModeleFlipper.setAdapter(adapter);
        champNouveauModeleFlipper.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    private void envoiMail(String subject, String message) {
        Resources resources = getContext().getResources();
        String emailsTo = resources.getString(R.string.mailContact);

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/html");

        i.putExtra(Intent.EXTRA_EMAIL, new String[]{emailsTo});
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, message);
        try {
            startActivity(Intent.createChooser(i, "Envoi du mail"));
        } catch (android.content.ActivityNotFoundException ex) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!")
                    .setMessage("Vous n'avez pas de mail configuré sur votre téléphone.")
                    .setNeutralButton("Fermer", null).setIcon(R.drawable.ic_tristesse).show();
        }
    }

    public interface FragmentActionCallback {
        void onTaskDone();
    }

}
