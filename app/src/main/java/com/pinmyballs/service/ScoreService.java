package com.pinmyballs.service;

import android.content.Context;

import com.pinmyballs.fragment.FragmentScoreFlipper;
import com.pinmyballs.metier.Score;
import com.pinmyballs.service.base.BaseScoreService;
import com.pinmyballs.service.parse.ParseScoreService;

import java.util.ArrayList;

public class ScoreService {
    private FragmentScoreFlipper.FragmentCallback mFragmentCallback;

    public ScoreService(FragmentScoreFlipper.FragmentCallback fragmentCallback) {
        mFragmentCallback = fragmentCallback;
    }

    public boolean ajouteScore(Context pContext, Score score){
        ParseScoreService parseScoreService = new ParseScoreService(mFragmentCallback);
        parseScoreService.ajouteScore(pContext, score);

        return true;
    }
    public ArrayList<Score> getScoresByFlipperId(Context pContext, long idFlipper){
        BaseScoreService baseScoreService = new BaseScoreService();
        return baseScoreService.getScoresByFlipperId(pContext, idFlipper);
    }
}