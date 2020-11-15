package com.pinmyballs.service;

import android.content.Context;

import com.pinmyballs.R;
import com.pinmyballs.fragment.FragmentActionsFlipper.FragmentActionCallback;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.service.parse.ParseFlipperService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FlipperService {

    private FragmentActionCallback mFragmentCallback;

    public FlipperService(FragmentActionCallback fragmentCallback) {
        mFragmentCallback = fragmentCallback;
    }

    public boolean remplaceToutFlipper(Context pContext) {
        boolean retour;
        BaseFlipperService baseFlipperService = new BaseFlipperService();
        ParseFlipperService parseFlipperService = new ParseFlipperService(null);
        List<Flipper> nvlleListe = parseFlipperService.getAllFlipper();
        retour = baseFlipperService.majListeFlipper(nvlleListe, pContext);
        return retour;
    }


    public void valideFlipper(Context pContext, Flipper flipper) {
        ParseFlipperService parseFlipperService = new ParseFlipperService(mFragmentCallback);
        final String dateToSave = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(new Date());
        parseFlipperService.updateDateFlipper(pContext, flipper, dateToSave);
    }


    //remplace supprimeflip
    public void modifieEtatFlip(Context pContext, Flipper flipper) {

        Date dateDuJour = new Date();
        String dateMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour);
        flipper.setDateMaj(dateMaj);
        flipper.setActif(flipper.isActif() ? 0 : 1);
        // Update of MongoDb
        ParseFlipperService parseFlipperService = new ParseFlipperService(mFragmentCallback);
        parseFlipperService.modifieEtatFlipper(pContext, flipper);
    }


    public void remplaceFlipper(Context pContext, Flipper flipper, long idNouveauModele, String commentaire, String pseudo) {
        //Get current date
        Date dateDuJour = new Date();
        String dateMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour);

        //Deactivate old model
        flipper.setDateMaj(dateMaj);
        flipper.setActif(0);

        //Making new Flipper & Commentaire
        long flipID = dateDuJour.getTime();
        Flipper nouveauFlipper = new Flipper(flipID, idNouveauModele, "?", flipper.getIdEnseigne(), true, dateMaj);
        nouveauFlipper.setModele(new BaseModeleService().getModeleById(pContext,idNouveauModele));
        nouveauFlipper.setEnseigne(flipper.getEnseigne());
        Commentaire commentaireToAdd = null;
        if (commentaire != null && commentaire.length() > 0) {
            commentaireToAdd = new Commentaire(dateDuJour.getTime(), dateDuJour.getTime(), commentaire, Commentaire.TYPE_REPLACE, dateMaj,
                    pseudo, true);
        }

        ParseFlipperService parseFlipperService = new ParseFlipperService(mFragmentCallback);
        parseFlipperService.remplaceModeleFlipper(pContext, flipper, nouveauFlipper, commentaireToAdd);

    }

    public void rajouterFlipperSurFlipper(Context pContext, Flipper flipper, long idNouveauModele, String commentaireString, String pseudo) {
        //Get current date
        Date dateDuJour = new Date();
        String dateMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour);

        //Making new Flipper & Commentaire
        long flipID = dateDuJour.getTime();
        Flipper nouveauFlipper = new Flipper(flipID, idNouveauModele, "?", flipper.getIdEnseigne(), true, dateMaj);
        //modele
        nouveauFlipper.setModele(new BaseModeleService().getModeleById(pContext, idNouveauModele));
        //enseigne
        nouveauFlipper.setEnseigne(flipper.getEnseigne());

        //commentaire
        Commentaire commentaire = new Commentaire();
        if (commentaireString != null && commentaireString.length() > 0) {
            commentaire = new Commentaire(dateDuJour.getTime(), flipID, commentaireString, Commentaire.TYPE_ADDITIONAL, dateMaj, pseudo, true);
            commentaire.setFlipper(flipper);
        }

        ParseFlipperService parseFlipperService = new ParseFlipperService(mFragmentCallback);
        parseFlipperService.ajouterFlipper(pContext, nouveauFlipper, commentaire);

    }

    public void modifieExploitant(Context pContext, Flipper flipper, String exploitant, String pseudo){

        flipper.setExploitant(exploitant);
        Date dateDuJour = new Date();
        String dateMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour);
        flipper.setDateMaj(dateMaj);

        //commentaire
        String comment = pContext.getString(R.string.comment_typ_exploitant)+ exploitant;
        Commentaire commentaire = new Commentaire(dateDuJour.getTime(), flipper.getId(), comment, Commentaire.TYPE_POST, dateMaj, pseudo, true);
        commentaire.setFlipper(flipper);

        // Update of MongoDb
        ParseFlipperService parseFlipperService = new ParseFlipperService(mFragmentCallback);
        //ParseCommentaireService parseCommentaireService = new ParseCommentaireService(mFragmentCallback);
        //TODO AJOUTER UN COMMENTAIRE A CHAQUE RENSEIGNEMENT d EXPLOITANT
        parseFlipperService.modifieEtatFlipper(pContext, flipper);
        //parseCommentaireService.ajouteCommentaire(pContext, commentaire);

        //Update of Sqlite database
        //TODO Update de la db locale directement

    }



    public void modifieNbCredits(Context pContext, Flipper flipper, String nbcredits, String pseudo){

        flipper.setNbCreditsDeuxEuros((nbcredits));
        //TODO check if above formula is correct and error proof
        Date dateDuJour = new Date();
        String dateMaj = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(dateDuJour);
        flipper.setDateMaj(dateMaj);

        // Update of MongoDb
        ParseFlipperService parseFlipperService = new ParseFlipperService(mFragmentCallback);
        parseFlipperService.modifieEtatFlipper(pContext, flipper);
    }

}
