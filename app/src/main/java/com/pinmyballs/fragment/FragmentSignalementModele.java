package com.pinmyballs.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.service.base.BaseModeleService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;




public class FragmentSignalementModele extends SignalementWizardFragment {


    EditText champPseudo;

    EditText champCommentaire;

    AutoCompleteTextView champModeleFlipper;

    AutoCompleteTextView champModeleDeuxiemeFlipper;

    AutoCompleteTextView champModeleTroisiemeFlipper;

    AutoCompleteTextView champModeleQuatriemeFlipper;

    AutoCompleteTextView champModeleCinquiemeFlipper;

    EditText champExploitant;

    EditText champNbCredits;

    ModeleFlipper modeleFlipper, modeleFlipper2, modeleFlipper3, modeleFlipper4, modeleFlipper5;

    HashMap hashMapModeles;
    ArrayList<String> listeModelesComplet;

    SharedPreferences settings;
    BaseModeleService modeleFlipperService;

    private final OnItemClickListener itemSelectionneListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(arg1.getWindowToken(), 0);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wizard_modele, container, false);
        champPseudo = (EditText) rootView.findViewById(R.id.champPseudo);
        champCommentaire = (EditText) rootView.findViewById(R.id.texteCommentaire);
        champModeleFlipper = (AutoCompleteTextView) rootView.findViewById(R.id.autocompletionModeleFlipper);
        champModeleDeuxiemeFlipper = (AutoCompleteTextView) rootView.findViewById(R.id.autocompletionModeleFlipper2);
        champModeleTroisiemeFlipper = (AutoCompleteTextView) rootView.findViewById(R.id.autocompletionModeleFlipper3);
        champModeleQuatriemeFlipper = (AutoCompleteTextView) rootView.findViewById(R.id.autocompletionModeleFlipper4);
        champModeleCinquiemeFlipper = (AutoCompleteTextView) rootView.findViewById(R.id.autocompletionModeleFlipper5);
        champExploitant = (EditText) rootView.findViewById(R.id.champExploitant);
        champNbCredits = (EditText) rootView.findViewById(R.id.champNbCredits);

        super.onCreate(savedInstanceState);

        modeleFlipperService = new BaseModeleService();

        //iniatilisation des listes
        listeModelesComplet = new ArrayList<>();
        hashMapModeles = new HashMap();

        // Initialisation du champ Pseudo
        settings = getActivity().getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
        String pseudoText = settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, "");
        champPseudo.setText(pseudoText);

        // Initialisation du champ Modele
        modeleFlipperService = new BaseModeleService();
        ArrayList<ModeleFlipper> listModeleFlipper = new BaseModeleService().getAllModeleFlipper(getActivity());

        for (ModeleFlipper modele : listModeleFlipper) {
            String NomComplet = modele.getNomComplet();
            listeModelesComplet.add(NomComplet);
            hashMapModeles.put(NomComplet, modele.getId());
        }
        // Create an ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, listeModelesComplet);

        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, modeleFlipperService.getAllNomModeleFlipper(getActivity().getApplicationContext()));
        initChampModele(champModeleFlipper, adapter);
        initChampModele(champModeleDeuxiemeFlipper, adapter);
        initChampModele(champModeleTroisiemeFlipper, adapter);
        initChampModele(champModeleQuatriemeFlipper, adapter);
        initChampModele(champModeleCinquiemeFlipper, adapter);



        return rootView;
    }

    public Commentaire getCommentaireToAdd() {
        Commentaire commentaireToAdd = null;
        if (champCommentaire.getText().length() != 0) {
            String pseudoCommentaire = getResources().getString(R.string.pseudoCommentaireAnonyme);
            Date dateDuJour = new Date();
            if (champPseudo.getText().length() > 0) {
                pseudoCommentaire = champPseudo.getText().toString();
            }
            String htmlString = Html.toHtml(champCommentaire.getText());
            htmlString = htmlString.replaceAll("[\n]", "");
            commentaireToAdd = new Commentaire(getParentActivity().getNewId(),
                    getParentActivity().getNewId(),
                    htmlString,
                    Commentaire.TYPE_NEW,
                    new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour),
                    pseudoCommentaire,
                    true);
        }
        return commentaireToAdd;
    }

    public ArrayList<ModeleFlipper> getModelesToAdd() {
        ArrayList<ModeleFlipper> listeRetour = new ArrayList<>();
        Context context = getActivity().getApplicationContext();
        String modelFlip1AsString = champModeleFlipper.getText().toString();
        String modelFlip2AsString = champModeleDeuxiemeFlipper.getText().toString();
        String modelFlip3AsString = champModeleTroisiemeFlipper.getText().toString();
        String modelFlip4AsString = champModeleQuatriemeFlipper.getText().toString();
        String modelFlip5AsString = champModeleCinquiemeFlipper.getText().toString();


        if (!modelFlip1AsString.equals("")&& hashMapModeles.get(modelFlip1AsString) != null) {
            modeleFlipper = modeleFlipperService.getModeleById(context, Long.parseLong(String.valueOf(hashMapModeles.get(modelFlip1AsString))));
                if (modeleFlipper != null) {
                listeRetour.add(modeleFlipper);
            }
        }
        if (!modelFlip2AsString.equals("") && hashMapModeles.get(modelFlip2AsString) != null) {
            modeleFlipper2 = modeleFlipperService.getModeleById(context, Long.parseLong(String.valueOf(hashMapModeles.get(modelFlip2AsString))));
            if (modeleFlipper2 != null) {
                listeRetour.add(modeleFlipper2);
            }
        }

        if (!modelFlip3AsString.equals("") && hashMapModeles.get(modelFlip3AsString) != null) {
            modeleFlipper3 = modeleFlipperService.getModeleById(context, Long.parseLong(String.valueOf(hashMapModeles.get(modelFlip3AsString))));
            if (modeleFlipper3 != null) {
                listeRetour.add(modeleFlipper3);
            }
        }

        if (!modelFlip4AsString.equals("") && hashMapModeles.get(modelFlip4AsString) != null) {
            modeleFlipper4 = modeleFlipperService.getModeleById(context, Long.parseLong(String.valueOf(hashMapModeles.get(modelFlip4AsString))));
            if (modeleFlipper4 != null) {
                listeRetour.add(modeleFlipper4);
            }
        }

        if (!modelFlip5AsString.equals("") && hashMapModeles.get(modelFlip5AsString) != null) {
            modeleFlipper5 = modeleFlipperService.getModeleById(context, Long.parseLong(String.valueOf(hashMapModeles.get(modelFlip1AsString))));
            if (modeleFlipper5 != null) {
                listeRetour.add(modeleFlipper5);
            }
        }

        return listeRetour;
    }

    @Override
    public boolean mandatoryFieldsComplete() {
        boolean isError = false;
        if (champModeleFlipper.getText().length() == 0) {
            new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!")
                    .setMessage("Vous devez renseigner au moins un modèle du flipper.").setNeutralButton("Fermer", null)
                    .setIcon(R.drawable.ic_delete).show();
            isError = true;
        } else {
            if (getModelesToAdd().size() == 0) {
                new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!")
                        .setMessage("Vous devez renseigner au moins un modèle de flipper parmi la liste proposée. Si le modèle de flipper n'est pas dans la liste, utilisez la soumission par email à l'aide de l'icône en haut à droite").setNeutralButton("Fermer", null)
                        .setIcon(R.drawable.ic_delete).show();
                isError = true;
            }
        }
        return !isError;
    }

    @Override
    public void completeStep() {
        ArrayList<Flipper> listeRetour = new ArrayList<>();
        int i = 0;
        for (ModeleFlipper modele : getModelesToAdd()) {
            Flipper flipper = new Flipper();
            flipper.setActif(1);
            flipper.setDateMaj(getFormattedDate());
            flipper.setIdEnseigne(getNewEnseigneId());
            flipper.setId(getNewEnseigneId() + i++);
            flipper.setIdModele(modele.getId());
            flipper.setExploitant(champExploitant.getText().toString());
            flipper.setNbCreditsDeuxEuros(champNbCredits.getText().toString());
            listeRetour.add(flipper);
        }
        getParentActivity().completeModele(listeRetour, getCommentaireToAdd(), champPseudo.getText().toString());
    }

    private void initChampModele(AutoCompleteTextView champModeleView, ArrayAdapter<String> adapter) {
        champModeleView.setAdapter(adapter);
        champModeleView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        champModeleView.setDropDownAnchor(R.id.autocompletionModeleFlipper);
        champModeleView.setOnItemClickListener(itemSelectionneListener);
    }
}
