package com.pinmyballs.service;

import android.content.Context;

import com.pinmyballs.fragment.FragmentHiScoreFlipper;
import com.pinmyballs.fragment.FragmentScoreFlipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.service.base.BaseScoreService;
import com.pinmyballs.service.parse.ParseScoreService;

import java.util.ArrayList;

public class ScoreService {
    private FragmentHiScoreFlipper.FragmentCallback mFragmentCallback;

    public ScoreService(FragmentHiScoreFlipper.FragmentCallback fragmentCallback) {
        mFragmentCallback = fragmentCallback;
    }

    public boolean ajouteScore(Context pContext, Score score){
        ParseScoreService parseScoreService = new ParseScoreService(mFragmentCallback);
        parseScoreService.ajouteScore(pContext, score);

        BaseScoreService baseScoreService = new BaseScoreService();
        baseScoreService.addScore(score,pContext);

        if (mFragmentCallback != null){
            mFragmentCallback.onTaskDone();

        }

        return true;
    }
    public ArrayList<Score> getScoresByFlipperId(Context pContext, long idFlipper){
        BaseScoreService baseScoreService = new BaseScoreService();

        if (mFragmentCallback != null){
            mFragmentCallback.onTaskDone();
        }
        return baseScoreService.getScoresByFlipperId(pContext, idFlipper);
    }
}