package com.pinmyballs.metier;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Commentaire  implements Serializable{

	private static final long serialVersionUID = 4904936104977593366L;
    public static final String TYPE_POST = "POST";
    public static final String TYPE_NEW = "NEW";
    public static final String TYPE_DELETE = "DELETED";
    public static final String TYPE_REINSTALL = "REINSTALLED";
    public static final String TYPE_REFRESH = "REFRESHED";
    public static final String TYPE_REPLACE = "REPLACED";
    public static final String TYPE_ADDITIONAL = "ADDITIONAL";

    //@SerializedName("objectId")
	//private String objectId;

	@SerializedName("COMM_ID")
	private long id;

	@SerializedName("COMM_FLIPPER_ID")
	private long flipperId;

	@SerializedName("COMM_TEXTE")
	private String texte;

    @SerializedName("COMM_TYPE")
    private String type;

	@SerializedName("COMM_DATE")
	private String date;

	@SerializedName("COMM_PSEUDO")
	private String pseudo;

	@SerializedName("COMM_ACTIF")
	private boolean actif;

	private Flipper flipper;

	public Commentaire(){
	}

	/**
	 *
	 * @param id
	 * @param flipperId
	 * @param texte
     * @param type
     * @param date
	 * @param pseudo
	 * @param actif
	 */
    public Commentaire(long id, long flipperId, String texte, String type, String date,
                       String pseudo, boolean actif) {
		this.setId(id);
		this.setFlipperId(flipperId);
		this.setTexte(texte);
        this.setType(type);
		this.setDate(date);
		this.setPseudo(pseudo);
        this.setActif(actif);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFlipperId() {
		return flipperId;
	}

	public void setFlipperId(long flipperId) {
		this.flipperId = flipperId;
	}

	public String getTexte() {
		return texte;
	}

	public void setTexte(String texte) {
		this.texte = texte;
	}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setPseudo(String pseudo) {
		this.pseudo = pseudo;
	}

	public boolean getActif() {
		return actif;
	}

	public void setActif(boolean actif) {
		this.actif = actif;
	}

	public void setActif(long actif) {
		this.actif = actif != 0;
	}

	public Flipper getFlipper() {
		return flipper;
	}

	public void setFlipper(Flipper flipper) {
		this.flipper = flipper;
	}

}
