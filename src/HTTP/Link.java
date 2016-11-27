package HTTP;

/**
 * Data structure hold links and their extensions
 */
public class Link {

    private String url;
    private String ext;

    /**
     * defualt constructor that devides url and ext
     *
     * @param link the entire link
     */
    public Link(String link) {
        this.url = link.substring(0, link.indexOf('/'));
        this.ext = link.substring(link.indexOf('/') + 1,
                link.indexOf('\"'));
    }

    /**
     * getter method for url
     *
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * getter method for ext
     *
     * @return ext
     */
    public String getExt() {
        return ext;
    }

    /**
     * a combination of both url and ext
     *
     * @return string representation
     */
    public String toString() {
        return getUrl() +
                "/" + getExt();
    }
}
