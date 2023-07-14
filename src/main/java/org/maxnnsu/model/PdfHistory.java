package org.maxnnsu.model;

import lombok.Data;

import java.util.Date;

@Data
public class PdfHistory {
    int hash;
    Date date;

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PdfHistory(int hash, Date date) {
        this.hash = hash;
        this.date = date;
    }
}
