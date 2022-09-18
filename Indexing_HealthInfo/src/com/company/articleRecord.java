package com.company;

import java.util.ArrayList;

public class articleRecord {
    String journal;
    ArrayList<String> mashmajor=new ArrayList<>();
    String year;
    ArrayList<String> abstractText=new ArrayList<>();
    String title;
    int pmid;

    public long getPointer() {
        return pointer;
    }

    public void setPointer(long pointer) {
        this.pointer = pointer;
    }

    long pointer;

    public int getPmid() {
        return pmid;
    }

    public void setPmid(int pmid) {
        this.pmid = pmid;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public ArrayList<String> getMashmajor() {
        return mashmajor;
    }

    public void setMashmajor(ArrayList<String> mashmajor) {
        this.mashmajor = mashmajor;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public ArrayList<String> getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(ArrayList<String> abstractText) {
        this.abstractText = abstractText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public articleRecord(int pmid,String journal, ArrayList<String> mashmajor, String year, ArrayList<String> abstractText, String title) {
        this.journal = journal;
        this.mashmajor = mashmajor;
        this.year = year;
        this.abstractText = abstractText;
        this.title = title;
        this.pmid=pmid;
    }



    public articleRecord(){

    }

    @Override
    public String toString() {
        return "articleRecord{" +
                "journal='" + journal + '\'' +
                ", mashmajor=" + mashmajor +
                ", year='" + year + '\'' +
                ", abstractText=" + abstractText +
                ", title='" + title + '\'' +
                ", pmid=" + pmid +
                ", pointer=" + pointer +
                '}';
    }
}
