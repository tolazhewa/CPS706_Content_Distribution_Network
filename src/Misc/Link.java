package Misc;

/**
 * Created by tolaz on 2016-11-22.
 */
public class Link {

    private String url;
    private String ext;

    public Link(String url, String ext){
        this.url = url;
        this.ext = ext;
    }

    public String getUrl() {
        return url;
    }

    public String getExt() {
        return ext;
    }

    public String toString(){
        return getUrl() + getExt();
    }
}
