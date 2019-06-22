package com.pinmyballs.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;

import com.pinmyballs.database.DAOBase;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;
import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.utils.LocationUtil;

import java.util.ArrayList;

public class FlipperDAO extends DAOBase {

    public FlipperDAO(Context pContext) {
        super(pContext);
    }

    public FlipperDAO(SQLiteDatabase pDb) {
        super(pDb);
    }

    /**
     * Retourne la liste des flippers pour une enseigne donnée
     *
     * @param enseigne
     * @return
     */
    public ArrayList<Flipper> getFlipperByEnseigne(Enseigne enseigne) {
        ArrayList<Flipper> listeRetour = new ArrayList<Flipper>();

        // On ne prend que les flippers actifs
        String strActif = " AND " + FlipperDatabaseHandler.FLIPPER_ACTIF + " = 1 ";


        Cursor cursor = mDb.query(FlipperDatabaseHandler.FLIPPER_TABLE_NAME + " INNER JOIN "
                        + FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME + " ON " + FlipperDatabaseHandler.FLIPPER_MODELE
                        + " = " + FlipperDatabaseHandler.MODELE_FLIPPER_ID,
                new String[]{FlipperDatabaseHandler.FLIPPER_ID,
                        FlipperDatabaseHandler.FLIPPER_MODELE,
                        FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E,
                        FlipperDatabaseHandler.FLIPPER_ENSEIGNE,
                        FlipperDatabaseHandler.FLIPPER_DATMAJ,
                        FlipperDatabaseHandler.FLIPPER_ACTIF,
                        FlipperDatabaseHandler.MODELE_FLIPPER_ID,
                        FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE,
                        FlipperDatabaseHandler.MODELE_FLIPPER_NOM,
                        FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT,
                        FlipperDatabaseHandler.MODELE_FLIPPER_OBJ_ID},
                FlipperDatabaseHandler.FLIPPER_ENSEIGNE + "=?" + strActif,
                new String[]{String.valueOf(enseigne.getId())},
                null, null, null);

        while (cursor.moveToNext()) {
            listeRetour.add(convertCursorToFlipper(cursor, enseigne));
        }
        cursor.close();

        return listeRetour;
    }

    public String getNbFlipperActif() {
        String NbFlip;

        //Cursor cursor = mDb.rawQuery("SELECT * FROM " + FlipperDatabaseHandler.FLIPPER_TABLE_NAME
        //							+ " WHERE " + FlipperDatabaseHandler.FLIPPER_ACTIF  + " = " + "1", null);

        Cursor cursor = mDb.rawQuery("SELECT COUNT(*) FROM " + FlipperDatabaseHandler.FLIPPER_TABLE_NAME
                + " WHERE " + FlipperDatabaseHandler.FLIPPER_ACTIF + " = " + "1", null);
        cursor.moveToFirst();
        NbFlip = String.valueOf(cursor.getInt(0));

        cursor.close();

        return NbFlip;
    }

    public String getNbFlipperActif(Enseigne enseigne) {
        String NbFlip;
        String strEnseigneId = String.valueOf(enseigne.getId());

        //Cursor cursor = mDb.rawQuery("SELECT * FROM " + FlipperDatabaseHandler.FLIPPER_TABLE_NAME
        //							+ " WHERE " + FlipperDatabaseHandler.FLIPPER_ACTIF  + " = " + "1", null);

        Cursor cursor = mDb.rawQuery("SELECT COUNT(*) FROM " + FlipperDatabaseHandler.FLIPPER_TABLE_NAME
                + " WHERE " + FlipperDatabaseHandler.FLIPPER_ACTIF + " = " + " 1 "
                + "AND " + FlipperDatabaseHandler.FLIPPER_ENSEIGNE + " = " + strEnseigneId, null);
        cursor.moveToFirst();
        NbFlip = String.valueOf(cursor.getInt(0));

        cursor.close();

        return NbFlip;
    }

