package com.pinmyballs.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.pinmyballs.CommentaireActivity;
import com.pinmyballs.PageInfoFlipperPager;
import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.fragment.EditCommentDialog;
import com.pinmyballs.metier.Commentaire;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author Fafouche
 */
public class ListeCommentaireAdapter extends ArrayAdapter<Commentaire> {
    private static final String TAG = "ListeCommentaireAdapter";

    private Context context;
    private List<Commentaire> listeCommentaire;

	public ListeCommentaireAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

    private OnClickListener CommentaireClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: clicked on comment no " + v.getTag());
            Commentaire commentaire = listeCommentaire.get((Integer) v.getTag());
            if (commentaire.getFlipper() != null) {
                Intent infoActivite = new Intent(getContext(), PageInfoFlipperPager.class);
                infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_POUR_INFO, commentaire.getFlipper());
                // On va sur l'onglet de la carte
                infoActivite.putExtra(PageInfoFlipperPager.INTENT_FLIPPER_ONGLET_DEFAUT, 0);
                getContext().startActivity(infoActivite);
            }
        }
    };
    private OnClickListener CommentaireEdit = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //Commentaire commentaire = listeCommentaire.get((Integer) v.getTag());
            int commentaireNumber = (Integer) ((ViewGroup) v.getParent().getParent()).getTag();
            Log.d(TAG, "onClick: clicked on edit comment no " + commentaireNumber);
            Commentaire commentaire = listeCommentaire.get(commentaireNumber);
            Log.d(TAG, "onClick: " + commentaire.getTexte());
            Bundle bundle = new Bundle();
            bundle.putString("text", commentaire.getTexte());
            bundle.putSerializable("comment", commentaire);
            EditCommentDialog editCommentDialog = new EditCommentDialog();
            editCommentDialog.setArguments(bundle);

            //editCommentDialog.setTargetFragment(,1);
            FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
            editCommentDialog.show(manager, "MyEditCommentaireDialog");


        }
    };

	public ListeCommentaireAdapter(Context context, int resource, List<Commentaire> items) {
		super(context, resource, items);
		this.listeCommentaire = items;
        this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.simple_list_item_commentaire, null);
		}

		// On set les tags pour pouvoir retrouver sur quelle ligne on a cliqué.
		v.setTag(position);
		v.setOnClickListener(CommentaireClickListener);

		Commentaire p = listeCommentaire.get(position);

		if (p != null) {

			TextView pseudoTV = v.findViewById(R.id.textePseudo);
			TextView dateTV = v.findViewById(R.id.textDate);
			TextView commentaireTV = v.findViewById(R.id.texteCommentaire);
            ImageButton editBtn = v.findViewById(R.id.buttonEdit);
            editBtn.setOnClickListener(CommentaireEdit);

            SharedPreferences settings = context.getSharedPreferences(PreferencesActivity.PREFERENCES_FILENAME, MODE_PRIVATE);
            boolean adminMode = settings.getBoolean(PreferencesActivity.KEY_PREFERENCES_ADMIN_MODE, PreferencesActivity.DEFAULT_VALUE_ADMIN_MODE);
            if (adminMode) {
                editBtn.setVisibility(View.INVISIBLE);
            }

            if (context instanceof CommentaireActivity) {
                editBtn.setVisibility(View.INVISIBLE);
            }


			if (pseudoTV != null && p.getFlipper() != null){
				if (p.getPseudo().length()>0){
                    Spanned html = Html.fromHtml(getContext().getResources().getString(
                            R.string.fulltextCommentaire2,
                            p.getPseudo(),
                            p.getFlipper().getModele().getNom(),
                            p.getFlipper().getEnseigne().getVille(),
                            p.getFlipper().getEnseigne().getNom()));
					CharSequence trimmed = trim(html, 0, html.length());
					pseudoTV.setText(trimmed);
				}else{
                    Spanned html = Html.fromHtml(getContext().getResources().getString(
                            R.string.fulltextCommentaireAnonyme,
                            p.getFlipper().getModele().getNom(),
                            p.getFlipper().getEnseigne().getVille()));
					CharSequence trimmed = trim(html, 0, html.length());
					pseudoTV.setText(trimmed);
				}
			}else if (pseudoTV != null && p.getPseudo() != null && p.getPseudo().length()>0) {
				pseudoTV.setText(p.getPseudo());
				pseudoTV.setTypeface(null, Typeface.BOLD);
			}
			if (dateTV != null) {
				dateTV.setText(p.getDate());
			}
			if (commentaireTV != null) {
				Spanned html = Html.fromHtml(p.getTexte());
				CharSequence trimmed = trim(html, 0, html.length());
				commentaireTV.setText(trimmed);
			}
		}
		return v;
	}

	public static CharSequence trim(CharSequence s, int start, int end) {
		while (start < end && Character.isWhitespace(s.charAt(start))) {
			start++;
		}

		while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
			end--;
		}

		return s.subSequence(start, end);
	}

}
