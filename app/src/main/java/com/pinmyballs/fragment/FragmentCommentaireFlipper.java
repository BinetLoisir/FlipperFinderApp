package com.pinmyballs.fragment;

import android.app.AlertDialog;
import android.content.Context;
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

	private EditText pseudoTV ;
	private EditText commentaire;
	private TextView tvPasCommentaire = null;

	private SharedPreferences settings;

	private ListView listeCommentaireView = null;

	private ScrollView newCommentaireLayout = null;

	private Flipper flipper;
	private ArrayList<Commentaire> listeCommentaires = null;

	private CommentaireService commentaireService;
	private ListeCommentaireAdapter listeCommentaireAdapter;

	public FragmentCommentaireFlipper(){
	}

	private final OnClickListener EnvoiCommentaireListener = new OnClickListener() {
		public void onClick(View v) {
			// On sauvegarde le pseudo
			Editor editor = settings.edit();
            editor.putString(PreferencesActivity.KEY_PSEUDO_FULL, pseudoTV.getText().toString());
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
				if (pseudoTV.getText().length() > 0){
					pseudoCommentaire = pseudoTV.getText().toString();
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
				if (listeCommentaires != null && listeCommentaires.size() > 0) {
					listeCommentaires.add(commentaireToAdd);
					listeCommentaireAdapter.notifyDataSetChanged();

				} else {
					rafraichitListeCommentaire();
				}

				//Hide keyboard and input form
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
            listeCommentaireAdapter = new ListeCommentaireAdapter(getActivity(), R.layout.simple_list_item_commentaire, listeCommentaires);
            listeCommentaireView.setAdapter(listeCommentaireAdapter);
        } else {
            tvPasCommentaire.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_commentaire_flipper, container, false);

		flipper = (Flipper) getArguments().getSerializable("flip");
		String pseudo = getArguments().getString("pseudo");

        commentaireService = new CommentaireService(this::rafraichitListeCommentaire);

        listeCommentaireView = rootView.findViewById(R.id.listeCommentaires);
        newCommentaireLayout = rootView.findViewById(R.id.layoutNewComm);
        pseudoTV = rootView.findViewById(R.id.champPseudo);
        commentaire = rootView.findViewById(R.id.texteCommentaire);
		Button boutonLaisserCommentaireFlipper = rootView.findViewById(R.id.boutonCommentaire);
        tvPasCommentaire = rootView.findViewById(R.id.textPasCommentaire);
		Button boutonAnnulerNouveauCommentaire = rootView.findViewById(R.id.boutonCancelNewCommentaire);
		Button boutonEnvoiCommentaire = rootView.findViewById(R.id.boutonNewCommentaire);

        // On cache le layout qui va servir à renseigner un nouveau commentaire
        newCommentaireLayout.setVisibility(View.GONE);

        rafraichitListeCommentaire();
        //Récupère le pseudo et préremplit le champ si besoin
        settings = getActivity().getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, 0);
		String pseudoText = settings.getString(PreferencesActivity.KEY_PSEUDO_FULL, "");

        pseudoTV.setText(pseudoText);

        boutonLaisserCommentaireFlipper.setOnClickListener(LaisserCommentaireListener);
        boutonEnvoiCommentaire.setOnClickListener(EnvoiCommentaireListener);
        boutonAnnulerNouveauCommentaire.setOnClickListener(AnnuleNouveauCommentaireListener);

        return rootView;
    }
	private final OnClickListener LaisserCommentaireListener = v -> {
		if (NetworkUtil.isConnected(getActivity().getApplicationContext())){
			Animation slide = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_up);
			newCommentaireLayout.setVisibility(View.VISIBLE);
			newCommentaireLayout.startAnimation(slide);
		}else{
			Toast toast = Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.toastAjouteCommentairePasPossibleReseau), Toast.LENGTH_SHORT);
			toast.show();
		}
	};

	private final OnClickListener AnnuleNouveauCommentaireListener = v -> {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		newCommentaireLayout.setVisibility(View.GONE);
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
