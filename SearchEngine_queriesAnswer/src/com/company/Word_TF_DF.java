package com.company;

public class Word_TF_DF {
    String word;
    double tf;
    int df;

    public Word_TF_DF() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public double getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public int getDf() {
        return df;
    }

    public void setDf(int df) {
        this.df = df;
    }

    public Word_TF_DF(String word, double tf, int df) {
        this.word = word;
        this.tf = tf;
        this.df = df;
    }
}
