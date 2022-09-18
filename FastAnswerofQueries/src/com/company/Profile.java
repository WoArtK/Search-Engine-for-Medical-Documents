package com.company;

import java.util.ArrayList;

public class Profile {
    String age;
    ArrayList<String> medical_exams=new ArrayList<>();
    ArrayList<String> drugs=new ArrayList<>();
    ArrayList<String> diseases=new ArrayList<>();
    ArrayList<String> treatments=new ArrayList<>();
    String gender;
    String region;
    double weight;
    double height;


    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public ArrayList<String> getMedical_exams() {
        return medical_exams;
    }

    public void setMedical_exams(ArrayList<String> medical_exams) {
        this.medical_exams = medical_exams;
    }

    public ArrayList<String> getDrugs() {
        return drugs;
    }

    public void setDrugs(ArrayList<String> drugs) {
        this.drugs = drugs;
    }

    public ArrayList<String> getDiseases() {
        return diseases;
    }

    public void setDiseases(ArrayList<String> diseases) {
        this.diseases = diseases;
    }

    public ArrayList<String> getTreatments() {
        return treatments;
    }

    public void setTreatments(ArrayList<String> treatments) {
        this.treatments = treatments;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Profile(String age, ArrayList<String> medical_exams, ArrayList<String> drugs, ArrayList<String> diseases, ArrayList<String> treatments, String gender, String region, double weight, double height) {
        this.age = age;
        this.medical_exams = medical_exams;
        this.drugs = drugs;
        this.diseases = diseases;
        this.treatments = treatments;
        this.gender = gender;
        this.region = region;
        this.weight = weight;
        this.height = height;
    }

    public Profile() {
    }

    public String custom_toString(){
        String pr=age+" "+gender+" "+region;
        if(weight!=0.0){
            pr=pr+" "+weight;
        }
        if(height!=0.0){
            pr=pr+" "+height;
        }
        for(String str:medical_exams){
            pr=pr+" "+str;
        }
        for(String str:drugs){
            pr=pr+" "+str;
        }
        for(String str:diseases){
            pr=pr+" "+str;
        }
        for(String str:treatments){
            pr=pr+" "+str;
        }
        return pr;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "age='" + age + '\'' +
                ", medical_exams=" + medical_exams +
                ", drugs=" + drugs +
                ", diseases=" + diseases +
                ", treatments=" + treatments +
                ", gender='" + gender + '\'' +
                ", region='" + region +
                ", weight=" + weight +
                ", height=" + height +
                '}';
    }
}
