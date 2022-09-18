package com.company;

import java.util.ArrayList;

public class WordTfDf_norma {
    ArrayList<Word_TF_DF> list=new ArrayList<>();
    double norma;

    public WordTfDf_norma(ArrayList<Word_TF_DF> list, double norma) {
        this.list = list;
        this.norma = norma;
    }

    public ArrayList<Word_TF_DF> getList() {
        return list;
    }

    public void setList(ArrayList<Word_TF_DF> list) {
        this.list = list;
    }

    public double getNorma() {
        return norma;
    }

    public void setNorma(double norma) {
        this.norma = norma;
    }

    public WordTfDf_norma() {
    }
}
