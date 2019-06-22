package com.pinmyballs.service.parse;


public class ParseVersionBaseService {

	/**
	 * Retourne la dernière version de la base à partir du cloud.
	 * @return int
	 */
	public int getDerniereVersionBase(){
		/*
		   ParseQuery query = new ParseQuery("VersionBase");
		   try {
		   ParseObject poVersion = (ParseObject)query.orderByDescending("VB_ID").find().get(0);
		   retour = poVersion.getInt("VB_VERSION");
		   } catch (ParseException e1) {
		   e1.printStackTrace();
		   }
		   */
		return 0;
	}
}
