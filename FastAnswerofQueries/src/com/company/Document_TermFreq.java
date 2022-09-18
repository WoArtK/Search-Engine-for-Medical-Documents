package com.company;

public class Document_TermFreq {

    int tf;
    long position;

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }



    public Document_TermFreq() {
    }

    public Document_TermFreq(int tf,long position) {
        this.tf = tf;
        this.position=position;
    }


    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }
}

