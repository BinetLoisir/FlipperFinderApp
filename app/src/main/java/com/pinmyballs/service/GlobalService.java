package com.pinmyballs.service;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinmyballs.R;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.metier.Tournoi;
import com.pinmyballs.service.base.BaseCommentaireService;
import com.pinmyballs.service.base.BaseEnseigneService;
import com.pinmyballs.service.base.BaseFlipperService;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.service.base.BaseScoreService;
import com.pinmyballs.service.base.BaseTournoiService;
import com.pinmyballs.service.parse.ParseCommentaireService;
import com.pinmyballs.service.parse.ParseEnseigneService;
import com.pinmyballs.service.parse.ParseFlipperService;
import com.pinmyballs.service.parse.ParseModeleService;
import com.pinmyballs.service.parse.ParseScoreService;
import com.pinmyballs.service.parse.ParseTournoiService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalService {

	Context mContext = null;

	public GlobalService(Context context){
		mContext = context;
	}

	public GlobalService(){
	}

	/**
	 * Returns the X last Commentaires
	 * @param pContext contexte
	 * @param nbMaxCommentaire Number of comments to return
	 * @return
	 */
    public ArrayList<Commentaire> getLastCommentaire(Context pContext, int nbMaxCommentaire) {
        ArrayList<Commentaire> listeRetour;
        BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
        BaseFlipperService baseFlipperService = new BaseFlipperService();

        listeRetour = baseCommentaireService.getLastCommentaire(pContext, nbMaxCommentaire);

        for (Commentaire commentaire : listeRetour) {
            Flipper flipper = baseFlipperService.getFlipperById(pContext, commentaire.getFlipperId());
            commentaire.setFlipper(flipper);
        }
        return listeRetour;
    }

    public ArrayList<Commentaire> getLastCommentaireType(Context pContext, int nbMaxCommentaire, String type, boolean includeNull) {
        ArrayList<Commentaire> listeRetour;
        BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
        BaseFlipperService baseFlipperService = new BaseFlipperService();

        listeRetour = baseCommentaireService.getLastCommentaireType(pContext, nbMaxCommentaire, type, includeNull);

        for (Commentaire commentaire : listeRetour) {
            Flipper flipper = baseFlipperService.getFlipperById(pContext, commentaire.getFlipperId());
            commentaire.setFlipper(flipper);
        }
        return listeRetour;
    }

	/**
	 * Returns the list of score for a given flipperId
	 * @param pContext context
	 * @param flipperId flipperId
	 * @return
	 */
	public ArrayList<Score> getScorebyFlipper(Context pContext, Long flipperId){
		ArrayList<Score> listeRetour;
		BaseScoreService baseScoreService = new BaseScoreService();
		listeRetour = baseScoreService.getScoresByFlipperId(pContext, flipperId);
		return listeRetour;
	}

	public String getNbFlips(Context pContext){
		return new BaseFlipperService().NombreFlipperActifs(pContext);
	}

	public Flipper getFlip(Context pContext, long id){
		return new BaseFlipperService().getFlipperById(pContext, id);
	}

	public String majBaseAvecNouveaute(Context pContext, String dateDerniereMaj) throws InterruptedException{

		BaseModeleService baseModeleService = new BaseModeleService();
		ParseModeleService parseModeleService = new ParseModeleService();

		BaseEnseigneService baseEnseigneService = new BaseEnseigneService();
		ParseEnseigneService parseEnseigneService = new ParseEnseigneService();

		BaseFlipperService baseFlipperService = new BaseFlipperService();
		ParseFlipperService parseFlipperService = new ParseFlipperService(null);

		BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
		ParseCommentaireService parseCommentaireService = new ParseCommentaireService(null);

		BaseTournoiService baseTournoiService = new BaseTournoiService();
		ParseTournoiService parseTournoiService = new ParseTournoiService();

		BaseScoreService baseScoreService = new BaseScoreService();
		ParseScoreService parseScoreService = new ParseScoreService(null);

		Long idMaxModele = baseModeleService.getIdMaxModele(pContext);

		// On récupère les données du cloud
		List<ModeleFlipper> nvlleListeModele = parseModeleService.getMajModeleById(idMaxModele);
		List<Enseigne> nvlleListeEnseigne = parseEnseigneService.getMajEnseigneByDate(dateDerniereMaj);
		List<Flipper> nvlleListeFlipper = parseFlipperService.getMajFlipperByDate(dateDerniereMaj);
		List<Commentaire> nvlleListeCommentaire = parseCommentaireService.getMajCommentaireByDate(dateDerniereMaj);
		List<Tournoi> nvlleListeTournoi = parseTournoiService.getAllTournoi();
		List<Score> nvelleListeScore = parseScoreService.getMajScoreByDate(dateDerniereMaj);
		//List<Score> nvelleListeScore = parseScoreService.getAllScores();

		boolean maj = false;
		String textPopup = "";

		// On met à jour la table des enseignes
		if (nvlleListeEnseigne != null && nvlleListeEnseigne.size() > 0){
			maj = true;
			textPopup = pContext.getResources().getString(R.string.toastMajEnseigne, nvlleListeEnseigne.size()) + "\n";
			baseEnseigneService.majListeEnseigne(nvlleListeEnseigne, pContext);
		}

		// On met à jour la table des modèles de flipper
		if (nvlleListeModele != null && nvlleListeModele.size() > 0){
			maj = true;
			textPopup += pContext.getResources().getString(R.string.toastMajModele, nvlleListeModele.size())+ "\n";
			baseModeleService.majListeModele(nvlleListeModele, pContext);
		}

		// On met à jour la table des flippers
		if (nvlleListeFlipper != null && nvlleListeFlipper.size() > 0){
			maj = true;
			textPopup += pContext.getResources().getString(R.string.toastMajFlipper, nvlleListeFlipper.size())+ "\n";
			baseFlipperService.majListeFlipper(nvlleListeFlipper, pContext);
		}

		// On met à jour la table des commentaires
		if (nvlleListeCommentaire != null && nvlleListeCommentaire.size() > 0){
			maj = true;
			textPopup += pContext.getResources().getString(R.string.toastMajCommentaire, nvlleListeCommentaire.size())+ "\n";
			baseCommentaireService.majListeCommentaire(nvlleListeCommentaire, pContext);
		}

		// On met à jour la table des tournois
		baseTournoiService.remplaceListeTournoi(nvlleListeTournoi, pContext);

        // On met à jour la table des scores
        if (nvelleListeScore != null && nvelleListeScore.size() > 0){
            maj = true;
            textPopup += pContext.getResources().getString(R.string.toastMajScore, nvelleListeScore.size());
            baseScoreService.majListeScore(nvelleListeScore,pContext);
        }

		// S'il y a eu une mise à jour, on envoie le récap en sortie.
		if (maj){
			return textPopup;
		}
		return null;
	}

	/*
	Methods to populate Database from JSON Files
	Deprecated
	 */

	private void populateEnseigne(SQLiteDatabase db) {
		BaseEnseigneService baseEnseigneService = new BaseEnseigneService();
		List<Enseigne> returnList;
		Gson gson = new GsonBuilder().create();
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("ENSEIGNE.json"), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		returnList = Arrays.asList(gson.fromJson(reader, Enseigne[].class));
		baseEnseigneService.initListeEnseigne(returnList, db);
	}

	private void populateFlipper(SQLiteDatabase db) {
		BaseFlipperService baseFlipperService= new BaseFlipperService();
		List<Flipper> returnList;
		Gson gson = new GsonBuilder().create();
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("FLIPPER.json"), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		returnList = Arrays.asList(gson.fromJson(reader, Flipper[].class));
		baseFlipperService.initListeFlipper(returnList, db);
	}

	private void populateCommentaire(SQLiteDatabase db) {
		BaseCommentaireService baseCommentaireService= new BaseCommentaireService();
		List<Commentaire> returnList;
		Gson gson = new GsonBuilder().create();
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("COMMENTAIRE.json"), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		returnList = Arrays.asList(gson.fromJson(reader, Commentaire[].class));
		baseCommentaireService.initListeCommentaire(returnList, db);
	}

	private void populateModele(SQLiteDatabase db) {
		BaseModeleService baseModeleService= new BaseModeleService();
		List<ModeleFlipper> returnList;
		Gson gson = new GsonBuilder().create();
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("MODELE_FLIPPER.json"), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		returnList = Arrays.asList(gson.fromJson(reader, ModeleFlipper[].class));
		baseModeleService.initListModele(returnList, db);
	}

	private void populateTournoi(SQLiteDatabase db) {
		BaseTournoiService baseTournoiService = new BaseTournoiService();
		List<Tournoi> returnList;
		Gson gson = new GsonBuilder().create();
		Reader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("TOURNOI.json"), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		returnList = Arrays.asList(gson.fromJson(reader, Tournoi[].class));
		baseTournoiService.initListeTournoi(returnList, db);
	}

	public void reinitDatabase(SQLiteDatabase db){
		populateModele(db);
		populateEnseigne(db);
		populateFlipper(db);
		populateCommentaire(db);
		populateTournoi(db);
	}


}
