package com.pinmyballs.service;

import android.util.Log;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.metier.Tournoi;
import com.pinmyballs.service.parse.ParseEnseigneService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ParseFactory {
    private static final String TAG = ParseFactory.class.getSimpleName();

    public ParseObject getParseObject(Flipper flipper){
        ParseObject parseObject = new ParseObject(FlipperDatabaseHandler.FLIPPER_TABLE_NAME);
        parseObject.put(FlipperDatabaseHandler.FLIPPER_ACTIF, flipper.isActif());
        parseObject.put(FlipperDatabaseHandler.FLIPPER_DATMAJ, flipper.getDateMaj());
        parseObject.put(FlipperDatabaseHandler.FLIPPER_ENSEIGNE, flipper.getIdEnseigne());
        parseObject.put(FlipperDatabaseHandler.FLIPPER_ID, flipper.getId());
        parseObject.put(FlipperDatabaseHandler.FLIPPER_MODELE, flipper.getIdModele());
        parseObject.put(FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E, "");
        return parseObject;
    }

    public ParseObject getParseObject(Enseigne enseigne){
        ParseObject parseEnseigne = new ParseObject(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME);
        Double latitude = Double.valueOf(enseigne.getLatitude());
        Double longitude = Double.valueOf(enseigne.getLongitude());
        ParseGeoPoint geopoint = new ParseGeoPoint(latitude,longitude);

        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_ID, enseigne.getId());
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_NOM, enseigne.getNom());

        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_LATITUDE, latitude);
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_LONGITUDE, longitude);
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_ADRESSE, enseigne.getAdresse());
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_CODE_POSTAL, enseigne.getCodePostal());
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_VILLE, enseigne.getVille());
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_PAYS, enseigne.getPays());
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_DATMAJ, enseigne.getDateMaj());
        parseEnseigne.put(FlipperDatabaseHandler.ENSEIGNE_GEOPOINT, geopoint);
        return parseEnseigne;
    }

    public ParseObject getParseObject(ModeleFlipper modeleFlipper){
        ParseObject parseModele = new ParseObject(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME);
        parseModele.put(FlipperDatabaseHandler.MODELE_FLIPPER_ID, modeleFlipper.getId());
        parseModele.put(FlipperDatabaseHandler.MODELE_FLIPPER_NOM, modeleFlipper.getNom());
        parseModele.put(FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE, modeleFlipper.getMarque());
        parseModele.put(FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT, modeleFlipper.getAnneeLancement());
        //parseModele.put(FlipperDatabaseHandler.MODELE_FLIPPER_OBJ_ID, modeleFlipper.getObjectId());
        return parseModele;
    }

    //La totale avec les pointeurs

    public ParseObject getPOWithPointersToExistingObjects(Flipper flipper){
        ParseObject parseFlipper = getParseObject(flipper);
        parseFlipper.put(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER, ParseObject.createWithoutData(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME,flipper.getModele().getObjectId()));
        parseFlipper.put(FlipperDatabaseHandler.FLIPPER_ENS_POINTER, ParseObject.createWithoutData(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME, new ParseEnseigneService().getEnseigneObjectID(flipper.getEnseigne().getId())));
        return parseFlipper;
    }

    public ArrayList<ParseObject> getPOWithPointersToExistingObjects(Flipper flipper, Commentaire commentaire){
        ArrayList<ParseObject> parseObjectArrayList = new ArrayList<>();
        //On s'occupe du Flipper et de ses pointeurs
        ParseObject parseFlipper = getParseObject(flipper);
        parseFlipper.put(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER, ParseObject.createWithoutData(FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME,flipper.getModele().getObjectId()));
        parseFlipper.put(FlipperDatabaseHandler.FLIPPER_ENS_POINTER, ParseObject.createWithoutData(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME, new ParseEnseigneService().getEnseigneObjectID(flipper.getEnseigne().getId())));
        parseObjectArrayList.add(parseFlipper);

        //puis du commentaire s'il y en a un
        if (commentaire.getTexte() != null) {
            ParseObject parseCommentaire = getParseObject(commentaire);
            parseCommentaire.put(FlipperDatabaseHandler.COMM_FLIP_POINTER, parseFlipper);
            parseObjectArrayList.add(parseCommentaire);
            parseObjectArrayList.add(parseCommentaire);
        }

        return parseObjectArrayList;
    }


    // Below method create new Objects in the relationship. OK when new
    public ParseObject getPOWithPointersToNewObjects(Flipper flipper){
        ParseObject parseFlipper = getParseObject(flipper);
        parseFlipper.put(FlipperDatabaseHandler.FLIPPER_ENS_POINTER, getParseObject(flipper.getEnseigne()));
        parseFlipper.put(FlipperDatabaseHandler.FLIPPER_MODELE_POINTER, getParseObject(flipper.getModele()));
        return parseFlipper;
    }


    public ParseObject getParseObject(Score score){
        ParseObject parseScore = new ParseObject(FlipperDatabaseHandler.SCORE_TABLE_NAME);
        parseScore.put(FlipperDatabaseHandler.SCORE_ID, score.getId());
        parseScore.put(FlipperDatabaseHandler.SCORE_PSEUDO, score.getPseudo());
        parseScore.put(FlipperDatabaseHandler.SCORE_DATE, score.getDate());
        parseScore.put(FlipperDatabaseHandler.SCORE_FLIPPER_ID, score.getFlipperId());
        return parseScore;
    }

    public ParseObject getParseObject(Tournoi tournoi){
        ParseObject parseTournoi = new ParseObject(FlipperDatabaseHandler.TOURNOI_TABLE_NAME);
        parseTournoi.put(FlipperDatabaseHandler.TOUR_ID, tournoi.getId());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_NOM, tournoi.getNom());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_DATE,tournoi.getDate());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_COMMENTAIRE, tournoi.getCommentaire());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_LATITUDE, Double.parseDouble(tournoi.getLatitude()));
        parseTournoi.put(FlipperDatabaseHandler.TOUR_LONGITUDE, Double.parseDouble(tournoi.getLongitude()));
        parseTournoi.put(FlipperDatabaseHandler.TOUR_ADRESSE, tournoi.getAdresse());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_CODE_POSTAL, tournoi.getCodePostal());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_VILLE, tournoi.getVille());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_PAYS, tournoi.getPays());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_URL, tournoi.getUrl());
        parseTournoi.put(FlipperDatabaseHandler.TOUR_ENS, tournoi.getEns());

        //addtional field in Parse
        try {
            Calendar cal =  Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(tournoi.getDate()));
            cal.add(Calendar.HOUR_OF_DAY,12);
            Date realdate = cal.getTime();
            Log.d(TAG,"Date transformed  " + tournoi.getDate() + " =>" +  realdate.toString());
            parseTournoi.put(FlipperDatabaseHandler.TOUR_REALDATE,realdate);

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
            return parseTournoi;
    }

    public ParseObject getParseObject(Commentaire commentaire){
        ParseObject parseCommentaire = new ParseObject(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME);
        parseCommentaire.put(FlipperDatabaseHandler.COMM_ID, commentaire.getId());
        parseCommentaire.put(FlipperDatabaseHandler.COMM_FLIPPER_ID, commentaire.getFlipperId());
        parseCommentaire.put(FlipperDatabaseHandler.COMM_DATE, commentaire.getDate());
        parseCommentaire.put(FlipperDatabaseHandler.COMM_PSEUDO, commentaire.getPseudo());
        parseCommentaire.put(FlipperDatabaseHandler.COMM_TEXTE, commentaire.getTexte());
        parseCommentaire.put(FlipperDatabaseHandler.COMM_TYPE, commentaire.getType());
        parseCommentaire.put(FlipperDatabaseHandler.COMM_ACTIF, commentaire.getActif());
        return parseCommentaire;
    }

    public Flipper getFlipper(ParseObject PO){
        Flipper flipper = new Flipper();
        flipper.setActif(PO.getBoolean(FlipperDatabaseHandler.FLIPPER_ACTIF));
        flipper.setDateMaj(PO.getString(FlipperDatabaseHandler.FLIPPER_DATMAJ));
        flipper.setIdEnseigne(PO.getLong(FlipperDatabaseHandler.FLIPPER_ENSEIGNE));
        flipper.setId(PO.getLong(FlipperDatabaseHandler.FLIPPER_ID));
        flipper.setIdModele(PO.getLong(FlipperDatabaseHandler.FLIPPER_MODELE));
        flipper.setNbCreditsDeuxEuros(Long.getLong(PO.getString(FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E)));
        return flipper;
    }

    public Enseigne getEnseigne(ParseObject PO){
        Enseigne enseigne = new Enseigne();
        enseigne.setId(PO.getLong(FlipperDatabaseHandler.ENSEIGNE_ID));
        enseigne.setDateMaj(PO.getString(FlipperDatabaseHandler.FLIPPER_DATMAJ));
        enseigne.setNom(PO.getString(FlipperDatabaseHandler.ENSEIGNE_NOM));
        enseigne.setAdresse(PO.getString(FlipperDatabaseHandler.ENSEIGNE_ADRESSE));
        enseigne.setCodePostal(PO.getString(FlipperDatabaseHandler.ENSEIGNE_CODE_POSTAL));
        enseigne.setVille(PO.getString(FlipperDatabaseHandler.ENSEIGNE_VILLE));
        enseigne.setPays(PO.getString(FlipperDatabaseHandler.ENSEIGNE_PAYS));
        enseigne.setType(PO.getString(FlipperDatabaseHandler.ENSEIGNE_TYPE));
        enseigne.setHoraire(PO.getString(FlipperDatabaseHandler.ENSEIGNE_HORAIRE));
        //enseigne.setLatitude(String.valueOf(PO.getLong(FlipperDatabaseHandler.ENSEIGNE_LATITUDE)));
        //enseigne.setLongitude(String.valueOf(PO.getLong(FlipperDatabaseHandler.ENSEIGNE_LONGITUDE)));
        enseigne.setLatitude(String.valueOf(PO.getParseGeoPoint(FlipperDatabaseHandler.ENSEIGNE_GEOPOINT).getLatitude()));
        enseigne.setLongitude(String.valueOf(PO.getParseGeoPoint(FlipperDatabaseHandler.ENSEIGNE_GEOPOINT).getLongitude()));
        return enseigne;
    }

    public ModeleFlipper getModele(ParseObject PO){
        ModeleFlipper modeleFlipper = new ModeleFlipper();
        modeleFlipper.setId(PO.getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ID));
        modeleFlipper.setNom(PO.getString(FlipperDatabaseHandler.MODELE_FLIPPER_NOM));
        modeleFlipper.setMarque(PO.getString(FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE));
        modeleFlipper.setAnneeLancement(PO.getLong(FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT));
        modeleFlipper.setObjectId(PO.getObjectId());
        return modeleFlipper;
    }

    public Tournoi getTournoi(ParseObject PO){
        Tournoi tournoi = new Tournoi();
        tournoi.setId(PO.getLong(FlipperDatabaseHandler.TOUR_ID));
        tournoi.setNom(PO.getString(FlipperDatabaseHandler.TOUR_NOM));
        tournoi.setDate(PO.getString(FlipperDatabaseHandler.TOUR_DATE));
        tournoi.setCommentaire(PO.getString(FlipperDatabaseHandler.TOUR_COMMENTAIRE));
        tournoi.setLatitude(PO.getString(FlipperDatabaseHandler.TOUR_LATITUDE));
        tournoi.setLongitude(PO.getString(FlipperDatabaseHandler.TOUR_LONGITUDE));
        tournoi.setAdresse(PO.getString(FlipperDatabaseHandler.TOUR_ADRESSE));
        tournoi.setCodePostal(PO.getString(FlipperDatabaseHandler.TOUR_CODE_POSTAL));
        tournoi.setVille(PO.getString(FlipperDatabaseHandler.TOUR_VILLE));
        tournoi.setPays(PO.getString(FlipperDatabaseHandler.TOUR_PAYS));
        tournoi.setUrl(PO.getString(FlipperDatabaseHandler.TOUR_URL));
        tournoi.setEns(PO.getString(FlipperDatabaseHandler.TOUR_ENS));
        return tournoi;
    }

    public Commentaire getCommentaire(ParseObject PO){
        Commentaire commentaire = new Commentaire();
        commentaire.setId(PO.getLong(FlipperDatabaseHandler.COMM_ID));
        commentaire.setActif(PO.getBoolean(FlipperDatabaseHandler.COMM_ACTIF));
        commentaire.setFlipperId(PO.getLong(FlipperDatabaseHandler.COMM_FLIPPER_ID));
        commentaire.setDate(PO.getString(FlipperDatabaseHandler.COMM_DATE));
        commentaire.setTexte(PO.getString(FlipperDatabaseHandler.COMM_TEXTE));
        commentaire.setType(PO.getString(FlipperDatabaseHandler.COMM_TYPE));
        commentaire.setPseudo(PO.getString(FlipperDatabaseHandler.COMM_PSEUDO));
        return commentaire;
    }
}
