package com.pinmyballs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrashItem{
    Long flipId;
    String pseudo;
    Date date;
    boolean processed;

    public Long getFlipId() {
        return flipId;
    }

    public String getFlipIdAsString() {
        return Long.toString(flipId);
    }

    public void setFlipId(Long flipId) {
        this.flipId = flipId;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public Date getDate() {
        return date;
    }

    public String getDateFormatted()  {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.FRANCE).format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public  TrashItem(){};

    public TrashItem(Long flipId, String pseudo, Date date, boolean processed) {
        this.flipId = flipId;
        this.pseudo = pseudo;
        this.date = date;
        this.processed = processed;
    }
}
