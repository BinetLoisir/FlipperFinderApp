package com.pinmyballs.service.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.pinmyballs.database.dao.TournoiDAO;
import com.pinmyballs.metier.Tournoi;

import java.util.ArrayList;
import java.util.List;

public class BaseTournoiService {

	public ArrayList<Tournoi> getAllTournoi(Context pContext){
		ArrayList<Tournoi> listeRetour;
		TournoiDAO tournoiDao = new TournoiDAO(pContext);
		tournoiDao.open();
		listeRetour = tournoiDao.getAllTournoi();
		tournoiDao.close();
		return listeRetour;
	}

	public ArrayList<Tournoi> getAllFutureTournoi(Context pContext){
		ArrayList<Tournoi> listeRetour;
		TournoiDAO tournoiDao = new TournoiDAO(pContext);
		tournoiDao.open();
		listeRetour = tournoiDao.getAllFutureTournoi();
		tournoiDao.close();
		return listeRetour;
	}

	public boolean initListeTournoi(List<Tournoi> listeObjets, SQLiteDatabase db){
		TournoiDAO tournoiDao = new TournoiDAO(db);
		for (Tournoi tournoi: listeObjets){
			tournoiDao.save(tournoi);
		}
		return true;
	}

	public boolean majListeTournoi(List<Tournoi> listeTournoi, Context pContext){
		return majListeTournoi(listeTournoi, pContext, false);
	}

	public boolean majListeTournoi(List<Tournoi> listeTournoi, Context pContext, boolean truncate){
		TournoiDAO tournoiDAO = new TournoiDAO(pContext);
		SQLiteDatabase db = tournoiDAO.open();
		db.beginTransaction();
		if (truncate){
			tournoiDAO.truncate();
		}
		for (Tournoi tournoi : listeTournoi){
			tournoiDAO.save(tournoi);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		tournoiDAO.close();
		return true;
	}

	public boolean remplaceListeTournoi(List<Tournoi> listeTournoi, Context pContext){
		if(listeTournoi == null){
			return true;
		}
		TournoiDAO tournoiDao = new TournoiDAO(pContext);
		SQLiteDatabase db = tournoiDao.open();
		db.beginTransaction();
		tournoiDao.truncate();
		for (Tournoi tournoi : listeTournoi){
			tournoiDao.save(tournoi);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		tournoiDao.close();
		return true;
	}

}
