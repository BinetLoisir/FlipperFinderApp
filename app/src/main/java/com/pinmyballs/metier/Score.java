package com.pinmyballs.metier;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Score  implements Serializable{

    private static final long serialVersionUID = 4904936104977593366L;


    @SerializedName("SCORE_ID")
    private long id;

    @SerializedName("SCORE_OBJECTID")
    private String objectId;

    @SerializedName("SCORE_SCORE")
    private long score;

    @SerializedName("SCORE_DATE")
    private String date;

    @SerializedName("SCORE_PSEUDO")
    private String pseudo;

    @SerializedName("SCORE_FLIPPER_ID")
    private long flipperId;

    private Flipper flipper;


    public Score(){
    }

    /**
     *
     * @param id score id
     * @param objectId score objectId
     * @param score score
     * @param date date
     * @param pseudo pseudo
     * @param flipperId flipID
     * @param flipper flipper
     */
    public Score(long id, String objectId, long score, String date, String pseudo, long flipperId, Flipper flipper) {
        this.id = id;
        this.objectId = objectId;
        this.score = score;
        this.date = date;
        this.pseudo = pseudo;
        this.flipperId = flipperId;
        this.flipper = flipper;
    }

    /**
     *
     * @param id score id
     * @param objectId score objectId
     * @param score score
     * @param date date
     * @param pseudo pseudo
     * @param flipperId flipID
     */
    public Score(long id, String objectId, long score, String date, String pseudo, long flipperId) {
        this.id = id;
        this.objectId = objectId;
        this.score = score;
        this.date = date;
        this.pseudo = pseudo;
        this.flipperId = flipperId;
    }




    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
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

    public long getFlipperId() {
        return flipperId;
    }

    public void setFlipperId(long flipperId) {
        this.flipperId = flipperId;
    }

    public Flipper getFlipper() {
        return flipper;
    }

    public void setFlipper(Flipper flipper) {
        this.flipper = flipper;
    }
}
