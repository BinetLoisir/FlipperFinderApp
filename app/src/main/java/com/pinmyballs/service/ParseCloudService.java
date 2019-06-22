package com.pinmyballs.service;

import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.ModeleFlipper;

import java.util.ArrayList;
import java.util.List;

public class ParseCloudService {

    private final static String TAG = ParseCloudService.class.getSimpleName();

    public ParseCloudService() {
    }

    /**
     *
     * @param latitude
     * @param longitude
     * @param maxDistance
     * @return
     * @throws Exception
     */
    public List<Enseigne> FetchEnseigneByLocation(double latitude, double longitude, double maxDistance) throws Exception {
        ArrayList<Enseigne> listEnseignes = new ArrayList<Enseigne>();
        ParseGeoPoint myLocation = new ParseGeoPoint(latitude, longitude);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME);
        query.whereGreaterThanOrEqualTo("ENS_NBFLIPS",1);
        query.whereWithinKilometers("ENS_GEO", myLocation, maxDistance);
        query.setLimit(25);

        List<ParseObject> results = query.find();
        Log.d(TAG, "Fetches Enseignes : results size : " + results.size());
        if (results.size() != 0) {
            for (ParseObject Object : results) {
                Enseigne EnseigneToAdd = new Enseigne();
                EnseigneToAdd.setNom(Object.getString(FlipperDatabaseHandler.ENSEIGNE_NOM));
                EnseigneToAdd.setLatitude(Double.toString(Object.getParseGeoPoint("ENS_GEO").getLatitude()));
                EnseigneToAdd.setLongitude(Double.toString(Object.getParseGeoPoint("ENS_GEO").getLongitude()));
                EnseigneToAdd.setAdresse(Object.getString(FlipperDatabaseHandler.ENSEIGNE_ADRESSE));
                EnseigneToAdd.setCodePostal(Object.getString(FlipperDatabaseHandler.ENSEIGNE_CODE_POSTAL));
                EnseigneToAdd.setVille(Object.getString(FlipperDatabaseHandler.ENSEIGNE_VILLE));
                EnseigneToAdd.setPays(Object.getString(FlipperDatabaseHandler.ENSEIGNE_PAYS));
                listEnseignes.add(EnseigneToAdd);
            }
        }
        return listEnseignes;
    }

    /**
     *
     * @param latitude Latitude du point
     * @param longitude Longitude of the
     * @param maxDistance Search radius
     * @return List de flips
     * @throws Exception
     */
    public List<Flipper> FetchNearbyFlippers(double latitude, double longitude, double maxDistance) throws Exception {
        ArrayList<Flipper> listFlipper = new ArrayList<Flipper>();

        ParseGeoPoint myLocation = new ParseGeoPoint(latitude, longitude);
        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME);
        innerQuery.whereWithinKilometers("ENS_GEO", myLocation, maxDistance);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereMatchesQuery(FlipperDatabaseHandler.FLIPPER_ENS_POINTER,innerQuery);
        query.include(FlipperDatabaseHandler.FLIPPER_ENS_POINTER);
        query.include(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER);

        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ACTIF, true);

        List<ParseObject> results = query.find();
        Log.d(TAG, "Fetched Nearby Flippers : " + results.size());
        if (results.size() != 0) {
            for (ParseObject PO : results) {
                ParseFactory parseFactory = new ParseFactory();
                Flipper flipperToAdd = parseFactory.getFlipper(PO);
                flipperToAdd.setEnseigne(parseFactory.getEnseigne(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_ENS_POINTER)));
                flipperToAdd.setModele(parseFactory.getModele(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER)));
                listFlipper.add(flipperToAdd);
            }
        }
        return listFlipper;
    }


    /**
     *
     * @param latLngBounds
     * @return
     * @throws Exception
     */
    public List<Flipper> FetchNearbyFlippersBounds(LatLngBounds latLngBounds) throws Exception {
        ArrayList<Flipper> listFlipper = new ArrayList<Flipper>();

        ParseGeoPoint SW = new ParseGeoPoint(latLngBounds.southwest.latitude, latLngBounds.southwest.longitude);
        ParseGeoPoint NE = new ParseGeoPoint(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude);
        ParseQuery<ParseObject> innerQuery = ParseQuery.getQuery(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME);
        innerQuery.whereWithinGeoBox("ENS_GEO", SW, NE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereMatchesQuery(FlipperDatabaseHandler.FLIPPER_ENS_POINTER,innerQuery);
        query.include(FlipperDatabaseHandler.FLIPPER_ENS_POINTER);
        query.include(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER);

        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ACTIF, true);

        List<ParseObject> results = query.find();
        Log.d(TAG, "Fetched on map : " + results.size() + " flippers");
        if (results.size() != 0) {
            for (ParseObject PO : results) {
                ParseFactory parseFactory = new ParseFactory();
                Flipper flipperToAdd = parseFactory.getFlipper(PO);
                flipperToAdd.setEnseigne(parseFactory.getEnseigne(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_ENS_POINTER)));
                flipperToAdd.setModele(parseFactory.getModele(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER)));
                listFlipper.add(flipperToAdd);
            }
        }
        return listFlipper;
    }

    /**
     *
     * @param enseigne
     * @return
     * @throws Exception
     */
    public List<Flipper> FetchFlippersByEnseigne(Enseigne enseigne) throws Exception {
        ArrayList<Flipper> listFlipper = new ArrayList<Flipper>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ENSEIGNE, enseigne.getId());
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ACTIF, true);
        query.include(FlipperDatabaseHandler.FLIPPER_ENS_POINTER);
        query.include(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER);
        query.setLimit(25);

        List<ParseObject> results = query.find();
        Log.d("Fetched Flippers", "results size : " + results.size());
        if (results.size() != 0) {
            for (ParseObject PO : results) {
                ParseFactory parseFactory = new ParseFactory();
                Flipper flipperToAdd = parseFactory.getFlipper(PO);
                flipperToAdd.setEnseigne(parseFactory.getEnseigne(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_ENS_POINTER)));
                flipperToAdd.setModele(parseFactory.getModele(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER)));
                listFlipper.add(flipperToAdd);
            }
        }
        return listFlipper;
    }

    public List<Flipper> FetchOtherFlippers(Flipper flipper) throws Exception {
        ArrayList<Flipper> listFlipper = new ArrayList<Flipper>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ENSEIGNE, flipper.getEnseigne().getId());
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ACTIF, true);
        //Flipper from query is excluded
        query.whereNotEqualTo(FlipperDatabaseHandler.FLIPPER_ID,flipper.getId());
        query.include(FlipperDatabaseHandler.FLIPPER_ENS_POINTER);
        query.include(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER);
        query.setLimit(25);

        List<ParseObject> results = query.find();
        Log.d("Fetched Flippers", "results size : " + results.size());
        if (results.size() != 0) {
            for (ParseObject PO : results) {
                ParseFactory parseFactory = new ParseFactory();
                Flipper flipperToAdd = parseFactory.getFlipper(PO);
                flipperToAdd.setEnseigne(parseFactory.getEnseigne(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_ENS_POINTER)));
                flipperToAdd.setModele(parseFactory.getModele(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER)));
                listFlipper.add(flipperToAdd);
            }
        }
        return listFlipper;
    }


    /**
     *
     * @param enseigne
     * @return
     * @throws Exception
     */
    public List<ModeleFlipper> FetchModelesByEnseigne(Enseigne enseigne) throws Exception {

        ArrayList<ModeleFlipper> listModele = new ArrayList<ModeleFlipper>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ENSEIGNE, enseigne.getId());
        query.whereEqualTo(FlipperDatabaseHandler.FLIPPER_ACTIF, true);
        query.include(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER);
        query.setLimit(25);

        List<ParseObject> results = query.find();
        Log.d(TAG, "Fetched modeles by enseigne : " + results.size());
        if (results.size() != 0) {
            for (ParseObject PO : results) {
                ModeleFlipper modeleToAdd = new ParseFactory().getModele(PO.getParseObject(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER));
                listModele.add(modeleToAdd);
            }
        }
        return listModele;
    }


    /**
     *
     * @param listFlipper
     * @return
     * @throws Exception
     */
    public List<ModeleFlipper> GetFlipModeles(List<Flipper> listFlipper) throws Exception {
        ArrayList<ModeleFlipper> listModele = new ArrayList<ModeleFlipper>();

        for(Flipper flip : listFlipper){
            ParseQuery<ParseObject> query = ParseQuery.getQuery(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME);
            query.whereEqualTo(FlipperDatabaseHandler.MODELE_FLIPPER_ID, flip.getIdModele());
            query.setLimit(1);
            List<ParseObject> results = query.find();
            Log.d("Fetched Modeles", "results size : " + results.size());
            if (results.size() != 0) {
                ModeleFlipper ModeleToAdd = new ModeleFlipper();
                ModeleToAdd.setNom(results.get(0).getString(FlipperDatabaseHandler.MODELE_FLIPPER_NOM));
                ModeleToAdd.setAnneeLancement(results.get(0).getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT));
                ModeleToAdd.setMarque(results.get(0).getString(FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE));
                ModeleToAdd.setId(results.get(0).getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ID));
                listModele.add(ModeleToAdd);
            }
        }
        return listModele;
    }

}