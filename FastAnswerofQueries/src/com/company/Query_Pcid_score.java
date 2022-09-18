package com.company;

import java.util.ArrayList;

public class Query_Pcid_score {
    String query;
    ArrayList<PCID_score> list=new ArrayList<>();

    public Query_Pcid_score() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ArrayList<PCID_score> getList() {
        return list;
    }

    public void setList(ArrayList<PCID_score> list) {
        this.list = list;
    }

    public Query_Pcid_score(String query, ArrayList<PCID_score> list) {
        this.query = query;
        this.list = list;
    }
}
