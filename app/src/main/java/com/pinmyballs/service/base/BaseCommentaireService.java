package com.pinmyballs.service.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.pinmyballs.database.dao.CommentaireDAO;
import com.pinmyballs.metier.Commentaire;

import java.util.ArrayList;
import java.util.List;

public class BaseCommentaireService {

	public ArrayList<Commentaire> getCommentaireByFlipperId(Context pContext, long idFlipper){
		ArrayList<Commentaire> listeRetour;
		CommentaireDAO commentaireDao = new CommentaireDAO(pContext);
		commentaireDao.open();
		listeRetour = commentaireDao.getCommentairePourFlipperId(idFlipper);
		commentaireDao.close();
		return listeRetour;
	}

	public ArrayList<Commentaire> getLastCommentaire(Context pContext, int nbMaxCommentaire){
		ArrayList<Commentaire> listeRetour;
		CommentaireDAO commentaireDao = new CommentaireDAO(pContext);
		commentaireDao.open();
		listeRetour = commentaireDao.getLastCommentaire(nbMaxCommentaire);
		commentaireDao.close();
		return listeRetour;
	}

    public ArrayList<Commentaire> getLastCommentaireType(Context pContext, int nbMaxCommentaire, String type, boolean includeNull) {
        ArrayList<Commentaire> listeRetour;
        CommentaireDAO commentaireDao = new CommentaireDAO(pContext);
        commentaireDao.open();
        listeRetour = commentaireDao.getLastCommentaireType(nbMaxCommentaire, type, includeNull);
        commentaireDao.close();
        return listeRetour;
    }

    //TODO lastCOMMENTAIRE AROUND
	/*public ArrayList<Commentaire> getLastCommentaireAround(Context pContext, int nbMaxCommentaire, LatLng latLng){
		ArrayList<Commentaire> listeRetour = new ArrayList<Commentaire>();
		CommentaireDAO commentaireDao = new CommentaireDAO(pContext);
		commentaireDao.open();
		listeRetour = commentaireDao.getLastCommentaireAround(nbMaxCommentaire);
		commentaireDao.close();
		return listeRetour;
	}*/

	public void addCommentaire(Commentaire commentaire, Context pContext){
		CommentaireDAO commentaireDao = new CommentaireDAO(pContext);
		commentaireDao.open();
		commentaireDao.save(commentaire);
		commentaireDao.close();
	}

	public void majListeCommentaire(List<Commentaire> listeCommentaire, Context pContext){
		majListeCommentaire(listeCommentaire, pContext, false);
	}

	public void initListeCommentaire(List<Commentaire> listeCommentaire, SQLiteDatabase db){
		CommentaireDAO commentaireDao = new CommentaireDAO(db);
		for (Commentaire commentaire: listeCommentaire){
			commentaireDao.save(commentaire);
		}
	}

	private void majListeCommentaire(List<Commentaire> listeCommentaire, Context pContext, boolean truncate){
		CommentaireDAO commentaireDao = new CommentaireDAO(pContext);
		SQLiteDatabase db = commentaireDao.open();
		db.beginTransaction();
		if (truncate){
			commentaireDao.truncate();
		}
		for (Commentaire commentaire: listeCommentaire){
			commentaireDao.save(commentaire);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		commentaireDao.close();
	}

}
