/**
 * Created by tolaz on 2016-11-07.
 */
public class Record {

    private String name;
    private String value;
    private String type;

    public Record(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName(){
        return this.name;
    }
    public String getValue(){
        return this.value;
    }
    public String getType(){
        return this.type;
    }
    public String toString(){ return "(" + getName() + ", " + getValue() + ", " + getType() + ")"; }


}
