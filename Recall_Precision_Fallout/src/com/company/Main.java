package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    ArrayList<String> allpcid=new ArrayList<>();
    static int TotalNumberOfDocsInDataset=14200260;

    public static void main(String[] args) throws IOException {
	// write your code here

        BufferedWriter bw=new BufferedWriter(new FileWriter("C:\\Users\\artemis\\Recall_Precision_Fallout\\final_Measures.txt"));
        for(int i=1;i<11;i++){
            System.out.println(i);
            HashMap<String,Integer> myanswers=ReadMyAnswers(i);
            ArrayList<String> Gold_Answers=ReadGoldenAnswers(i);
            double f = fallout(myanswers,Gold_Answers);
            double p= precision(myanswers,Gold_Answers);
            double r= recall(myanswers,Gold_Answers);
            bw.write((i-1)+"\n\tPrecision= "+p+"\n\tRecall= "+r+"\n\tFallout= "+f+"\n");
        }
        bw.close();

    }



    private static double recall( HashMap<String,Integer> myanswers,ArrayList<String> Gold_Answers) {
        int number_of_relevant_and_retrieved=0;
        int number_of_relevant= Gold_Answers.size();
        for(String str:Gold_Answers){
            if(myanswers.containsKey(str))
                number_of_relevant_and_retrieved++;
        }
        if(number_of_relevant_and_retrieved!=0){
            return (double)number_of_relevant_and_retrieved/(double)number_of_relevant;
        }
        return 0.0;
    }

    private static double precision( HashMap<String,Integer> myanswers,ArrayList<String> Gold_Answers) {
        int number_of_relevant_and_retrieved=0;
        int number_of_retrieved= myanswers.size();
        for(String str:Gold_Answers){
            if(myanswers.containsKey(str))
                number_of_relevant_and_retrieved++;
        }
        if(number_of_relevant_and_retrieved!=0){
            return (double)number_of_relevant_and_retrieved/(double)number_of_retrieved;
        }
        return 0.0;
    }

    private static double fallout( HashMap<String,Integer> myanswers,ArrayList<String> Gold_Answers) {
        int counter_of_RetrievedRelevant=0;
        int non_relevant_retrieved_docs;
        for(String str:Gold_Answers){
            if(myanswers.containsKey(str))
                counter_of_RetrievedRelevant++;
        }
        non_relevant_retrieved_docs=myanswers.size()-counter_of_RetrievedRelevant;

        return (double)non_relevant_retrieved_docs/(double)TotalNumberOfDocsInDataset;
    }

    private static ArrayList<String> ReadGoldenAnswers(int num) throws FileNotFoundException {
        System.out.println("gold answers");
        Scanner sc = new Scanner(new File("C:\\Users\\artemis\\Recall_Precision_Fallout\\files\\goldAnswers"+num+".txt"));
        ArrayList<String> answers=new ArrayList<>();
        while(sc.hasNextLine()){
            String line=(sc.nextLine()).replace("\"","");
            String[] terms=line.split("/pubmed/");
            answers.add(terms[1]);
        }
        return answers;
    }

    private static HashMap<String,Integer> ReadMyAnswers(int num) throws FileNotFoundException {
        System.out.println("Read my answers");
        int counter=1;
        Scanner sc = new Scanner(new File("C:\\Users\\artemis\\Recall_Precision_Fallout\\files\\output"+num+".txt"));
        HashMap<String,Integer> asnwers=new HashMap<>();
        while(sc.hasNextLine()){
            String line=sc.nextLine();
            String[] terms=line.split(" ");
            asnwers.put(terms[1],counter++);

        }
        return asnwers;
    }
}
