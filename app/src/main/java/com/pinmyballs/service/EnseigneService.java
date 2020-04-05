package com.pinmyballs.service;

import android.content.Context;

import com.pinmyballs.metier.Enseigne;
import com.pinmyballs.service.base.BaseEnseigneService;
import com.pinmyballs.service.parse.ParseEnseigneService;

import java.util.List;

public class EnseigneService {

	public boolean remplaceToutEnseigne(Context pContext){
		boolean retour;
		BaseEnseigneService baseEnseigneService = new BaseEnseigneService();
		ParseEnseigneService parseEnseigneService = new ParseEnseigneService();
		List<Enseigne> nvlleListe = parseEnseigneService.getAllEnseigne();
		retour = baseEnseigneService.majListeEnseigne(nvlleListe, pContext);
		return retour;
	}

}
