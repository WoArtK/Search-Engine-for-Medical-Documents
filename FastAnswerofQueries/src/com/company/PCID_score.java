package com.company;

public class PCID_score {
    String Pcid;
    double score;

    public PCID_score(String pcid, double score) {
        Pcid = pcid;
        this.score = score;
    }

    public PCID_score() {
    }

    public String getPcid() {
        return Pcid;
    }

    public void setPcid(String pcid) {
        Pcid = pcid;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
