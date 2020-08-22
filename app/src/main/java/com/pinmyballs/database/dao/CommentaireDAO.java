package com.pinmyballs.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pinmyballs.database.DAOBase;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Commentaire;

import java.util.ArrayList;

public class CommentaireDAO extends DAOBase{

    private static final String TAG = "CommentaireDAO";

	public CommentaireDAO(Context pContext) {
		super(pContext);
	}

	public CommentaireDAO(SQLiteDatabase pDb) {
		super(pDb);
	}

    public ArrayList<Commentaire> getLastCommentaire(int nbMaxCommentaire) {
        ArrayList<Commentaire> listeRetour = new ArrayList<>();
        String strWhere = " Where " + FlipperDatabaseHandler.COMM_ACTIF + " = 1 ";
        String strOrder = " ORDER BY " + FlipperDatabaseHandler.COMM_DATE + " DESC, " + FlipperDatabaseHandler.COMM_ID + " DESC ";

        Cursor cursor = mDb.rawQuery("select " + FlipperDatabaseHandler.COMM_ID +
                " , " + FlipperDatabaseHandler.COMM_FLIPPER_ID +
                " , " + FlipperDatabaseHandler.COMM_TEXTE +
                " , " + FlipperDatabaseHandler.COMM_TYPE +
                " , " + FlipperDatabaseHandler.COMM_PSEUDO +
                " , " + FlipperDatabaseHandler.COMM_DATE +
                " , " + FlipperDatabaseHandler.COMM_ACTIF +
                " from " + FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME +
                strWhere + strOrder, null);

        int i = 0;
        while (cursor.moveToNext() && i++ < nbMaxCommentaire) {
            listeRetour.add(convertCursorToCommentaire(cursor));
        }
        cursor.close();

        return listeRetour;
    }

    public ArrayList<Commentaire> getLastCommentaireType(int nbMaxCommentaire, String type, boolean includeNull) {
        ArrayList<Commentaire> listeRetour = new ArrayList<>();
        String strWhere;
        if (!includeNull) {
            strWhere = " WHERE " + FlipperDatabaseHandler.COMM_ACTIF + " = 1 " +
                    " AND " + FlipperDatabaseHandler.COMM_TYPE + " = '" + type + "'";

        } else {
            strWhere = " WHERE " + FlipperDatabaseHandler.COMM_ACTIF + " = 1 " +
                    " AND (" + FlipperDatabaseHandler.COMM_TYPE + " = '" + type + "'" +
                    " OR " + FlipperDatabaseHandler.COMM_TYPE + " IS NULL ) ";
        }
        Log.d(TAG, "getLastCommentaireType: SQL where " + strWhere);

        String strOrder = " ORDER BY " + FlipperDatabaseHandler.COMM_DATE + " DESC, " + FlipperDatabaseHandler.COMM_ID + " DESC ";

        Cursor cursor = mDb.rawQuery("select " + FlipperDatabaseHandler.COMM_ID +
                " , " + FlipperDatabaseHandler.COMM_FLIPPER_ID +
                " , " + FlipperDatabaseHandler.COMM_TEXTE +
                " , " + FlipperDatabaseHandler.COMM_TYPE +
                " , " + FlipperDatabaseHandler.COMM_PSEUDO +
                " , " + FlipperDatabaseHandler.COMM_DATE +
                " , " + FlipperDatabaseHandler.COMM_ACTIF +
                " from " + FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME +
                strWhere + strOrder, null);

        int i = 0;
        while (cursor.moveToNext() && i++ < nbMaxCommentaire) {
            listeRetour.add(convertCursorToCommentaire(cursor));
        }
        cursor.close();

        return listeRetour;
    }


//TODO commentaires geolocalisés
	/*public ArrayList<Commentaire> getLastCommentaireAround(int nbMaxCommentaire, PointF center, long distance){
		ArrayList<Commentaire> listeRetour = new ArrayList<Commentaire>();
		String strSelection = "SELECT"
				+ FlipperDatabaseHandler.FLIPPER_ID + " , "
				+ FlipperDatabaseHandler.FLIPPER_MODELE + " , "
				+ FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E + " , "
				+ FlipperDatabaseHandler.FLIPPER_ENSEIGNE + " , "
				+ FlipperDatabaseHandler.FLIPPER_DATMAJ + " , "
				+ FlipperDatabaseHandler.FLIPPER_ACTIF + " , "
				+ FlipperDatabaseHandler.COMM_ID + " , "
				+ FlipperDatabaseHandler.COMM_FLIPPER_ID + " , "
				+ FlipperDatabaseHandler.COMM_TEXTE + " , "
				+ FlipperDatabaseHandler.COMM_PSEUDO + " , "
				+ FlipperDatabaseHandler.COMM_DATE + " , "
				+ FlipperDatabaseHandler.COMM_ACTIF + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_ID + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_TYPE + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_NOM + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_HORAIRE + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_LATITUDE + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_LONGITUDE + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_ADRESSE + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_CODE_POSTAL + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_VILLE + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_PAYS + " , "
				+ FlipperDatabaseHandler.ENSEIGNE_DATMAJ; //TODO


		String strFrom = "FROM"
				+ FlipperDatabaseHandler.FLIPPER_TABLE_NAME
				+ " INNER JOIN "
				+ FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME
				+ " ON "
				+ FlipperDatabaseHandler.FLIPPER_MODELE
				+ " = "
				+ FlipperDatabaseHandler.COMM_FLIPPER_ID
				+ " INNER JOIN "
				+ FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME
				+ " ON "
				+ FlipperDatabaseHandler.ENSEIGNE_ID
				+ " = "
				+ FlipperDatabaseHandler.FLIPPER_ENSEIGNE;


		// On commence par la clause WHERE en fonction de la distance
		final double mult = 1; // mult = 1.1; is more reliable
		PointF p1 = LocationUtil.calculateDerivedPosition(center, mult * distance, 0);
		PointF p2 = LocationUtil.calculateDerivedPosition(center, mult * distance, 90);
		PointF p3 = LocationUtil.calculateDerivedPosition(center, mult * distance, 180);
		PointF p4 = LocationUtil.calculateDerivedPosition(center, mult * distance, 270);
		double fudge = Math.pow(Math.cos(Math.toRadians(center.x)), 2);

		String strWhereEnseigne = " Where " + "CAST(" + FlipperDatabaseHandler.ENSEIGNE_LATITUDE + " AS REAL)" + " > "
				+ String.valueOf(p3.x) + " And " + "CAST(" + FlipperDatabaseHandler.ENSEIGNE_LATITUDE + " AS REAL)"
				+ " < " + String.valueOf(p1.x) + " And " + "CAST(" + FlipperDatabaseHandler.ENSEIGNE_LONGITUDE
				+ " AS REAL)" + " < " + String.valueOf(p2.y) + " And " + "CAST("
				+ FlipperDatabaseHandler.ENSEIGNE_LONGITUDE + " AS REAL)" + " > " + String.valueOf(p4.y);

		// On ne garde que les commentaires actifs
		String strWhereComm =  " AND " + FlipperDatabaseHandler.COMM_ACTIF + " = 1 ";


		// On ordonne par date
		String strOrder =  " ORDER BY " + FlipperDatabaseHandler.COMM_DATE + " DESC, " + FlipperDatabaseHandler.COMM_ID+ " DESC ";



		//Cursor cursor2 = mDb.rawQuery(strSelection + strFrom + + strWhereEnseigne + strWhereComm + strOrder,null);

		Cursor cursor = mDb.rawQuery("select " + FlipperDatabaseHandler.COMM_ID +
				" , " +  FlipperDatabaseHandler.COMM_FLIPPER_ID +
				" , " +  FlipperDatabaseHandler.COMM_TEXTE +
				" , " +  FlipperDatabaseHandler.COMM_PSEUDO +
				" , " +  FlipperDatabaseHandler.COMM_DATE +
				" , " +  FlipperDatabaseHandler.COMM_ACTIF +
				" from " + FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME +
				strWhere + strOrder, null);

		int i = 0;
		while (cursor.moveToNext() && i++ < nbMaxCommentaire) {
			listeRetour.add(convertCursorToCommentaire(cursor));
		}
		cursor.close();

		return listeRetour;
	}*/

