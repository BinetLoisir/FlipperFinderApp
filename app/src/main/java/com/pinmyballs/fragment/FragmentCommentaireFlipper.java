package com.pinmyballs.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.pinmyballs.PageInfoFlipperPager;
import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.CommentaireService;
import com.pinmyballs.utils.ListeCommentaireAdapter;
import com.pinmyballs.utils.NetworkUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FragmentCommentaireFlipper extends Fragment implements EditCommentDialog.OnInputSelected {

    private static final String TAG = "FragmentCommentaireFlip";

	Button boutonLaisserCommentaireFlipper;
	EditText pseudo = null;
	EditText commentaire = null;
	String pseudoText = "";
	TextView tvPasCommentaire = null;

	SharedPreferences settings;

	ListView listeCommentaireView = null;

	ScrollView newCommentaireLayout = null;

	Button boutonAnnulerNouveauCommentaire = null;
	Button boutonEnvoiCommentaire = null;

	Flipper flipper;
	ArrayList<Commentaire> listeCommentaires = null;

	CommentaireService commentaireService;
	private OnClickListener EnvoiCommentaireListener = new OnClickListener() {
		public void onClick(View v) {
			// On sauvegarde le pseudo
			Editor editor = settings.edit();
            editor.putString(PreferencesActivity.KEY_PSEUDO_FULL, pseudo.getText().toString());
			editor.apply();

			// Si un commentaire a été écrit, l'envoyer!
			if (commentaire.getText().length() == 0){
				new AlertDialog.Builder(getActivity()).setTitle("Envoi impossible!").setMessage("Vous n'avez pas rempli le champ commentaire!").setNeutralButton("Fermer", null).setIcon(R.drawable.ic_delete).show();
			}else{
				//EasyTracker.getTracker().sendEvent("ui_action", "button_press", "add_commentaire_button", 0L);
				String htmlString = Html.toHtml(commentaire.getText());
				htmlString = htmlString.replaceAll("[\n]", "");
				Date dateDuJour = new Date();
				String pseudoCommentaire = getResources().getString(R.string.pseudoCommentaireAnonyme);
				if (pseudo.getText().length() > 0){
					pseudoCommentaire = pseudo.getText().toString();
				}
				Commentaire commentaireToAdd = new Commentaire(	dateDuJour.getTime(),
						flipper.getId(),
						htmlString,
                        Commentaire.TYPE_POST,
						new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour),
						pseudoCommentaire,
						true);
				commentaireToAdd.setFlipper(flipper);
				//((AppCompatActivity)getActivity()).setSupportProgressBarIndeterminateVisibility(true);
				commentaireService.ajouteCommentaire(getActivity(), commentaireToAdd);
				// Rafraichir la liste des commentaires
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				newCommentaireLayout.setVisibility(View.GONE);
			}
		}
	};


    private void rafraichitListeCommentaire() {
        // Récupère la liste des commentaires et les affiche
        listeCommentaires = commentaireService.getCommentaireByFlipperId(getActivity().getApplicationContext(), flipper.getId());
        if (listeCommentaires != null && listeCommentaires.size() > 0) {
            tvPasCommentaire.setVisibility(View.INVISIBLE);
            ListeCommentaireAdapter customAdapter = new ListeCommentaireAdapter(getActivity(), R.layout.simple_list_item_commentaire, listeCommentaires);
            listeCommentaireView.setAdapter(customAdapter);
        } else {
            tvPasCommentaire.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_commentaire_flipper, container, false);

        Intent i = getActivity().getIntent();
        flipper = (Flipper) i.getSerializableExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO);

        commentaireService = new CommentaireService(new FragmentCallback() {
            @Override
            public void onTaskDone() {
                rafraichitListeCommentaire();
            }
        });

        listeCommentaireView = (ListView) rootView.findViewById(R.id.listeCommentaires);
        newCommentaireLayout = (ScrollView) rootView.findViewById(R.id.layoutNewComm);
        pseudo = (EditText) rootView.findViewById(R.id.champPseudo);
        commentaire = (EditText) rootView.findViewById(R.id.texteCommentaire);
        boutonLaisserCommentaireFlipper = (Button) rootView.findViewById(R.id.boutonCommentaire);
        tvPasCommentaire = (TextView) rootView.findViewById(R.id.textPasCommentaire);
        boutonAnnulerNouveauCommentaire = (Button) rootView.findViewById(R.id.boutonCancelNewCommentaire);
        boutonEnvoiCommentaire = (Button) rootView.findViewById(R.id.boutonNewCommentaire);

        // On cache le layout qui va servir à renseigner un nouveau commentaire
        newCommentaireLayout.setVisibility(View.GONE);

        rafraichitListeCommentaire();
        //Récupère le pseudo et préremplit le champ si besoin
        settings = getActivity().getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
        pseudoText = settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, "");

        pseudo.setText(pseudoText);

        boutonLaisserCommentaireFlipper.setOnClickListener(LaisserCommentaireListener);

        boutonEnvoiCommentaire.setOnClickListener(EnvoiCommentaireListener);
        boutonAnnulerNouveauCommentaire.setOnClickListener(AnnuleNouveauCommentaireListener);

        return rootView;
    }
	private OnClickListener LaisserCommentaireListener = new OnClickListener() {
		public void onClick(View v) {
			if (NetworkUtil.isConnected(getActivity().getApplicationContext())){
				Animation slide = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_up);
				newCommentaireLayout.setVisibility(View.VISIBLE);
				newCommentaireLayout.startAnimation(slide);
			}else{
				Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toastAjouteCommentairePasPossibleReseau), Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};

	private OnClickListener AnnuleNouveauCommentaireListener = new OnClickListener() {
		public void onClick(View v) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			newCommentaireLayout.setVisibility(View.GONE);
		}
	};

    @Override
    public void sendInput(String input) {
        Log.d(TAG, "sendInput: Found incoming input" + input);
        //update commentaire in database
    }


	public interface FragmentCallback {
		void onTaskDone();
	}

}
