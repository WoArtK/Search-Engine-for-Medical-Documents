/*
 *  apache Lucene Snowball Library for Stemming
 *  TODO: further work, try instead of stemming, to Lemmatization terms
 *        with BioLemmatizer and compare the results
 *
 * */

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import org.json.simple.parser.ParseException;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Index {

    static ArrayList<articleRecord> ListOfArticles = new ArrayList<>();

    static ArrayList<String> StopWordsList = new ArrayList<>();

    static HashMap<String, HashMap<Integer, Integer>> allWords = new HashMap<>(); //<word,<pcid,termFreq>>

    static TreeMap<String, Integer> sortedWords = new TreeMap<>();

    static HashMap<String, HashMap<Integer, Integer>> allWordsStemmed = new HashMap<>(); //<word,<pcid,termFreq>>

    static HashMap<Integer, Long> DocumentsMap = new HashMap<>(); //use in DocumentsFile

    static int MAX_NUMBER_OF_RECORDS = 100000;

    static Queue<String> Voc_Path_queue = new LinkedList<>();

    static Queue<String> Pos_Path_queue = new LinkedList<>();

    static int Voc_File_Counter = 0;

    static int Post_File_Counter = 0;

    static int Merged_Voc_File_Counter = 0;

    static int Merged_Post_File_Counter = 0;

    static long Doc_File_Seek = 0;

    static int current_number_of_records = 0;
    static ArrayList<String> lostwords=new ArrayList<>();
    static String  current_path;




    public static void main(String[] args) throws IOException, ParseException {
        long startTime = System.nanoTime();
        current_path=System.getProperty("user.dir");

        ReadStopWordsFile();

        Create_CollectionFile();

        String delimiter = "\t\n\r\f ";


        JsonFactory f = new MappingJsonFactory();
        JsonParser jp = f.createJsonParser(new File("D:\\BIOASQ\\allMeSH_2019.json"));
        JsonToken current;
        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            System.err.println("Error: root should be object: quiting.");
        }

        File Dfile = new File("C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\DocumentFile.txt");
        RandomAccessFile DocumentFile = new RandomAccessFile(Dfile, "rw");

        while (jp.nextToken() != JsonToken.END_OBJECT) {

            String fieldName = jp.getCurrentName();
            current = jp.nextToken();
            if (fieldName.equals("articles")) {
                if (current == JsonToken.START_ARRAY) {
                    // For each of the records in the array
                    JsonToken token=jp.nextToken();
                    while (token != JsonToken.END_ARRAY) {
                        ArrayList<String> mashmajor_Arraylist = new ArrayList<>();
                        ArrayList<String> Abstract_ArrayList = new ArrayList<>();

                        JsonNode node = jp.readValueAsTree();

                        for(JsonNode str:node.get("meshMajor")){
                            mashmajor_Arraylist.add(str.asText().toLowerCase().replace(","," "));
                        }

                        String journal=node.get("journal").asText().toLowerCase();
                        //String MeshMajor=node.get("meshMajor").asText().toLowerCase();
                        String abstractText=node.get("abstractText").asText().toLowerCase();
                        int pmid=Integer.parseInt(node.get("pmid").asText());
                        String title=node.get("title").asText().toLowerCase();
                        String year;
                        if(node.get("year").asText()!=null){
                            year=node.get("year").asText();
                        }
                        else{
                            year="null";
                        }

                        //tokenizing abstract to take each term separately->remove stopwords
                        StringTokenizer tokenizer = new StringTokenizer(abstractText, delimiter);
                        while (tokenizer.hasMoreTokens()) {
                            //replace all symbols
                            String[] str = tokenizer.nextToken().replaceAll("[^\\p{L}\\p{Nd}\\p{Nl}]+", " ").split("\\s+");
                            for (String tmp_str : str) {
                                if (!StopWordsList.contains(tmp_str) && !tmp_str.equals("")) {
                                    Abstract_ArrayList.add(tmp_str);
                                }
                            }
                        }

                        articleRecord rec = new articleRecord(pmid, journal, mashmajor_Arraylist, year, Abstract_ArrayList, title);

                        String DocumentFile_input;
                        DocumentFile_input = pmid + "\t\"" + title + "\"\t" + year + "\t{";
                        for (String s : mashmajor_Arraylist) {
                            DocumentFile_input = DocumentFile_input + s;
                        }
                        DocumentFile_input = DocumentFile_input + "}\n";
                        DocumentFile.seek(Doc_File_Seek);
                        DocumentFile.write(DocumentFile_input.getBytes("UTF-8"));
                        DocumentsMap.put(pmid, Doc_File_Seek);
                        Doc_File_Seek = DocumentFile.getFilePointer();


                        token=jp.nextToken();
                        if (current_number_of_records <= MAX_NUMBER_OF_RECORDS && token!=JsonToken.END_ARRAY) {
                            ListOfArticles.add(rec);
                        } else {
                            ListOfArticles.add(rec);
                            ApplyPartialIndexing();
                        }

                        current_number_of_records++;


                    }
                } else {
                    System.out.println("Error: records should be an array: skipping.");
                    jp.skipChildren();
                }
            } else {
                System.out.println("Unprocessed property: " + fieldName);
                jp.skipChildren();
            }


        }

        DocumentFile.close();
        merge_Vocabulary_and_Posting_File();
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        long minutes= TimeUnit.NANOSECONDS.toMinutes(totalTime);
        long seconds= TimeUnit.NANOSECONDS.toSeconds(totalTime);
        long hours=TimeUnit.NANOSECONDS.toHours(totalTime);
        System.out.println("running time in nanoseconds="+totalTime);
        System.out.println("running time in seconds="+seconds);
        System.out.println("running time in minitus="+minutes);
        System.out.println("running time in hours="+hours);


        //check_Merged_Files();
        System.out.println("lost words");
        for(String s:lostwords){
            System.out.println(s);
        }
        System.out.println("ent of lost words");

    }


    /*
    * it provides a way to check if everything is ok with pointers.
    * it's slow process. So it's just for testing on a Sample
    * */
    private static void check_Merged_Files() throws IOException {
        File vfile=new File("C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\Merged_VocabularyFile1.txt");
        File pfile=new File("C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\Merged_PostingFile1.txt");
        File dfile=new File("C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\DocumentFile.txt");
        RandomAccessFile merged_VocFile = new RandomAccessFile(vfile, "r");
        RandomAccessFile merged_PostFile = new RandomAccessFile(pfile, "r");
        RandomAccessFile DocFile = new RandomAccessFile(dfile, "r");
        long PostFile_seek = 0;
        long VocFile_seek = 0;
        long DocFile_seek=0;
        merged_VocFile.seek(VocFile_seek);
        String inputVocabularyFile=merged_VocFile.readLine();
        String vline= new String(inputVocabularyFile.getBytes("ISO-8859-1"), "UTF-8");

        while(vline!=null){
            String[] voc1_words = vline.split(" ");
            merged_PostFile.seek(Long.parseLong(voc1_words[2]));
            System.out.println(voc1_words[0]+" "+voc1_words[1]);
            for(int i=0;i<Integer.parseInt(voc1_words[1]);i++) {
                String pline = merged_PostFile.readLine();
                String[] post1_words = pline.split(" ");
                DocFile.seek(Long.parseLong(post1_words[2]));
                String dline=DocFile.readLine();
                String[] doc1_words=dline.split("\t");
                System.out.println(post1_words[1]+" "+doc1_words[0]+" "+doc1_words[1]);
            }
            System.out.println();

            inputVocabularyFile=merged_VocFile.readLine();
            vline= new String(inputVocabularyFile.getBytes("ISO-8859-1"), "UTF-8");

        }
        merged_VocFile.close();
        merged_PostFile.close();
        DocFile.close();

    }

    private static void merge_Vocabulary_and_Posting_File() throws IOException {
        //oso h oura me ta voc_paths exei elements kane loop
        while (!Voc_Path_queue.isEmpty()) {

            //pare to prwto pou mphke kai afairesai to
            String voc_path1 = Voc_Path_queue.remove();
            String pos_path1 = Pos_Path_queue.remove();
            if (!Voc_Path_queue.isEmpty()) { //an exei kai allo shmainei pvw tha prpei na kanoyme merge duo alliws einai
                //to totalmerged auto

                String vocabulary_path = Merged_Voc_File_Name_Creator();//kainourio vocFile gia na valoume ta merged
                String Path_name_vocabularyFile = "C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\" + vocabulary_path + ".txt";
                Voc_Path_queue.add(Path_name_vocabularyFile);//to vazoyme sthn oura gia na to kanoyme kai ayto merge se epomeno stadio
                String posting_path = Merged_Posting_File_Name_Creator();//kanourio postingFile
                String Path_name_postingFile = "C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\" + posting_path + ".txt";
                Pos_Path_queue.add(Path_name_postingFile);//omoiws me to vocfile
                File Vfile = new File(Path_name_vocabularyFile);
                File Pfile = new File(Path_name_postingFile);

                RandomAccessFile merged_VocFile = new RandomAccessFile(Vfile, "rw");
                RandomAccessFile merged_PostFile = new RandomAccessFile(Pfile, "rw");


                //arxikopoioume ekw apo th while gia na kratame pou vriskete to seek sta kainouria arxeia
                //kathe fora
                long merged_PostFile_seek = 0;
                long merged_VocFile_seek = 0;


                //prin kanoume gia prwth fora write tou leme oti tha prepei na grapsei sth thesh 0
                merged_PostFile.seek(0);
                merged_VocFile.seek(0);

                //omoiws me pio panw
                String voc_path2 = Voc_Path_queue.remove();
                System.out.println("merging "+voc_path1+" + "+voc_path2);

                String pos_path2 = Pos_Path_queue.remove();

                RandomAccessFile oldvoc1 = new RandomAccessFile(voc_path1, "r");
                RandomAccessFile oldvoc2 = new RandomAccessFile(voc_path2, "r");
                RandomAccessFile oldpos1 = new RandomAccessFile(pos_path1, "r");
                RandomAccessFile oldpos2 = new RandomAccessFile(pos_path2, "r");


                String inputVoc1=oldvoc1.readLine();
                String inputVoc2=oldvoc2.readLine();

                while(inputVoc1!=null && inputVoc2!=null){
                    String voc1_line=new String(inputVoc1.getBytes("ISO-8859-1"), "UTF-8");
                    String voc2_line=new String(inputVoc2.getBytes("ISO-8859-1"), "UTF-8");

                    String[] voc1_words = voc1_line.split(" ");
                    String[] voc2_words = voc2_line.split(" ");
                    int res = voc1_words[0].compareTo(voc2_words[0]);
                    if(res==0){

                            //einai oi idies lexeis
                            //neo df pou prokuptei apo to sum twn paliwn
                            int df = Integer.parseInt(voc1_words[1]) + Integer.parseInt(voc2_words[1]);

                            //autoi einai oi pointers apo to kathe voc sto antistoixo post gia th lexi
                            long post1_pointer = Long.parseLong(voc1_words[2]);
                            long post2_pointer = Long.parseLong(voc2_words[2]);

                            //kinoume mesa sto kainoyrio vocfile
                            merged_VocFile.seek(merged_VocFile_seek);
                            merged_VocFile.write((voc1_words[0] + " " + df + " " + merged_PostFile_seek + "\n").getBytes("UTF-8"));

                            int df_voc1_word = Integer.parseInt(voc1_words[1]);
                            oldpos1.seek(post1_pointer);
                            for (int i = 0; i < df_voc1_word; i++) {
                                String newLine = oldpos1.readLine() + "\n";
                                merged_PostFile.write(newLine.getBytes("UTF-8"));

                            }

                            int df_voc2_word = Integer.parseInt(voc2_words[1]);
                            oldpos2.seek(post2_pointer);
                            for (int i = 0; i < df_voc2_word; i++) {
                                String newLine = oldpos2.readLine() + "\n";
                                merged_PostFile.write(newLine.getBytes("UTF-8"));

                            }

                            //pairnw tis nees times twn seek gia na kserw pou na paw sthn epomenh loop
                            merged_PostFile_seek = merged_PostFile.getFilePointer();
                            merged_VocFile_seek = merged_VocFile.getFilePointer();


                        inputVoc1=oldvoc1.readLine();
                        inputVoc2=oldvoc2.readLine();


                    }
                    else if(res>0){

                        //edw tha kataxvrhsoyme th lexh apo to w2 kai tha prepei na krathsoume
                        //th lexh apo to voc1 gia na th sugkrinoume me thn epomenh
                            long post2_pointer = Long.parseLong(voc2_words[2]);

                            merged_VocFile.seek(merged_VocFile_seek);
                            merged_VocFile.write((voc2_words[0] + " " + voc2_words[1] + " " + merged_PostFile_seek + "\n").getBytes("UTF-8"));
                            merged_PostFile.seek(merged_PostFile_seek);

                            int df_voc2_word = Integer.parseInt(voc2_words[1]);
                            oldpos2.seek(post2_pointer);
                            for (int i = 0; i < df_voc2_word; i++) {
                                String newLine = oldpos2.readLine() + "\n";
                                merged_PostFile.write(newLine.getBytes("UTF-8"));
                            }

                            merged_PostFile_seek = merged_PostFile.getFilePointer();
                            merged_VocFile_seek = merged_VocFile.getFilePointer();
                            inputVoc2=oldvoc2.readLine();

                    }
                    else {
                        //a.compare(b) to a prepei na einai prin to b
                        //edw tha kataxvrhsoyme th lexh apo to w1 kai tha prepei na krathsoume
                        //th lexh apo to voc2 gia na th sugkrinoume me thn epomenh tou voc1

                            long post1_pointer = Long.parseLong(voc1_words[2]);

                            merged_VocFile.seek(merged_VocFile_seek);
                            merged_VocFile.write((voc1_words[0] + " " + voc1_words[1] + " " + merged_PostFile_seek + "\n").getBytes("UTF-8"));
                            merged_PostFile.seek(merged_PostFile_seek);

                            int df_voc1_word = Integer.parseInt(voc1_words[1]);
                            oldpos1.seek(post1_pointer);
                            for (int i = 0; i < df_voc1_word; i++) {
                                String newLine = oldpos1.readLine() + "\n";
                                merged_PostFile.write(newLine.getBytes("UTF-8"));
                            }

                            merged_PostFile_seek = merged_PostFile.getFilePointer();
                            merged_VocFile_seek = merged_VocFile.getFilePointer();

                        inputVoc1=oldvoc1.readLine();

                    }

                }

                if(inputVoc1==null && inputVoc2==null){
                    //todo edw tha prepei apla na to kleisw


                }
                else if( inputVoc1==null && inputVoc2!=null){
                    //fortwnw ola tis lexeis poy menoyn sto voc2_line

                    while(inputVoc2!=null){

                        String voc2_line=new String(inputVoc2.getBytes("ISO-8859-1"), "UTF-8");

                        String[] voc2_words = voc2_line.split(" ");
                        long post2_pointer = Long.parseLong(voc2_words[2]);

                        merged_VocFile.seek(merged_VocFile_seek);
                        merged_VocFile.write((voc2_words[0] + " " + voc2_words[1] + " " + merged_PostFile_seek+"\n").getBytes("UTF-8"));
                        merged_PostFile.seek(merged_PostFile_seek);

                        oldpos2.seek((post2_pointer));
                        int df=Integer.parseInt(voc2_words[1]);
                        for(int i=0;i<df;i++) {
                            String newLine = oldpos2.readLine() + "\n";
                            merged_PostFile.write(newLine.getBytes("UTF-8"));
                        }

                        merged_PostFile_seek = merged_PostFile.getFilePointer();
                        merged_VocFile_seek = merged_VocFile.getFilePointer();

                        inputVoc2=oldvoc2.readLine();

                    }


                }
                else if(inputVoc1!=null && inputVoc2==null){
                    while(inputVoc1!=null){
                        String voc1_line=new String(inputVoc1.getBytes("ISO-8859-1"), "UTF-8");

                        String[] voc1_words = voc1_line.split(" ");
                        long post1_pointer = Long.parseLong(voc1_words[2]);

                        merged_VocFile.seek(merged_VocFile_seek);
                        merged_VocFile.write((voc1_words[0] + " " + voc1_words[1] + " " + merged_PostFile_seek+"\n").getBytes("UTF-8"));
                        merged_PostFile.seek(merged_PostFile_seek);

                        oldpos1.seek((post1_pointer));
                        int df=Integer.parseInt(voc1_words[1]);
                        for(int i=0;i<df;i++) {
                            String newLine = oldpos1.readLine() + "\n";
                            merged_PostFile.write(newLine.getBytes("UTF-8"));
                        }
                        merged_PostFile_seek = merged_PostFile.getFilePointer();
                        merged_VocFile_seek = merged_VocFile.getFilePointer();

                        inputVoc1=oldvoc1.readLine();

                    }
                }
                else{
                    System.err.println("this is a case should not happen! it happens" +
                            "because while stopped but neither voc1_line nor voc2_line is null");
                }
                delete(new File(voc_path2));
                delete(new File(pos_path2));
                delete(new File(voc_path1));
                delete(new File(pos_path1));
                merged_VocFile.close();
                merged_PostFile.close();

            } else {


                break;
            }


        }

    }


    private static void ApplyPartialIndexing() throws IOException {
        createWordsHashmap();
        StemallWords();
        CreateSortedWordsTree();

        String vocabulary_path = Voc_File_Name_Creator();

        String Path_name_vocabularyFile = "C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\" + vocabulary_path + ".txt";
        Voc_Path_queue.add(Path_name_vocabularyFile);

        String posting_path = Posting_File_Name_Creator();

        String Path_name_postingFile = "C:\\Users\\artemis\\IndexFiles\\CollectionIndex\\" + posting_path + ".txt";
        Pos_Path_queue.add(Path_name_postingFile);

        Write_Words_to_VocabularyFile_and_PostFile(Path_name_vocabularyFile, Path_name_postingFile);

        clear_structs();
    }


    private static void clear_structs() {
        current_number_of_records = 0;
        ListOfArticles.clear();
        allWords.clear();
        sortedWords.clear();
        allWordsStemmed.clear();
    }


    private static void Write_Words_to_VocabularyFile_and_PostFile(String Path_name_vocabularyFile,
                                                                   String Path_name_postingFile) throws IOException {
        File Vfile = new File(Path_name_vocabularyFile);
        File Pfile = new File(Path_name_postingFile);

        RandomAccessFile VocFile = new RandomAccessFile(Vfile, "rw");
        RandomAccessFile PostFile = new RandomAccessFile(Pfile, "rw");

        long VocFile_seek = 0;
        long PostFile_seek = 0;

        for (Map.Entry word : sortedWords.entrySet()) {
            if (allWordsStemmed.containsKey(word.getKey().toString())) {
                String vocabulary_input = word.getKey() + " " + word.getValue() + " " + PostFile_seek + "\n";
                VocFile.seek(VocFile_seek);
                VocFile.write(vocabulary_input.getBytes("UTF-8"));
                VocFile_seek = VocFile.getFilePointer();

                for (Map.Entry pmid : allWordsStemmed.get(word.getKey().toString()).entrySet()) {
                    String post_input = pmid.getKey() + " " + pmid.getValue() + " " + DocumentsMap.get(pmid.getKey()) + "\n";
                    PostFile.seek(PostFile_seek);
                    PostFile.write(post_input.getBytes("UTF-8"));
                    PostFile_seek = PostFile.getFilePointer();
                }

            } else {
                System.err.println("cannot find Stemmed " + word.getKey().toString() + " in allWordsStemmed");
            }

        }

        VocFile.close();
        PostFile.close();
    }


    private static void CreateSortedWordsTree() {
        for (Map.Entry word : allWordsStemmed.entrySet()) {
            int counter = 0;
            sortedWords.put(word.getKey().toString(), allWordsStemmed.get(word.getKey()).entrySet().size());

        }
    }


    private static void Create_CollectionFile() throws IOException {
        File file = new File("C:\\Users\\artemis\\IndexFiles\\CollectionIndex");
        if (!file.exists()) {
            if (file.mkdir()) {
            } else {
                System.err.println("Failed to create directory! 1");
            }
        } else {
            delete(file);
            if (file.mkdir()) {
            } else {
                System.err.println("Failed to create directory! 2");
            }
        }
    }


    public static void delete(File file)
            throws IOException{

        if(file.isDirectory()){

            //directory is empty, then delete it
            if(file.list().length==0){

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            }else{

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if(file.list().length==0){
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        }else{
            //if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }


    static void ReadStopWordsFile() throws IOException {
        File file = new File("C:\\Users\\artemis\\IdeaProjects\\stopwordsEn.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String st;
        while ((st = br.readLine()) != null) {
            StopWordsList.add(st);
        }
    }


    /*
     * this function creates a hashmap of all unique words of all abstracts of articles upto now
     * */
    static void createWordsHashmap() {
        //for each article
        for (articleRecord iter : ListOfArticles) {

            //fpr each word of abstractText of the article
            for (String word : iter.getAbstractText()) {

                //if hashmap of words contains  this word
                if (allWords.containsKey(word)) {
                    //if yes

                    //look if the this word has a record for this particular pmid
                    if (allWords.get(word).containsKey(iter.getPmid())) {
                        //if yes increase tf
                        int counter = allWords.get(word).get(iter.getPmid());
                        allWords.get(word).put(iter.getPmid(), ++counter);
                    } else {
                        //if no, put it on
                        allWords.get(word).put(iter.getPmid(), 1);
                    }
                } else {
                    HashMap<Integer, Integer> tmp = new HashMap<>();
                    tmp.put(iter.getPmid(), 1);
                    allWords.put(word, tmp);
                }

            }

        }
    }


    static void StemallWords() {
        PorterStemmer stemmer = new PorterStemmer();
        for (Map.Entry word : allWords.entrySet()) {

            stemmer.setCurrent(word.getKey().toString());
            stemmer.stem();

            allWordsStemmed.put(stemmer.getCurrent(), allWords.get(word.getKey()));
        }

    }


    static String Voc_File_Name_Creator() {
        return "VocabularyFile" + Voc_File_Counter++;
    }


    static String Posting_File_Name_Creator() {
        return "PostingFile" + Post_File_Counter++;
    }


    static String Merged_Voc_File_Name_Creator() {
        return "Merged_VocabularyFile" + Merged_Voc_File_Counter++;
    }


    static String Merged_Posting_File_Name_Creator() {
        return "Merged_PostingFile" + Merged_Post_File_Counter++;
    }
}
