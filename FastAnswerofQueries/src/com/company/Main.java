package com.company;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.tartarus.snowball.ext.PorterStemmer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;


public class Main {

    static String  current_path;
    static HashMap<String,Long> Pointer2DocumentFile=new HashMap<>();
    static HashMap<String,DocFreq_Position> VocabularyFileHashMap=new HashMap<>(); // <word,<df,position>>
    static HashMap<String,Double> NormasHashMap=new HashMap<>();
    static HashMap<String,HashMap<String,Document_TermFreq>> PostingFileHashMap= new HashMap<>(); // <word,<pmid,(tf,position)>>
    static ArrayList<Query> ListofQueriesandProfiles= new ArrayList<>();
    static HashMap<String,SimilarWords> similarWordsHashMap=new HashMap<String, SimilarWords>();//<word,obj(similar0,similar1,similar2)>
    static ArrayList<String> ListofExpandedQueries=new ArrayList<>();
    static ArrayList<String> StopWordsList = new ArrayList<>();
    static ArrayList<Query_Pcid_score> finalarray=new ArrayList<>();


    static int NumberofDocs=0;

    static ArrayList<Double> Vector=new ArrayList<>();//use: its the vector of each query for vector space model similarity calculation
    static int qmax; //use: keep the max N for each query to calculate tf

    public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException {
        // write your code here
        current_path=System.getProperty("user.dir");
        System.out.println("new version of lalala current path: "+current_path);
        readVocabularyandNormas();
        readQueriesandProfiles();
        readSimilarWords();
        //readDrugBankXml();
        expand_Queries_Profiles_SimilarWords();
        //readPCID_Pointers_To_Documents_with_VectorsFile();
        ReadStopWords();
        clearStructs();
        QueriesEvaluation();
        printfinalArray();

    }

    private static void printfinalArray() throws IOException {
        System.out.println("printfinalArray");
        // "http://www.ncbi.nlm.nih.gov/pubmed/16571130",
        BufferedWriter bw=new BufferedWriter(new FileWriter(current_path+"//CollectionIndex//answers.txt"));
        BufferedWriter bw_extended=new BufferedWriter(new FileWriter(current_path+"//CollectionIndex//answersExtended.txt"));
        for(int i=0;i<finalarray.size();i++){
            Query_Pcid_score ob=finalarray.get(i);
            bw_extended.write(ob.getQuery()+"\n");
            bw.write(ListofQueriesandProfiles.get(i).getQuery()+"\n");
            System.out.println(ob.getQuery());
            for(int j=0;j<ob.getList().size();j++){
                bw_extended.write("http://www.ncbi.nlm.nih.gov/pubmed/"+ob.getList().get(j).getPcid()+"\n");
                bw.write("http://www.ncbi.nlm.nih.gov/pubmed/"+ob.getList().get(j).getPcid()+"\n");
            }
            System.out.println();
        }
        bw.close();
        bw_extended.close();
    }


    private static void clearStructs() {
        System.out.println("clearStructs");
        similarWordsHashMap.clear();

    }


