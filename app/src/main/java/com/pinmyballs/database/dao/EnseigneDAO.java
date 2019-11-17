package com.pinmyballs.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;

import com.pinmyballs.database.DAOBase;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.utils.LocationUtil;

import java.util.ArrayList;

public class EnseigneDAO extends DAOBase{

	public EnseigneDAO(Context pContext) {
		super(pContext);
	}
	public EnseigneDAO(SQLiteDatabase pDb) {
		super(pDb);
	}

	public ArrayList<Enseigne> getListEnseignePourDistance(PointF center, long distance){
		ArrayList<Enseigne> listeRetour = new ArrayList<Enseigne>();
		final double mult = 1; // mult = 1.1; is more reliable
		PointF p1 = LocationUtil.calculateDerivedPosition(center, mult * distance, 0);
		PointF p2 = LocationUtil.calculateDerivedPosition(center, mult * distance, 90);
		PointF p3 = LocationUtil.calculateDerivedPosition(center, mult * distance, 180);
		PointF p4 = LocationUtil.calculateDerivedPosition(center, mult * distance, 270);

		double fudge = Math.pow(Math.cos(Math.toRadians(center.x)),2);
		String strWhere =  " Where "
			+ "CAST(" +FlipperDatabaseHandler.ENSEIGNE_LATITUDE + " AS REAL)" + " > " + p3.x + " And "
			+ "CAST(" + FlipperDatabaseHandler.ENSEIGNE_LATITUDE + " AS REAL)" + " < " + p1.x + " And "
			+ "CAST(" + FlipperDatabaseHandler.ENSEIGNE_LONGITUDE + " AS REAL)" + " < " + p2.y + " And "
			+ "CAST(" + FlipperDatabaseHandler.ENSEIGNE_LONGITUDE + " AS REAL)" + " > " + p4.y;

		String strOrder =  " ORDER BY (("+center.x+" - ENS_LATITUDE) * ("+center.x+" - ENS_LATITUDE)" +
			" + ("+center.y+" - ENS_LONGITUDE) * ("+center.y+" - ENS_LONGITUDE) * "+fudge+")";

		Cursor cursor = mDb.rawQuery("select " + FlipperDatabaseHandler.ENSEIGNE_ID +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_TYPE +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_NOM +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_HORAIRE +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_LATITUDE +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_LONGITUDE +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_ADRESSE +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_CODE_POSTAL +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_VILLE +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_PAYS +
				" , " +  FlipperDatabaseHandler.ENSEIGNE_DATMAJ +
				" from " + FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME +
				strWhere + strOrder, null);

		while (cursor.moveToNext()) {
			// Faire quelque chose
			listeRetour.add(convertCursorToEnseigne(cursor));
		}
		cursor.close();
		return listeRetour;
	}

	private Enseigne convertCursorToEnseigne(Cursor c){
		Enseigne enseigne = new Enseigne();
		enseigne.setId(c.getLong(0));
		enseigne.setType(c.getString(1));
		enseigne.setNom(c.getString(2));
		enseigne.setHoraire(c.getString(3));
		enseigne.setLatitude(c.getString(4));
		enseigne.setLongitude(c.getString(5));
		enseigne.setAdresse(c.getString(6));
		enseigne.setCodePostal(c.getString(7));
		enseigne.setVille(c.getString(8));
		enseigne.setPays(c.getString(9));
		enseigne.setDateMaj(c.getString(10));
		return enseigne;
	}

	public void save(Enseigne enseigne){
		ContentValues contentValues = new ContentValues();
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_ID, enseigne.getId());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_TYPE, enseigne.getType());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_NOM, enseigne.getNom());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_HORAIRE, enseigne.getHoraire());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_LATITUDE, enseigne.getLatitude());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_LONGITUDE, enseigne.getLongitude());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_ADRESSE, enseigne.getAdresse());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_CODE_POSTAL, enseigne.getCodePostal());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_VILLE, enseigne.getVille());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_PAYS, enseigne.getPays());
		contentValues.put(FlipperDatabaseHandler.ENSEIGNE_DATMAJ, enseigne.getDateMaj());

		mDb.delete(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME, FlipperDatabaseHandler.ENSEIGNE_ID + "=?", new String[] { String.valueOf(enseigne.getId()) });
		mDb.insert(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME, null, contentValues);
	}

	public void truncate(){
		mDb.delete(FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME, null, null);
	}


}
