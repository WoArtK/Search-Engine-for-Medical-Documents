import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Record{
    @SerializedName("journal") protected String journal;
    @SerializedName("title") protected String title;
    @SerializedName("meshMajor") protected List<String> meshMajor;
    @SerializedName("year") protected String year;
    @SerializedName("abstractText") protected String abstractText;
    @SerializedName("pmid") protected String pmid;

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getMeshMajor() {
        return meshMajor;
    }

    public void setMeshMajor(List<String> meshMajor) {
        this.meshMajor = meshMajor;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }


    public Record(String journal, String title, List<String> meshMajor, String year, String abstractText, String pmid) {
        this.journal = journal;
        this.title = title;
        this.meshMajor = meshMajor;
        this.year = year;
        this.abstractText = abstractText;
        this.pmid = pmid;
    }

    public Record() {
    }
}

