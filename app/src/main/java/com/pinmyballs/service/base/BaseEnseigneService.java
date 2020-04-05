package com.pinmyballs.service.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.pinmyballs.database.dao.EnseigneDAO;
import com.pinmyballs.metier.Enseigne;

import java.util.List;

public class BaseEnseigneService {

	public boolean majListeEnseigne(List<Enseigne> listeEnseignes, Context pContext){
		return majListeEnseigne(listeEnseignes, pContext, false);
	}

	public void initListeEnseigne(List<Enseigne> listeObjets, SQLiteDatabase db){
		EnseigneDAO enseigneDao = new EnseigneDAO(db);
		for (Enseigne enseigne: listeObjets){
			enseigneDao.save(enseigne);
		}
    }

	private boolean majListeEnseigne(List<Enseigne> listeEnseignes, Context pContext, boolean truncate){
		EnseigneDAO enseigneDao = new EnseigneDAO(pContext);
		SQLiteDatabase db = enseigneDao.open();
		db.beginTransaction();
		if (truncate){
			enseigneDao.truncate();
		}
		for (Enseigne enseigne : listeEnseignes){
			enseigneDao.save(enseigne);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		enseigneDao.close();
		return true;
	}
}
