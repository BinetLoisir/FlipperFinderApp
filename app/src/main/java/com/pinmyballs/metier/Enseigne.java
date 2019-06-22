package com.pinmyballs.metier;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Enseigne  implements Serializable{

	private static final long serialVersionUID = 4803600387182980034L;

	//@SerializedName("objectId")
	//private String objectId;

	@SerializedName("ENS_ID")
	private long id;

	@SerializedName("ENS_TYPE")
	private String type;

	@SerializedName("ENS_NOM")
	private String nom;

	@SerializedName("ENS_HORAIRE")
	private String horaire;

	@SerializedName("ENS_LATITUDE")
	private String latitude;

	@SerializedName("ENS_LONGITUDE")
	private String longitude;

	@SerializedName("ENS_ADRESSE")
	private String adresse;

	@SerializedName("ENS_CODE_POSTAL")
	private String codePostal;

	@SerializedName("ENS_VILLE")
	private String ville;

	@SerializedName("ENS_PAYS")
	private String pays;

	@SerializedName("ENS_DATMAJ")
	private String dateMaj;

	public Enseigne(){
	}

	/**
	 *
	 * @param id id
	 * @param type type
	 * @param nom nom
	 * @param horaire horaire
	 * @param latitude latitude
	 * @param longitude longitude
	 * @param adresse adresse
	 * @param codePostal CP
	 * @param ville Ville
	 * @param pays Pays
	 * @param dateMaj datMaj
	 */
	public Enseigne(long id, String type, String nom, String horaire,
			String latitude, String longitude, String adresse,
			String codePostal, String ville, String pays, String dateMaj) {
		this.id = id;
		this.type = type;
		setNom(nom);
		this.horaire = horaire;
		this.latitude = latitude;
		this.longitude = longitude;
		setAdresse(adresse);
		this.codePostal = codePostal;
		setVille(ville);
		this.pays = pays;
		this.setDateMaj(dateMaj);
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		if (nom != null){
			this.nom = nom.replace("''", "'");
		}
	}
	public String getHoraire() {
		return horaire;
	}
	public void setHoraire(String horaire) {
		this.horaire = horaire;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getAdresse() {
		return adresse;
	}
	public void setAdresse(String adresse) {
		if (adresse != null){
			this.adresse = adresse.replace("''", "'");
		}
	}
	public String getCodePostal() {
		return codePostal;
	}
	public void setCodePostal(String codePostal) {
		this.codePostal = codePostal;
	}
	public String getVille() {
		return ville;
	}
	public void setVille(String ville) {
		if (ville != null){
			this.ville = ville.replace("''", "'");
		}
	}
	public String getPays() {
		return pays;
	}
	public void setPays(String pays) {
		this.pays = pays;
	}

	public String getDateMaj() {
		return dateMaj;
	}

	public void setDateMaj(String dateMaj) {
		this.dateMaj = dateMaj;
	}

	public String getAdresseCompleteSansPays(){
		return adresse + " " + codePostal + " " + ville;
	}

	public String getAdresseCompleteAvecPays(){
		return getAdresseCompleteSansPays() + " " + pays;
	}
}
