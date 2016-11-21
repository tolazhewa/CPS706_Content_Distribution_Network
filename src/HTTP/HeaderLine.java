package HTTP;

/**
 * header line
 */
public class HeaderLine {
    private String name;
    private String value;

    /**
     * constructor for header lines
     *
     * @param name  name of the line
     * @param value value of the name
     */
    public HeaderLine(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * returns the name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * return the value
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * returns all the information of the header line
     *
     * @return header line information
     */
    public String toString() {
        return this.getName() + ": " + this.getValue() + "\\r\\n";
    }
}
