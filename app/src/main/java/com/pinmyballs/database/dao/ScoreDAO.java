package com.pinmyballs.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.pinmyballs.database.DAOBase;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.metier.Score;

import java.util.ArrayList;

public class ScoreDAO extends DAOBase{

    public ScoreDAO(Context pContext) {
        super(pContext);
    }

    public ScoreDAO(SQLiteDatabase pDb) {
        super(pDb);
    }

    public ArrayList<Score> getLastScore(int nbMaxScore){
        ArrayList<Score> listeRetour = new ArrayList<>();
        String strOrder =  " ORDER BY " + FlipperDatabaseHandler.SCORE_DATE + " DESC, " + FlipperDatabaseHandler.SCORE_ID+ " DESC ";

        Cursor cursor = mDb.rawQuery("select " + FlipperDatabaseHandler.SCORE_ID +
                " , " +  FlipperDatabaseHandler.SCORE_FLIPPER_ID +
                " , " +  FlipperDatabaseHandler.SCORE_OBJECTID +
                " , " +  FlipperDatabaseHandler.SCORE_DATE +
                " , " +  FlipperDatabaseHandler.SCORE_SCORE +
                " , " +  FlipperDatabaseHandler.SCORE_PSEUDO +
                " from " + FlipperDatabaseHandler.SCORE_TABLE_NAME +
                strOrder, null);

        int i = 0;
        while (cursor.moveToNext() && i++ < nbMaxScore) {
            listeRetour.add(convertCursorToScore(cursor));
        }
        cursor.close();

        return listeRetour;
    }

    public ArrayList<Score> getScorePourFlipperId(long flipperId){
        ArrayList<Score> listeRetour = new ArrayList<>();
        String strWhere =  " Where "
                + FlipperDatabaseHandler.SCORE_FLIPPER_ID + " = " + flipperId;
        String strOrder =  " ORDER BY " + FlipperDatabaseHandler.SCORE_SCORE + " DESC ";

        Cursor cursor = mDb.rawQuery("select " + FlipperDatabaseHandler.SCORE_ID +
                " , " +  FlipperDatabaseHandler.SCORE_FLIPPER_ID +
                " , " +  FlipperDatabaseHandler.SCORE_OBJECTID +
                " , " +  FlipperDatabaseHandler.SCORE_DATE +
                " , " +  FlipperDatabaseHandler.SCORE_SCORE +
                " , " +  FlipperDatabaseHandler.SCORE_PSEUDO +
                " from " + FlipperDatabaseHandler.SCORE_TABLE_NAME +
                strWhere + strOrder, null);

        while (cursor.moveToNext()) {
            listeRetour.add(convertCursorToScore(cursor));
        }
        cursor.close();
        return listeRetour;
    }

    private Score convertCursorToScore(Cursor c){
        Score score = new Score();
        score.setId(c.getLong(0));
        score.setFlipperId(c.getLong(1));
        score.setObjectId(c.getString(2));
        score.setDate(c.getString(3));
        score.setScore(c.getInt(4));
        score.setPseudo(c.getString(5));
        return score;
    }

    public void save(Score score) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FlipperDatabaseHandler.SCORE_ID, score.getId());
        contentValues.put(FlipperDatabaseHandler.SCORE_FLIPPER_ID, score.getFlipperId());
        contentValues.put(FlipperDatabaseHandler.SCORE_OBJECTID, score.getObjectId());
        contentValues.put(FlipperDatabaseHandler.SCORE_DATE, score.getDate());
        contentValues.put(FlipperDatabaseHandler.SCORE_PSEUDO, score.getPseudo());
        contentValues.put(FlipperDatabaseHandler.SCORE_SCORE,score.getScore());

        mDb.delete(FlipperDatabaseHandler.SCORE_TABLE_NAME, FlipperDatabaseHandler.SCORE_ID + "=?", new String[]{String.valueOf(score.getId())});
        mDb.insert(FlipperDatabaseHandler.SCORE_TABLE_NAME, null, contentValues);
    }

    public void truncate(){
        mDb.delete(FlipperDatabaseHandler.SCORE_TABLE_NAME, null, null);
    }

}
