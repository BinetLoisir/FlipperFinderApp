package com.pinmyballs.service.parse;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.pinmyballs.R;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.fragment.FragmentHiScoreFlipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.service.base.BaseScoreService;
import com.pinmyballs.utils.ProgressBarHandler;

import java.util.ArrayList;
import java.util.List;

public class ParseScoreService {
    private static final String TAG = "ParseScoreService";
    private FragmentHiScoreFlipper.FragmentCallback mFragmentCallback;

    public ParseScoreService(FragmentHiScoreFlipper.FragmentCallback fragmentCallback) {
        mFragmentCallback = fragmentCallback;
    }
    /**
     * Retourne la liste des scores à mettre à jour à partir d'une date donnée.
     * @param dateDerniereMaj
     * @return
     */
    public List<Score> getMajScoreByDate(String dateDerniereMaj){
        List<Score> listScore = new ArrayList<>();

        List<ParseObject> listePo;
        ParseQuery<ParseObject> query = new ParseQuery<>(FlipperDatabaseHandler.SCORE_TABLE_NAME);
        try {
            query.setLimit(2000);
            query.whereGreaterThanOrEqualTo(FlipperDatabaseHandler.SCORE_DATE, dateDerniereMaj);
            listePo = query.find();
        } catch (ParseException e1) {
            e1.printStackTrace();
            return null;
        }

        for (ParseObject po : listePo){
            Score score = new Score(po.getLong(FlipperDatabaseHandler.SCORE_ID),
                    po.getObjectId(),
                    po.getLong(FlipperDatabaseHandler.SCORE_SCORE),
                    po.getString(FlipperDatabaseHandler.SCORE_DATE),
                    po.getString(FlipperDatabaseHandler.SCORE_PSEUDO),
                    po.getLong(FlipperDatabaseHandler.SCORE_FLIPPER_ID));
            listScore.add(score);
        }
        return listScore;
    }

    public List<Score> getAllScores(){
        List<Score> listScore = new ArrayList<>();

        List<ParseObject> listePo;
        ParseQuery<ParseObject> query = new ParseQuery<>(FlipperDatabaseHandler.SCORE_TABLE_NAME);
        try {
            query.setLimit(2000);
            listePo = query.find();
        } catch (ParseException e1) {
            e1.printStackTrace();
            return null;
        }

        for (ParseObject po : listePo){
            Score score = new Score(po.getLong(FlipperDatabaseHandler.SCORE_ID),
                    po.getObjectId(),
                    po.getLong(FlipperDatabaseHandler.SCORE_SCORE),
                    po.getString(FlipperDatabaseHandler.SCORE_DATE),
                    po.getString(FlipperDatabaseHandler.SCORE_PSEUDO),
                    po.getLong(FlipperDatabaseHandler.SCORE_FLIPPER_ID));
            listScore.add(score);
        }
        return listScore;
    }

    public void ajouteScore(final Context pContext, final Score score){
        final ProgressBarHandler mProgressBarHandler = new ProgressBarHandler(pContext);
        mProgressBarHandler.show();
        final ParseObject parseScore = new ParseObject(FlipperDatabaseHandler.SCORE_TABLE_NAME);
        parseScore.put(FlipperDatabaseHandler.SCORE_ID, score.getId());
        parseScore.put(FlipperDatabaseHandler.SCORE_FLIPPER_ID, score.getFlipperId());
        parseScore.put(FlipperDatabaseHandler.SCORE_DATE, score.getDate());
        parseScore.put(FlipperDatabaseHandler.SCORE_PSEUDO, score.getPseudo());
        parseScore.put(FlipperDatabaseHandler.SCORE_SCORE, score.getScore());
        parseScore.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mProgressBarHandler.hide();
                if (e == null){
                    //TODO A checker
                    score.setObjectId(parseScore.getObjectId());
                    BaseScoreService baseScoreService = new BaseScoreService();
                    baseScoreService.addScore(score, pContext);
                    Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastAjouteScoreCloudOK), Toast.LENGTH_LONG);
                    toast.show();
                    Log.d(TAG, "Parse save in background done: ");
                    if (mFragmentCallback != null){
                        mFragmentCallback.onTaskDone();
                    }
                }else{
                    Toast toast = Toast.makeText(pContext, pContext.getResources().getString(R.string.toastAjouteScoreCloudKO), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

}