	public ArrayList<Commentaire> getCommentairePourFlipperId(long flipperId){
		ArrayList<Commentaire> listeRetour = new ArrayList<>();
		String strWhere =  " Where "
			+ FlipperDatabaseHandler.COMM_FLIPPER_ID + " = " + flipperId
			+ " AND "+ FlipperDatabaseHandler.COMM_ACTIF+ " = 1 ";
		String strOrder =  " ORDER BY " + FlipperDatabaseHandler.COMM_DATE + " DESC ";

		Cursor cursor = mDb.rawQuery("select " + FlipperDatabaseHandler.COMM_ID +
				" , " +  FlipperDatabaseHandler.COMM_FLIPPER_ID +
				" , " +  FlipperDatabaseHandler.COMM_TEXTE +
                " , " + FlipperDatabaseHandler.COMM_TYPE +
				" , " +  FlipperDatabaseHandler.COMM_PSEUDO +
				" , " +  FlipperDatabaseHandler.COMM_DATE +
				" , " +  FlipperDatabaseHandler.COMM_ACTIF +
				" from " + FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME +
				strWhere + strOrder, null);

		while (cursor.moveToNext()) {
			listeRetour.add(convertCursorToCommentaire(cursor));
		}
		cursor.close();
		return listeRetour;
	}

	private Commentaire convertCursorToCommentaire(Cursor c){
		Commentaire commentaire = new Commentaire();
		commentaire.setId(c.getLong(0));
		commentaire.setFlipperId(c.getLong(1));
		commentaire.setTexte(c.getString(2));
        commentaire.setType(c.getString(3));
        commentaire.setPseudo(c.getString(4));
        commentaire.setDate(c.getString(5));
        commentaire.setActif(c.getLong(6));
		return commentaire;
	}

	public void save(Commentaire commentaire) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(FlipperDatabaseHandler.COMM_ID, commentaire.getId());
		contentValues.put(FlipperDatabaseHandler.COMM_DATE, commentaire.getDate());
		contentValues.put(FlipperDatabaseHandler.COMM_FLIPPER_ID, commentaire.getFlipperId());
		contentValues.put(FlipperDatabaseHandler.COMM_PSEUDO, commentaire.getPseudo());
		contentValues.put(FlipperDatabaseHandler.COMM_TEXTE, commentaire.getTexte());
        contentValues.put(FlipperDatabaseHandler.COMM_TYPE, commentaire.getType());
		contentValues.put(FlipperDatabaseHandler.COMM_ACTIF, commentaire.getActif());

		mDb.delete(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME, FlipperDatabaseHandler.COMM_ID + "=?", new String[]{String.valueOf(commentaire.getId())});
		mDb.insert(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME, null, contentValues);
	}

	public void truncate(){
		mDb.delete(FlipperDatabaseHandler.COMMENTAIRE_TABLE_NAME, null, null);
	}

}
