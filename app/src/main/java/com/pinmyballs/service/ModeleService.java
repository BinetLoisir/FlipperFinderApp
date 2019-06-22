package com.pinmyballs.service;

import android.content.Context;

import com.pinmyballs.metier.ModeleFlipper;
import com.pinmyballs.service.base.BaseModeleService;
import com.pinmyballs.service.parse.ParseModeleService;

import java.util.List;

public class ModeleService {

	public boolean remplaceToutModele(Context pContext){
		boolean retour = true;
		BaseModeleService baseModeleService = new BaseModeleService();
		ParseModeleService parseModeleService = new ParseModeleService();
		List<ModeleFlipper> nvlleListe = parseModeleService.getAllModeleFlipper();
		retour = baseModeleService.majListeModele(nvlleListe, pContext);
		return retour;
	}

    public boolean ajouteModele(Context pContext, ModeleFlipper modeleFlipper) {
        boolean retour = true;
        ParseModeleService parseModeleService = new ParseModeleService();
        parseModeleService.ajouterModele(pContext, modeleFlipper);
        return retour;
    }

    public long getMaxIdModeleFlipper(Context pContext) {
        BaseModeleService baseModeleService = new BaseModeleService();
        return baseModeleService.getMaxIdModeleFlipper(pContext);
    }


}