    /**
     * Retourne la liste des autres flippers pour un flipper donné
     *
     * @param flipper
     * @return
     */
    public ArrayList<Flipper> getOtherFlippers(Flipper flipper) {
        ArrayList<Flipper> listeRetour = new ArrayList<Flipper>();

        // On ne prend que les flippers actifs
        String strActif = " AND " + FlipperDatabaseHandler.FLIPPER_ACTIF + " = 1 ";

        // On ne prend que les flippers actifs
        String flipId = String.valueOf(flipper.getId());
        String strAutre = " AND " + FlipperDatabaseHandler.FLIPPER_ID + " <> " + flipId + " ";

        Enseigne enseigne = flipper.getEnseigne();


        Cursor cursor = mDb.query(FlipperDatabaseHandler.FLIPPER_TABLE_NAME + " INNER JOIN "
                        + FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME + " ON " + FlipperDatabaseHandler.FLIPPER_MODELE
                        + " = " + FlipperDatabaseHandler.MODELE_FLIPPER_ID,
                new String[]{FlipperDatabaseHandler.FLIPPER_ID,
                        FlipperDatabaseHandler.FLIPPER_MODELE,
                        FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E,
                        FlipperDatabaseHandler.FLIPPER_ENSEIGNE,
                        FlipperDatabaseHandler.FLIPPER_DATMAJ,
                        FlipperDatabaseHandler.FLIPPER_ACTIF,
                        FlipperDatabaseHandler.MODELE_FLIPPER_ID,
                        FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE,
                        FlipperDatabaseHandler.MODELE_FLIPPER_NOM,
                        FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT,
                        FlipperDatabaseHandler.MODELE_FLIPPER_OBJ_ID},
                FlipperDatabaseHandler.FLIPPER_ENSEIGNE + "=?" + strActif + strAutre,
                new String[]{String.valueOf(enseigne.getId())},
                null, null, null);

        while (cursor.moveToNext()) {
            listeRetour.add(convertCursorToFlipper(cursor, enseigne));
        }
        cursor.close();

        return listeRetour;
    }


    public Flipper getFlipperById(long idFlipper) {
        Flipper flipperRetour = null;
        String strWhereFlipper = " Where " + FlipperDatabaseHandler.FLIPPER_ID + "=" + idFlipper;

        Cursor cursor = mDb.rawQuery("SELECT " + FlipperDatabaseHandler.FLIPPER_ID + " , "
                + FlipperDatabaseHandler.FLIPPER_MODELE + " , " + FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E + " , "
                + FlipperDatabaseHandler.FLIPPER_ENSEIGNE + " , " + FlipperDatabaseHandler.FLIPPER_DATMAJ + " , "
                + FlipperDatabaseHandler.FLIPPER_ACTIF + " , " + FlipperDatabaseHandler.MODELE_FLIPPER_ID + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE + " , " + FlipperDatabaseHandler.MODELE_FLIPPER_NOM + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_OBJ_ID + " , "
                + FlipperDatabaseHandler.ENSEIGNE_ID + " , " + FlipperDatabaseHandler.ENSEIGNE_TYPE + " , "
                + FlipperDatabaseHandler.ENSEIGNE_NOM + " , " + FlipperDatabaseHandler.ENSEIGNE_HORAIRE + " , "
                + FlipperDatabaseHandler.ENSEIGNE_LATITUDE + " , " + FlipperDatabaseHandler.ENSEIGNE_LONGITUDE + " , "
                + FlipperDatabaseHandler.ENSEIGNE_ADRESSE + " , " + FlipperDatabaseHandler.ENSEIGNE_CODE_POSTAL + " , "
                + FlipperDatabaseHandler.ENSEIGNE_VILLE + " , " + FlipperDatabaseHandler.ENSEIGNE_PAYS + " , "
                + FlipperDatabaseHandler.ENSEIGNE_DATMAJ + " FROM "
                + FlipperDatabaseHandler.FLIPPER_TABLE_NAME + " INNER JOIN "
                + FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME + " ON " + FlipperDatabaseHandler.FLIPPER_MODELE
                + " = " + FlipperDatabaseHandler.MODELE_FLIPPER_ID + " INNER JOIN "
                + FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME + " ON " + FlipperDatabaseHandler.ENSEIGNE_ID + " = "
                + FlipperDatabaseHandler.FLIPPER_ENSEIGNE + strWhereFlipper, null);

        if (cursor.moveToNext()) {
            flipperRetour = convertBigCursorToFlipper(cursor);
        }
        cursor.close();

        return flipperRetour;
    }

