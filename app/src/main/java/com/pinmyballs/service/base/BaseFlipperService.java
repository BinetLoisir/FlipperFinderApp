package com.pinmyballs.service.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.pinmyballs.database.dao.FlipperDAO;
import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.metier.Flipper;

public class BaseFlipperService {


	public ArrayList<Flipper> rechercheFlipper(Context pContext, double latitude, double longitude, int rayon, int maxListeSize, String modele){
		ArrayList<Flipper> listeRetour = new ArrayList<>();

		FlipperDAO flipperDao = new FlipperDAO(pContext);

		flipperDao.open();
		PointF center = new PointF((float)latitude, (float)longitude);

		ArrayList<Flipper> listeFlipper = flipperDao.getFlipperByDistance(center, rayon, modele);
		flipperDao.close();

		for (int i = 0 ; i < listeFlipper.size() && i < maxListeSize ;i++){
			listeRetour.add(listeFlipper.get(i));
		}

		return listeRetour;
	}

	public ArrayList<Flipper> rechercheFlipper(Context pContext, LatLng latLng, int rayon, int maxListeSize, String modele){
		ArrayList<Flipper> listeRetour = new ArrayList<>();

		FlipperDAO flipperDao = new FlipperDAO(pContext);

		flipperDao.open();
		PointF center = new PointF((float)latLng.latitude, (float)latLng.longitude);

		ArrayList<Flipper> listeFlipper = flipperDao.getFlipperByDistance(center, rayon, modele);
		flipperDao.close();

		for (int i = 0 ; i < listeFlipper.size() && i < maxListeSize ;i++){
			listeRetour.add(listeFlipper.get(i));
		}

		return listeRetour;
	}

	public ArrayList<Flipper> rechercheFlipper(Context pContext, Enseigne enseigne){
		ArrayList<Flipper> listeRetour = new ArrayList<>();

		FlipperDAO flipperDao = new FlipperDAO(pContext);
		flipperDao.open();
		ArrayList<Flipper> listeFlipper = flipperDao.getFlipperByEnseigne(enseigne);
		flipperDao.close();

		/*for (int i = 0 ; i < listeFlipper.size() ;i++){
			listeRetour.add(listeFlipper.get(i));
		}*/

		return new ArrayList<>(listeFlipper);

	}

	public ArrayList<Flipper> rechercheOtherFlipper(Context pContext, Flipper flipper){
		ArrayList<Flipper> listeRetour = new ArrayList<>();

		FlipperDAO flipperDao = new FlipperDAO(pContext);
		flipperDao.open();
		ArrayList<Flipper> listeFlipper = flipperDao.getOtherFlippers(flipper);
		flipperDao.close();

		/*for (int i = 0 ; i < listeFlipper.size() ;i++){
			listeRetour.add(listeFlipper.get(i));
		}*/

		return new ArrayList<>(listeFlipper);

	}

	public ArrayList<Flipper> getAllActiveFlippers(Context pContext){
		ArrayList<Flipper> listeRetour = new ArrayList<>();

		FlipperDAO flipperDao = new FlipperDAO(pContext);
		flipperDao.open();
		ArrayList<Flipper> listeFlipper = flipperDao.getAllFlipperActif();
		flipperDao.close();

		return new ArrayList<>(listeFlipper);

	}


	public String NombreFlipperActifs(Context pContext){
		String nb;

		FlipperDAO flipperDao = new FlipperDAO(pContext);
			flipperDao.open();
			nb = flipperDao.getNbFlipperActif();
			flipperDao.close();

		return nb;
	}

    public String NombreFlipperActifs(Context pContext, Enseigne enseigne){
        String nb;

        FlipperDAO flipperDao = new FlipperDAO(pContext);
        flipperDao.open();
        nb = flipperDao.getNbFlipperActif(enseigne);
        flipperDao.close();

        return nb;
    }


	public Flipper getFlipperById(Context pContext, long idFlipper){
		Flipper flipperRetour;

		FlipperDAO flipperDao = new FlipperDAO(pContext);

		flipperDao.open();
		flipperRetour = flipperDao.getFlipperById(idFlipper);
		flipperDao.close();

		return flipperRetour;
	}

	public ArrayList<Flipper> getFlipperByModel(Context pContext, long idModel){
		ArrayList<Flipper> flipperRetour;

		FlipperDAO flipperDao = new FlipperDAO(pContext);

		flipperDao.open();
		flipperRetour = flipperDao.getFlipperByModel(idModel);
		flipperDao.close();

		return flipperRetour;
	}



	public void initListeFlipper(List<Flipper> listeObjets, SQLiteDatabase db){
		FlipperDAO flipperDao = new FlipperDAO(db);
		for (Flipper flipper: listeObjets){
			flipperDao.save(flipper);
		}
	}

	public boolean majListeFlipper(List<Flipper> listeFlipper, Context pContext){
		return majListeFlipper(listeFlipper, pContext, false);
	}

	/**
	 *
	 * @param listeFlipper
	 * @param pContext
	 * @param truncate If true, the table is deleted
	 * @return
	 */
	public boolean majListeFlipper(List<Flipper> listeFlipper, Context pContext, boolean truncate){
		FlipperDAO flipperDao = new FlipperDAO(pContext);
		SQLiteDatabase db = flipperDao.open();
		db.beginTransaction();
		if (truncate){
			flipperDao.truncate();
		}
		for (Flipper flipper : listeFlipper){
			flipperDao.save(flipper);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		flipperDao.close();
		return true;
	}

	public void majFlipper(Flipper flipper, Context pContext){
		FlipperDAO flipperDao = new FlipperDAO(pContext);
		flipperDao.open();
		flipperDao.save(flipper);
		flipperDao.close();
	}

}
