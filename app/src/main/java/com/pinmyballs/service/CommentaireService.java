package com.pinmyballs.service;

import android.content.Context;

import com.pinmyballs.fragment.FragmentCommentaireFlipper.FragmentCallback;
import com.pinmyballs.metier.Commentaire;
import com.pinmyballs.service.base.BaseCommentaireService;
import com.pinmyballs.service.parse.ParseCommentaireService;

import java.util.ArrayList;

public class CommentaireService {
	private FragmentCallback mFragmentCallback;

	public CommentaireService(FragmentCallback fragmentCallback) {
		mFragmentCallback = fragmentCallback;
	}

	public boolean ajouteCommentaire(Context pContext, Commentaire commentaire){
		ParseCommentaireService parseCommentaireService = new ParseCommentaireService(mFragmentCallback);
		parseCommentaireService.ajouteCommentaire(pContext, commentaire);

		return true;
	}
	public ArrayList<Commentaire> getCommentaireByFlipperId(Context pContext, long idFlipper){
		BaseCommentaireService baseCommentaireService = new BaseCommentaireService();
		return baseCommentaireService.getCommentaireByFlipperId(pContext, idFlipper);
	}

    public boolean updateCommentaire(Context pContext, Commentaire oldCommentaire, Commentaire newCommentaire) {
        ParseCommentaireService parseCommentaireService = new ParseCommentaireService(mFragmentCallback);
        return parseCommentaireService.updateCommentaire(pContext, oldCommentaire, newCommentaire);
    }
}
