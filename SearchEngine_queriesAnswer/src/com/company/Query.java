package com.company;

public class Query {
    String Query;
    Profile profile;

    public String getQuery() {
        return Query;
    }

    public void setQuery(String query) {
        Query = query;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Query(String query, Profile profile) {
        Query = query;
        this.profile = profile;
    }

    public Query() {
    }

    @Override
    public String toString() {
        return "Query{" +
                "Query='" + Query + '\'' +
                ", \nprofile=" + profile.toString() +
                '}';
    }
}
