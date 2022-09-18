import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Article implements Serializable{

    @SerializedName("articles") protected List<Record> articles;

    public List<Record> getArticles() {
        return articles;
    }

    public void setArticles(List<Record> articles) {
        this.articles = articles;
    }

    public Article(List<Record> articles) {
        this.articles = articles;
    }

    public Article() {
    }
}
