package com.pinmyballs.service.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.pinmyballs.database.dao.ScoreDAO;
import com.pinmyballs.metier.Score;

import java.util.ArrayList;
import java.util.List;

public class BaseScoreService {

    public ArrayList<Score> getScoresByFlipperId(Context pContext, long idFlipper){
        ArrayList<Score> listeRetour = new ArrayList<Score>();
        ScoreDAO scoreDAO = new ScoreDAO(pContext);
        scoreDAO.open();
        listeRetour = scoreDAO.getScorePourFlipperId(idFlipper);
        scoreDAO.close();
        return listeRetour;
    }

    public ArrayList<Score> getLastScore(Context pContext, int nbMaxScore){
        ArrayList<Score> listeRetour = new ArrayList<Score>();
        ScoreDAO scoreDAO = new ScoreDAO(pContext);
        scoreDAO.open();
        listeRetour = scoreDAO.getLastScore(nbMaxScore);
        scoreDAO.close();
        return listeRetour;
    }

    public boolean addScore(Score score, Context pContext){
        ScoreDAO scoreDAO = new ScoreDAO(pContext);
        scoreDAO.open();
        scoreDAO.save(score);
        scoreDAO.close();
        return true;
    }

    public boolean majListeScore(List<Score> listScore, Context pContext){
        return majListeScore(listScore, pContext,false);
    }

    public boolean initListeScore(List<Score> listeScore, SQLiteDatabase db){
        ScoreDAO scoreDAO = new ScoreDAO(db);
        for (Score score: listeScore){
            scoreDAO.save(score);
        }
        return true;
    }

    public boolean majListeScore(List<Score> listScore, Context pContext, boolean truncate){
        ScoreDAO scoreDAO = new ScoreDAO(pContext);
        SQLiteDatabase db = scoreDAO.open();
        db.beginTransaction();
        if (truncate){
            scoreDAO.truncate();
        }
        for (Score score: listScore){
            scoreDAO.save(score);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        scoreDAO.close();
        return true;
    }

}
