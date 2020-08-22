package com.pinmyballs.service.parse;

import android.content.Context;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Tournoi;
import com.pinmyballs.service.ParseFactory;

import java.util.ArrayList;
import java.util.List;

public class ParseTournoiService {

	/**
	 * Retourne tous les tournois à partir du cloud
	 */
	public List<Tournoi> getAllTournoi() {
		List<Tournoi> listeTournoi = new ArrayList<>();
		List<ParseObject> listePo = new ArrayList<>();
		ParseQuery<ParseObject> query = new ParseQuery<>(
				FlipperDatabaseHandler.TOURNOI_TABLE_NAME);
		try {
			listePo = query.find();
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		for (ParseObject po : listePo) {
			Tournoi tournoi = new Tournoi(
					po.getLong(FlipperDatabaseHandler.TOUR_ID),
					po.getString(FlipperDatabaseHandler.TOUR_NOM),
					po.getString(FlipperDatabaseHandler.TOUR_COMMENTAIRE),
					po.getString(FlipperDatabaseHandler.TOUR_DATE),
					po.getNumber(FlipperDatabaseHandler.TOUR_LATITUDE).toString(),
					po.getNumber(FlipperDatabaseHandler.TOUR_LONGITUDE).toString(),
					po.getString(FlipperDatabaseHandler.TOUR_ADRESSE),
					po.getString(FlipperDatabaseHandler.TOUR_CODE_POSTAL),
					po.getString(FlipperDatabaseHandler.TOUR_VILLE),
					po.getString(FlipperDatabaseHandler.TOUR_PAYS),
					po.getString(FlipperDatabaseHandler.TOUR_URL),
					po.getString(FlipperDatabaseHandler.TOUR_ENS));
			listeTournoi.add(tournoi);
		}
		return listeTournoi;
	}

    public boolean ajouterTournoi(final Context pContext, Tournoi tournoi) {

        ParseFactory parseFactory = new ParseFactory();
        //creation d'une liste d'envoi
        ArrayList<ParseObject> objectsToSend = new ArrayList<>();

        // On créé l'objet du nouveau tournoi et on l'ajoute à la liste d'envoi
        objectsToSend.add(parseFactory.getParseObject(tournoi));


        //Begin to send
        Toast toast = Toast.makeText(pContext, "Envoi en cours", Toast.LENGTH_SHORT);
        toast.show();

        ParseObject.saveAllInBackground(objectsToSend, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast toast = Toast.makeText(pContext, "Envoi effectué, Merci pour votre contribution :)", Toast.LENGTH_LONG);
                toast.show();
                //close activity SignalementActivity
                //finish();
            }
        });

        return true;
    }

}
