package DNS;

/**
 * A class to hold records
 */
public class Record {

    private String name;
    private String value;
    private String type;

    /**
     * constructor that sets all the information for the record
     *
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
     *
     * @return name
     */
    public String getName(){
        return this.name;
    }

    /**
     * returns value
     *
     * @return value
     */
    public String getValue(){
        return this.value;
    }

    /**
     * returns type
     *
     * @return type
     */
    public String getType(){
        return this.type;
    }

    /**
     * return true if the records are equal, else false
     *
     * @param b other object
     * @return true if equal, false if not
     */
    public boolean equals(Object b) {
        if (getClass() != b.getClass())
            return false;
        Record r = (Record) b;
        return r.getName().equals(this.getName()) &&
                r.getValue().equals(this.getValue()) &&
                r.getType().equals(this.getType());
    }

    /**
     * prints out the recording in approperiate formatting
     *
     * @return record
     */
    public String toString() {
        return "(" + getName() +
                ", " + getValue() +
                ", " + getType() + ")";
    }
}
