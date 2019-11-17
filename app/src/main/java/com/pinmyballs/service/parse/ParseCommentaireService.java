package com.pinmyballs.service.parse;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.pinmyballs.R;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.fragment.FragmentCommentaireFlipper.FragmentCallback;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.service.base.BaseCommentaireService;
import com.pinmyballs.utils.ProgressBarHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParseCommentaireService {
	private FragmentCallback mFragmentCallback;

	public ParseCommentaireService(FragmentCallback fragmentCallback) {
		mFragmentCallback = fragmentCallback;
	}
	/**
     * Retourne la liste des commentaires à mettre à jour à partir d'une date donnée.
	 * @param dateDerniereMaj
	 * @return
	 */
	public List<Commentaire> getMajCommentaireByDate(String dateDerniereMaj){
		List<Commentaire> listeCommentaire = new ArrayList<Commentaire>();

		List<ParseObject> listePo;
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME);
		try {
			query.setLimit(2500);
			query.whereGreaterThanOrEqualTo(FlipperDatabaseHandler.COMM_DATE, dateDerniereMaj);
			listePo = query.find();
		} catch (ParseException e1) {
			e1.printStackTrace();
			return null;
		}
		for (ParseObject po : listePo){
			Commentaire commentaire = new Commentaire(po.getLong(FlipperDatabaseHandler.COMM_ID),
					po.getLong(FlipperDatabaseHandler.COMM_FLIPPER_ID),
					po.getString(FlipperDatabaseHandler.COMM_TEXTE),
                    po.getString(FlipperDatabaseHandler.COMM_TYPE),
					po.getString(FlipperDatabaseHandler.COMM_DATE),
					po.getString(FlipperDatabaseHandler.COMM_PSEUDO),
					po.getBoolean(FlipperDatabaseHandler.COMM_ACTIF));
			listeCommentaire.add(commentaire);
		}
		return listeCommentaire;
	}

    public boolean ajouteCommentaire(final Context pContext, final Commentaire commentaire) {
        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();

		ParseObject parseCommentaire = new ParseObject(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME);
		parseCommentaire.put(FlipperDatabaseHandler.COMM_ID, commentaire.getId());
		parseCommentaire.put(FlipperDatabaseHandler.COMM_FLIPPER_ID, commentaire.getFlipperId());
		parseCommentaire.put(FlipperDatabaseHandler.COMM_DATE, commentaire.getDate());
		parseCommentaire.put(FlipperDatabaseHandler.COMM_PSEUDO, commentaire.getPseudo());
		parseCommentaire.put(FlipperDatabaseHandler.COMM_TEXTE, commentaire.getTexte());
        parseCommentaire.put(FlipperDatabaseHandler.COMM_TYPE, commentaire.getType());
		parseCommentaire.put(FlipperDatabaseHandler.COMM_ACTIF, commentaire.getActif());

		//get the FlipperObjectID by direct query to the Cloud
		String FlipperObjectID = "";
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
		query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ID,commentaire.getFlipperId());
		try {
			FlipperObjectID = query.getFirst().getObjectId();
			Log.d("Flipper ObjectID: ", FlipperObjectID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//add the pointer
		if(FlipperObjectID != null && !FlipperObjectID.isEmpty()) {
			parseCommentaire.put(FlipperDatabaseHandler.COMM_FLIP_POINTER, ParseObject.createWithoutData(FlipperDatabaseHandler.FLIPPER_TABLE_NAME,FlipperObjectID));

		}

		parseCommentaire.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
                mProgressBarHandler.hide();
				if (e == null){
					BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
					baseCommentaireService.addCommentaire(commentaire, pContext);
					Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastAjouteCommentaireCloudOK), Toast.LENGTH_LONG);
					toast.show();
					if (mFragmentCallback != null){
						mFragmentCallback.onTaskDone();
					}
				}else{
					Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastAjouteCommentaireCloudKO), Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
		return true;
	}

    public boolean updateCommentaire(final Context pContext, final Commentaire oldCommentaire, final Commentaire newCommentaire) {
        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();

        final Date dateDuJour = new Date();
        final String dateMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.COMM_ID, oldCommentaire.getId());
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {
                    objects.get(0).put(FlipperDatabaseHandler.COMM_TEXTE, newCommentaire.getTexte());
                    objects.get(0).put(FlipperDatabaseHandler.COMM_DATE, dateMaj);
                    objects.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                mProgressBarHandler.hide();
                                //TODO Mise à jour direct de la base locale.
                                //BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
                                //flipper.setDateMaj(dateToSave);
                                //baseFlipperService.majFlipper(flipper, pContext);
                                Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastAjouteCommentaireCloudOK), Toast.LENGTH_SHORT);
                                toast.show();
                                if (mFragmentCallback != null) {
                                    mFragmentCallback.onTaskDone();
                                }
                            } else {
                                Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastAjouteCommentaireCloudKO), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                }
            }
        });
        return true;
    }
}
