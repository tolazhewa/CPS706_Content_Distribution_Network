/**
 * A class to hold records
 */
public class Record {

    private String name;
    private String value;
    private String type;

    /**
     * constructor that sets all the information for the record
     * @param name name associated with the record
     * @param value value corresponding to the name
     * @param type type of the record
     */
    public Record(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * return name
     * @return name
     */
    public String getName(){
        return this.name;
    }

    /**
     * returns value
     * @return value
     */
    public String getValue(){
        return this.value;
    }

    /**
     * returns type
     * @return type
     */
    public String getType(){
        return this.type;
    }

    /**
     * prints out the recording in approperiate formatting
     * @return record
     */
    public String toString(){ return "(" + getName() + ", " + getValue() + ", " + getType() + ")"; }
}
