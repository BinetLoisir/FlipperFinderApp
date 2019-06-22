package com.pinmyballs.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pinmyballs.database.DAOBase;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Tournoi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TournoiDAO extends DAOBase{

	public TournoiDAO(Context pContext) {
		super(pContext);
	}

	public TournoiDAO(SQLiteDatabase pDb) {
		super(pDb);
	}

	public ArrayList<Tournoi> getAllTournoi(){
		ArrayList<Tournoi> listeRetour = new ArrayList<Tournoi>();

		//on ordonne par date
		String strOrder =  FlipperDatabaseHandler.TOUR_DATE + " DESC ";

		Cursor cursor = mDb.query(FlipperDatabaseHandler.TOURNOI_TABLE_NAME,
				new String[]{FlipperDatabaseHandler.TOUR_ID,
					FlipperDatabaseHandler.TOUR_NOM,
					FlipperDatabaseHandler.TOUR_COMMENTAIRE,
					FlipperDatabaseHandler.TOUR_DATE,
					FlipperDatabaseHandler.TOUR_LATITUDE,
					FlipperDatabaseHandler.TOUR_LONGITUDE,
					FlipperDatabaseHandler.TOUR_ADRESSE,
					FlipperDatabaseHandler.TOUR_CODE_POSTAL,
					FlipperDatabaseHandler.TOUR_VILLE,
					FlipperDatabaseHandler.TOUR_PAYS,
					FlipperDatabaseHandler.TOUR_URL,
					FlipperDatabaseHandler.TOUR_ENS},
					null,null, null, null, strOrder, null);

		while (cursor.moveToNext()) {
			listeRetour.add(convertCursorToModeleFlipper(cursor));
		}
		cursor.close();

		return listeRetour;
	}

	public ArrayList<Tournoi> getAllFutureTournoi(){
		ArrayList<Tournoi> listeRetour = new ArrayList<Tournoi>();

		//on ordonne par date
		String strOrder =  FlipperDatabaseHandler.TOUR_DATE + " DESC ";

		String datedujour = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE).format(new Date());
		String selection = FlipperDatabaseHandler.TOUR_DATE +" >= " +"'"+ datedujour+"'";

		Cursor cursor = mDb.query(FlipperDatabaseHandler.TOURNOI_TABLE_NAME,
				new String[]{FlipperDatabaseHandler.TOUR_ID,
						FlipperDatabaseHandler.TOUR_NOM,
						FlipperDatabaseHandler.TOUR_COMMENTAIRE,
						FlipperDatabaseHandler.TOUR_DATE,
						FlipperDatabaseHandler.TOUR_LATITUDE,
						FlipperDatabaseHandler.TOUR_LONGITUDE,
						FlipperDatabaseHandler.TOUR_ADRESSE,
						FlipperDatabaseHandler.TOUR_CODE_POSTAL,
						FlipperDatabaseHandler.TOUR_VILLE,
						FlipperDatabaseHandler.TOUR_PAYS,
						FlipperDatabaseHandler.TOUR_URL,
						FlipperDatabaseHandler.TOUR_ENS},
				selection,null, null, null, strOrder, null);

		while (cursor.moveToNext()) {
			listeRetour.add(convertCursorToModeleFlipper(cursor));
		}
		cursor.close();

		return listeRetour;
	}

	private Tournoi convertCursorToModeleFlipper(Cursor c){
		return new Tournoi(c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
				c.getString(4), c.getString(5), c.getString(6), c.getString(7),
				c.getString(8), c.getString(9), c.getString(10),c.getString(11));
	}

	public void save(Tournoi tournoi){
		ContentValues contentValues = new ContentValues();
		contentValues.put(FlipperDatabaseHandler.TOUR_ID, tournoi.getId());
		contentValues.put(FlipperDatabaseHandler.TOUR_NOM, tournoi.getNom());
		contentValues.put(FlipperDatabaseHandler.TOUR_COMMENTAIRE, tournoi.getCommentaire());
		contentValues.put(FlipperDatabaseHandler.TOUR_DATE, tournoi.getDate());
		contentValues.put(FlipperDatabaseHandler.TOUR_LATITUDE, tournoi.getLatitude());
		contentValues.put(FlipperDatabaseHandler.TOUR_LONGITUDE, tournoi.getLongitude());
		contentValues.put(FlipperDatabaseHandler.TOUR_ADRESSE, tournoi.getAdresse());
		contentValues.put(FlipperDatabaseHandler.TOUR_CODE_POSTAL, tournoi.getCodePostal());
		contentValues.put(FlipperDatabaseHandler.TOUR_VILLE, tournoi.getVille());
		contentValues.put(FlipperDatabaseHandler.TOUR_PAYS, tournoi.getPays());
		contentValues.put(FlipperDatabaseHandler.TOUR_URL, tournoi.getUrl());
		contentValues.put(FlipperDatabaseHandler.TOUR_ENS, tournoi.getEns());

		mDb.delete(FlipperDatabaseHandler.TOURNOI_TABLE_NAME, FlipperDatabaseHandler.TOUR_ID + "=?", new String[] { String.valueOf(tournoi.getId())});
		mDb.insert(FlipperDatabaseHandler.TOURNOI_TABLE_NAME, null, contentValues);
	}

	public void truncate(){
		mDb.delete(FlipperDatabaseHandler.TOURNOI_TABLE_NAME, null, null);
	}
}