    /**
     * Requête de la mort qui ramène les enseignes les flippers et les modèles à
     * partir d'un point et d'une distance
     *
     * @param center
     * @param distance
     * @param modele   un éventuel filtre sur le modele
     * @return
     */
    public ArrayList<Flipper> getFlipperByDistance(PointF center, long distance, String modele) {
        ArrayList<Flipper> listeRetour = new ArrayList<Flipper>();

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

        String strAndModele = "";
        if (modele != null && modele.length() > 0) {
            modele = modele.replace("'", "''");
            strAndModele = " AND UPPER(" + FlipperDatabaseHandler.MODELE_FLIPPER_NOM + ") = '" + modele.toUpperCase()
                    + "' ";
        }
        // On ordonne par distance
        String strOrder = " ORDER BY ((" + center.x + " - ENS_LATITUDE) * (" + center.x + " - ENS_LATITUDE)" + " + (" + center.y
                + " - ENS_LONGITUDE) * (" + center.y + " - ENS_LONGITUDE) * " + fudge + ")";

        // On ne prend que les flippers actifs
        String strActif = " AND " + FlipperDatabaseHandler.FLIPPER_ACTIF + " = 1 ";

        // Et on balance la purée
        Cursor cursor = mDb.rawQuery("SELECT "
                + FlipperDatabaseHandler.FLIPPER_ID + " , "
                + FlipperDatabaseHandler.FLIPPER_MODELE + " , "
                + FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E + " , "
                + FlipperDatabaseHandler.FLIPPER_ENSEIGNE + " , "
                + FlipperDatabaseHandler.FLIPPER_DATMAJ + " , "
                + FlipperDatabaseHandler.FLIPPER_ACTIF + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_ID + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_MARQUE + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_NOM + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_ANNEE_LANCEMENT + " , "
                + FlipperDatabaseHandler.MODELE_FLIPPER_OBJ_ID + " , "
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
                + FlipperDatabaseHandler.ENSEIGNE_DATMAJ + " FROM "
                + FlipperDatabaseHandler.FLIPPER_TABLE_NAME + " INNER JOIN "
                + FlipperDatabaseHandler.MODELE_FLIPPER_TABLE_NAME + " ON " + FlipperDatabaseHandler.FLIPPER_MODELE
                + " = " + FlipperDatabaseHandler.MODELE_FLIPPER_ID + " INNER JOIN "
                + FlipperDatabaseHandler.ENSEIGNE_TABLE_NAME + " ON " + FlipperDatabaseHandler.ENSEIGNE_ID + " = "
                + FlipperDatabaseHandler.FLIPPER_ENSEIGNE + strWhereEnseigne + strAndModele + strActif + strOrder, null);

        while (cursor.moveToNext()) {
            listeRetour.add(convertBigCursorToFlipper(cursor));
        }
        cursor.close();

        return listeRetour;
    }

    public void save(Flipper flipper) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FlipperDatabaseHandler.FLIPPER_ID, flipper.getId());
        contentValues.put(FlipperDatabaseHandler.FLIPPER_MODELE, flipper.getIdModele());
        contentValues.put(FlipperDatabaseHandler.FLIPPER_NB_CREDITS_2E, flipper.getNbCreditsDeuxEuros());
        contentValues.put(FlipperDatabaseHandler.FLIPPER_ENSEIGNE, flipper.getIdEnseigne());
        contentValues.put(FlipperDatabaseHandler.FLIPPER_ACTIF, flipper.isActif());
        contentValues.put(FlipperDatabaseHandler.FLIPPER_DATMAJ, flipper.getDateMaj());

        mDb.delete(FlipperDatabaseHandler.FLIPPER_TABLE_NAME, FlipperDatabaseHandler.FLIPPER_ID + "=?", new String[]{String.valueOf(flipper.getId())});
        mDb.insert(FlipperDatabaseHandler.FLIPPER_TABLE_NAME, null, contentValues);
    }

    public void truncate() {
        mDb.delete(FlipperDatabaseHandler.FLIPPER_TABLE_NAME, null, null);
    }

    private Flipper convertBigCursorToFlipper(Cursor c) {
        ModeleFlipper modele = new ModeleFlipper(c.getLong(6), c.getString(8), c.getString(7), c.getLong(9), c.getString(10));

        Enseigne enseigne = new Enseigne(c.getLong(11), c.getString(12), c.getString(13), c.getString(14),
                c.getString(15), c.getString(16), c.getString(17), c.getString(18), c.getString(19), c.getString(20), c.getString(21));

        Flipper flipper = new Flipper();
        flipper.setId(c.getLong(0));
        flipper.setModele(modele);
        flipper.setEnseigne(enseigne);
        flipper.setNbCreditsDeuxEuros(c.getLong(2));
        flipper.setDateMaj(c.getString(4));
        //flipper.getDateMaj();
        flipper.setActif(c.getLong(5));
        return flipper;
    }

    private Flipper convertCursorToFlipper(Cursor c, Enseigne enseigne) {
        ModeleFlipper modele = new ModeleFlipper(c.getLong(6), c.getString(8), c.getString(7), c.getLong(9), c.getString(10));

        Flipper flipper = new Flipper();
        flipper.setId(c.getLong(0));
        flipper.setModele(modele);
        flipper.setEnseigne(enseigne);
        flipper.setNbCreditsDeuxEuros(c.getLong(2));
        flipper.setDateMaj(c.getString(4));
        //flipper.getDateMaj();
        flipper.setActif(c.getLong(5));
        return flipper;
    }

}
