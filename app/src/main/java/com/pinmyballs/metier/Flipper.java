package com.pinmyballs.metier;


import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.io.Serializable;

@Parcel
public class Flipper implements Serializable {

    private static final long serialVersionUID = 2088862582774334050L;

    @SerializedName("FLIP_ID")
    long id;

    @SerializedName("FLIP_MODELE")
    long idModele;

    @SerializedName("FLIP_NB_CREDITS_2E")
    String NbCreditsDeuxEuros;

    @SerializedName("FLIP_ENSEIGNE")
    long idEnseigne;

    @SerializedName("FLIP_ACTIF")
    boolean actif;

    @SerializedName("FLIP_DATMAJ")
    String dateMaj;

    @SerializedName("FLIP_NOTE")
    int note;

    @SerializedName("FLIP_EXPL")
    String exploitant;

    @SerializedName("FLIP_PHOTO")
    String photo;

    ModeleFlipper modele;
    Enseigne enseigne;

    public Flipper() {
    }

    /**
     * @param id                 flipid
     * @param idModele           flipmodele id
     * @param NbCreditsDeuxEuros nbCredit2euros
     * @param idEnseigne         enseigneid
     * @param actif              actif
     * @param dateMaj            datedederniereMaj
     */
    public Flipper(long id, long idModele, String NbCreditsDeuxEuros, long idEnseigne, boolean actif, String dateMaj) {
        this.id = id;
        this.idModele = idModele;
        this.NbCreditsDeuxEuros = NbCreditsDeuxEuros;
        this.idEnseigne = idEnseigne;
        this.actif = actif;
        this.dateMaj = dateMaj;
    }

    public Flipper(long id, long idModele, String NbCreditsDeuxEuros, long idEnseigne, boolean actif, String dateMaj, String exploitant, int note) {
        this.id = id;
        this.idModele = idModele;
        this.NbCreditsDeuxEuros = NbCreditsDeuxEuros;
        this.idEnseigne = idEnseigne;
        this.actif = actif;
        this.dateMaj = dateMaj;
        this.exploitant=exploitant;
        this.note=note;
    }

    public Flipper(long id, long idModele, String nbCreditsDeuxEuros, long idEnseigne, boolean actif, String dateMaj, int note, String exploitant, String photo, ModeleFlipper modele, Enseigne enseigne) {
        this.id = id;
        this.idModele = idModele;
        NbCreditsDeuxEuros = nbCreditsDeuxEuros;
        this.idEnseigne = idEnseigne;
        this.actif = actif;
        this.dateMaj = dateMaj;
        this.note = note;
        this.exploitant = exploitant;
        this.photo = photo;
        this.modele = modele;
        this.enseigne = enseigne;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ModeleFlipper getModele() {
        return modele;
    }

    public void setModele(ModeleFlipper modele) {
        this.modele = modele;
        if (modele != null) {
            this.idModele = modele.getId();
        }
    }


    public Enseigne getEnseigne() {
        return enseigne;
    }

    public void setEnseigne(Enseigne enseigne) {
        this.enseigne = enseigne;
        if (enseigne != null) {
            this.idEnseigne = enseigne.getId();
        }
    }

    public long getIdModele() {
        return idModele;
    }

    public void setIdModele(long idModele) {
        this.idModele = idModele;
    }

    public long getIdEnseigne() {
        return idEnseigne;
    }

    public void setIdEnseigne(long idEnseigne) {
        this.idEnseigne = idEnseigne;
    }

    public String getDateMaj() {
        return this.dateMaj;
    }

    public void setDateMaj(String dateMaj) {
        this.dateMaj = dateMaj;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(long actif) {
        this.actif = actif == 1;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public void setInactif(long actif) {
        this.actif = actif == 0;
    }

    public String getNbCreditsDeuxEuros() {
        return NbCreditsDeuxEuros;
    }

    public void setNbCreditsDeuxEuros(String nbCreditsDeuxEuros) {
		this.NbCreditsDeuxEuros = nbCreditsDeuxEuros;
	}

	public int getNote() {
		return note;
	}

	public void setNote(int note) {
		this.note = note;
	}

	public String getExploitant() {
		return exploitant;
	}

	public void setExploitant(String exploitant) {
		this.exploitant = exploitant;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}