    private static void ReadStopWords() throws IOException {
        System.out.println("ReadStopWords");
        File file = new File(current_path+"//CollectionIndex//stopwordsEn.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String st;
        while ((st = br.readLine()) != null) {
            StopWordsList.add(st);
        }
    }

    private static void readDrugBankXml() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(current_path+"//full database.xml"));
        doc.getDocumentElement().normalize();
        System.out.println("Root element: " + doc.getDocumentElement().getNodeName());

    }


    private static void expand_Queries_Profiles_SimilarWords() throws IOException {
        System.out.println("expand_Queries_Profiles_SimilarWords");
        int count=0;
        for(Query q:ListofQueriesandProfiles){
            System.out.println(++count+") "+q.getQuery() );
            String newQuery=q.getQuery();
            Profile profile=q.getProfile();

            newQuery=newQuery+" "+profile.custom_toString();
            String FinalQuery=addSimilarWords(newQuery);

            ListofExpandedQueries.add(FinalQuery);

        }
    }



    private static void QueriesEvaluation() throws IOException {
        System.out.println("QueriesEvaluation");
        // RandomAccessFile DocVectors = new RandomAccessFile(new File(current_path+"//Documents_with_Vectors.txt"), "r");
        PorterStemmer stemmer = new PorterStemmer();
        int count=0;

        for(String q:ListofExpandedQueries){
            System.out.println("working on "+q);
            qmax=1;
            HashMap<String,WordTfDf_norma> mapofAnswers=new HashMap<>();//pcid, <Arraylist of words/tf/df>/norma
            HashMap<String,Integer> qwords=new HashMap<>();
            HashMap<String,Double> qweights=new HashMap<>();
            ArrayList<PCID_score> listofScores=new ArrayList<>();
            double qnorma=0.0;
            String delimiter = "\t\n\r\f ";
            StringTokenizer tokenizer = new StringTokenizer(q, delimiter);
            while (tokenizer.hasMoreTokens()) {
                //replace all symbols
                String[] str = tokenizer.nextToken().replaceAll("[^\\p{L}]+", " ").split("\\s+");
                for (String tmp_str : str) {
                    if (!StopWordsList.contains(tmp_str) && !tmp_str.equals("")) {
                        stemmer.setCurrent(tmp_str.toLowerCase());
                        stemmer.stem();
                        String stemmedword=stemmer.getCurrent();
                        if(qwords.containsKey(stemmedword)){
                            int termfreq=qwords.get(stemmedword);
                            qwords.put(stemmedword,++termfreq);

                            if(qmax<termfreq){
                                qmax=termfreq;
                                System.out.println("mpainei edw!!!!");
                            }
                        }else{
                            qwords.put(stemmedword,1);
                        }

                    }
                }
            }
            for(Map.Entry entry:qwords.entrySet()){

                String word=entry.getKey().toString();
                int freq=qwords.get(word);
                if(VocabularyFileHashMap.containsKey(word)){
                    double df=(double)VocabularyFileHashMap.get(word).getDocumentFreq();
                    System.out.println("freq: "+freq+", qmax:"+qmax+" Num:"+NumberofDocs+" df:"+df);
                    double weight=((double)freq/(double)qmax) * ( Math.log((double)NumberofDocs/df)/Math.log(2) );
                    System.out.println("word: "+word +" weigtht: "+weight+" df: "+df);
                    qweights.put(word,weight);

                    qnorma=qnorma+Math.pow(weight,2);

                }
                else{
                    qweights.put(word,0.0);
                }

            }
            qnorma=Math.sqrt(qnorma);
            System.out.println("sqrt(qnorma) "+qnorma);
            RandomAccessFile postFile=new RandomAccessFile(new File(current_path+"//CollectionIndex//Merged_PostingFile6.txt"),"r");

            for(Map.Entry entry:qwords.entrySet()){
                String word=entry.getKey().toString();

                if(VocabularyFileHashMap.containsKey(word)){

                    int df=VocabularyFileHashMap.get(word).getDocumentFreq();
                    long pointer=VocabularyFileHashMap.get(word).getPosition();
                    postFile.seek(pointer);
                    System.out.println(word+" "+df);
                    for(int i=0;i<df;i++){
                        String[] line=postFile.readLine().split(" ");
                        //System.out.println("\t"+line[0]+" "+line[1]);
                        String pcid=line[0];
                        double tf=Double.parseDouble(line[1]);
                        if(mapofAnswers.containsKey(pcid)){
                            mapofAnswers.get(pcid).getList().add(new Word_TF_DF(word,tf,df));
                        }
                        else{
                            ArrayList<Word_TF_DF> tmp=new ArrayList<>();
                            tmp.add(new Word_TF_DF(word,tf,df) );
                            mapofAnswers.put(pcid ,new WordTfDf_norma( tmp  , NormasHashMap.get(pcid) ) );
                        }
                    }
                }
                else{
                    qweights.put(word,0.0);
                }

            }
            postFile.close();

            for(Map.Entry pcid:mapofAnswers.entrySet()){
                double cosinesim=0.0;
                WordTfDf_norma obj=mapofAnswers.get(pcid.getKey());
                ArrayList<Word_TF_DF> list=obj.getList();
                double sum=0.0;
                for(Word_TF_DF ob:list){
                    double doc_word_weigtht=ob.getTf() * ( Math.log((( double)NumberofDocs/(double)ob.df ) ) /Math.log(2));

                    double query_word_weight=qweights.get(ob.getWord());
                    sum=sum+(doc_word_weigtht*query_word_weight);
                }

                cosinesim=sum/(qnorma*obj.getNorma());

                listofScores.add(new PCID_score(pcid.getKey().toString(),cosinesim));
            }

            Collections.sort(listofScores, new Comparator<PCID_score>() {
                @Override
                public int compare(PCID_score o1, PCID_score o2) {
                    return Double.compare(o2.getScore(),o1.getScore());
                }
            });
            ArrayList<PCID_score> temp=new ArrayList<>();
            BufferedWriter bw=new BufferedWriter(new FileWriter(current_path+"//output"+count+".txt"));
            bw.write(ListofQueriesandProfiles.get(count).getQuery()+"\n");
            for(int i=0;i<listofScores.size();i++){
                bw.write("PCID: "+listofScores.get(i).getPcid()+" SCORE: "+listofScores.get(i).getScore()+"\n");
                temp.add(listofScores.get(i));
            }
            count++;
            bw.close();
            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.println("----------------------------------------------------------------------------------------------");
            System.out.println("----------------------------------------------------------------------------------------------");
            finalarray.add(new Query_Pcid_score(q,temp));
            if(count==13){
                break;
            }


        }
    }

    private static String addSimilarWords(String Query) {
        //System.out.println("addSimilarWords");
        String finalQuery=Query;
        String[] words=Query.split(" ");
        for(String str:words){
            if(similarWordsHashMap.containsKey(str)){
                SimilarWords similars=similarWordsHashMap.get(str);
                finalQuery=finalQuery+" "+similars.getWord0()+" "+similars.getWord1()+" "+similars.getWord2();
            }
        }
        //String result=lookupfordrugsinDrugbank(finalQuery);
        return finalQuery;
    }

    private static String lookupfordrugsinDrugbank(String finalQuery) {
        return  " ";
    }

    private static void readSimilarWords() throws FileNotFoundException {
        System.out.println("readSimilarWords");
        File similarWordsFile=new File(current_path+"//CollectionIndex//words_and_3Similars.txt");
        Scanner sc=new Scanner(similarWordsFile,"UTF-8");
        while (sc.hasNextLine()){
            String[] words=sc.nextLine().split(" ");
            similarWordsHashMap.put(words[0],new SimilarWords(words[1],words[2],words[3]));
            //System.out.println(words[0]+" similars are:"+words[1]+" "+words[2]+" "+words[3]);
        }
    }

    private static void readQueriesandProfiles() throws IOException, ParseException {
        System.out.println("readQueriesandProfiles");
        JSONParser parser = new JSONParser();
        Reader reader = new FileReader(current_path+"//CollectionIndex//Custom_profile_phaseB_1b_01a.json");
        JSONArray jsonArray = (JSONArray) parser.parse(reader);
        //System.out.println(jsonArray.getClass());
        for(int i=0;i<jsonArray.size();i++){
            JSONObject object=(JSONObject) jsonArray.get(i);
            String question=object.get("Query").toString();
            //System.out.println("query: "+question);


            JSONObject prObject=(JSONObject) object.get("profile");
            String age=prObject.get("age").toString();
            ArrayList<String> medical_examsList = new ArrayList<String>();
            ArrayList<String> drugsList = new ArrayList<String>();
            ArrayList<String> diseasesList = new ArrayList<String>();
            ArrayList<String> treatmentsList = new ArrayList<String>();


            JSONArray medical_exams=(JSONArray)prObject.get("medical_exams");
            if(medical_exams!=null){
                for(int j=0;j<medical_exams.size();j++){
                    medical_examsList.add(medical_exams.get(j).toString());
                }
            }

            JSONArray drugs=(JSONArray)prObject.get("drugs");
            if(drugs!=null){
                for(int j=0;j<drugs.size();j++){
                    drugsList.add(drugs.get(j).toString());
                }
            }

            JSONArray diseases=(JSONArray)prObject.get("diseases");
            if(diseases!=null){
                for(int j=0;j<diseases.size();j++){
                    diseasesList.add(diseases.get(j).toString());
                }
            }

            JSONArray treatments=(JSONArray)prObject.get("treatments");
            if(treatments!=null){
                for(int j=0;j<treatments.size();j++){
                    treatmentsList.add(treatments.get(j).toString());
                }
            }
            String gender=prObject.get("gender").toString();
            String region=prObject.get("region").toString();
            double weight=Double.parseDouble(prObject.get("weight").toString());
            double height=Double.parseDouble(prObject.get("height").toString());

            if(medical_examsList.size()==0 && drugsList.size()==0 && diseasesList.size()==0 && treatmentsList.size()==0){

            }else{
                Profile profile=new Profile();
                profile.setAge(age);
                profile.setMedical_exams(medical_examsList);
                profile.setDrugs(drugsList);
                profile.setDiseases(diseasesList);
                profile.setTreatments(treatmentsList);
                profile.setGender(gender);
                profile.setRegion(region);
                profile.setWeight(weight);
                profile.setHeight(height);

                Query query=new Query(question,profile);

                ListofQueriesandProfiles.add(query);
            }



        }
    }


    private static void readVocabularyandNormas() throws IOException {
        System.out.println("readVocabularyandNormas");
        Scanner VocabularyFile = new Scanner(new File(current_path+"//CollectionIndex//Merged_VocabularyFile6.txt"),"UTF-8");
        while(VocabularyFile.hasNext()){
            String[] line=VocabularyFile.nextLine().split(" ");
            String word=line[0];
            String doc_freq=line[1];
            String position=line[2];
            VocabularyFileHashMap.put(word,new DocFreq_Position(Integer.parseInt(doc_freq),Long.parseLong(position)));
        }
        VocabularyFile.close();
        Scanner normasFile = new Scanner(new File(current_path+"//CollectionIndex//Pcid_normas.txt"),"UTF-8");
        while(normasFile.hasNext()){
            String[] line=normasFile.nextLine().split(" ");
            NormasHashMap.put(line[0],Double.parseDouble(line[1]));
        }
        NumberofDocs=NormasHashMap.size();
        normasFile.close();

    }


}
