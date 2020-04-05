package com.pinmyballs.service.parse;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.service.ParseFactory;

import java.util.ArrayList;
import java.util.List;

public class ParseModeleService {
    private static final String TAG = "ParseModeleService";
	/**
	 * Retourne tous les modèles de flipper à partir du cloud
	 *
	 * @return int
	 */
	public List<ModeleFlipper> getAllModeleFlipper() {
		List<ModeleFlipper> listeModele = new ArrayList<ModeleFlipper>();
		List<ParseObject> listePo = new ArrayList<ParseObject>();
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
				FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME);
		try {
			listePo = query.find();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		for (ParseObject po : listePo) {
			ModeleFlipper modeleFlipper = new ModeleFlipper(
					po.getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ID),
					po.getString(FlipperDatabaseHandler.MODELE_FLIPPER_NOM),
					po.getString(FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE),
					po.getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT),
					po.getObjectId());
			listeModele.add(modeleFlipper);
		}

		return listeModele;
	}

	/**
	 * Retourne la liste des modèles de flippers à mettre à jour à partir d'un id
	 * @return
	 */
	public List<ModeleFlipper> getMajModeleById(long id){
		List<ModeleFlipper> listeModele = new ArrayList<ModeleFlipper>();

		List<ParseObject> listePo;
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME);
		try {
			query.setLimit(400);
			query.whereGreaterThan(FlipperDatabaseHandler.MODELE_FLIPPER_ID, id);
			listePo = query.find();
		} catch (ParseException e1) {
			e1.printStackTrace();
			return null;
		}
		for (ParseObject po : listePo){
			ModeleFlipper modele = new ModeleFlipper(po.getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ID),
					po.getString(FlipperDatabaseHandler.MODELE_FLIPPER_NOM),
					po.getString(FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE),
					po.getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT),
					po.getObjectId());
			listeModele.add(modele);
		}
		return listeModele;
	}

	public String getModeleObjectId(long id){
		String modeleObjectId;
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME);
		query.whereEqualTo(FlipperDatabaseHandler.MODELE_FLIPPER_ID, id);
		try {
			query.whereEqualTo(FlipperDatabaseHandler.MODELE_FLIPPER_ID, id);
			modeleObjectId = query.getFirst().getObjectId();
            Log.d(TAG, "gotModeleObjectId for "+ id +" : "+ modeleObjectId);
			return modeleObjectId;
		} catch (ParseException e1) {
			e1.printStackTrace();
			return null;
		}
	}

    public void ajouterModele(final Context pContext, final ModeleFlipper modeleFlipper) {

		ParseFactory parseFactory = new ParseFactory();
		//creation d'une liste d'envoi
		ArrayList<ParseObject> objectsToSend = new ArrayList<ParseObject>();

		// On créé l'objet du nouveau flipper et on l'ajoute à la liste d'envoi
		objectsToSend.add(parseFactory.getParseObject(modeleFlipper));


		ParseObject.saveAllInBackground(objectsToSend, new SaveCallback() {
			@Override
			public void done(ParseException e) {
                Toast toast = Toast.makeText(pContext, modeleFlipper.getNomComplet() + " ajouté. Refresh database.", Toast.LENGTH_LONG);
				toast.show();
			}
		});

	}
}
